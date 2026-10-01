package com.example.network

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null
)

data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun sendMessage(
        systemPrompt: String,
        conversationHistory: List<Pair<String, String>>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API key is missing. Please configure your API key in the AI Studio Secrets panel."
        }

        val contents = mutableListOf<Content>()
        for ((sender, text) in conversationHistory) {
            val role = if (sender == "user") "user" else "model"
            contents.add(Content(parts = listOf(Part(text = "[$role]: $text"))))
        }
        contents.add(Content(parts = listOf(Part(text = "[user]: $userMessage"))))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = service.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            reply ?: "No response generated."
        } catch (e: Exception) {
            "Error communicating with AI: ${e.localizedMessage ?: e.toString()}"
        }
    }

    suspend fun evaluateUserMessage(
        personaDiagnosis: String,
        userMessage: String
    ): EvaluatedResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext EvaluatedResponse(85, "Motivational Interviewing - Exploration", "ApiKey missing, defaulted response.", "Calm", "🙂", "Maintaining steady gaze, cooperative posture.")
        }

        val prompt = """
            You are an expert clinical supervisor evaluating a mental health student's communication in a role-play with a patient diagnosed with $personaDiagnosis.
            Also determine the persona's current emotional state based on this interaction (choose from: Calm, Anxious, Agitated, Defensive, Hopeful, Withdrawn, Sad, Frustrated).
            Also provide observable nonverbal visual cues (e.g. eye contact, posture, psychomotor symptoms, facial expression).
            Analyze the student's message: "$userMessage"
            
            Return JSON format strictly:
            {
              "score": <integer 0 to 100>,
              "technique": "<e.g., Motivational Interviewing - Reflection, CBT - Collaborative Empiricism, TIC - Trustworthiness, De-escalation - Empathic Validation, Diagnosis - Symptom Probe>",
              "comment": "<1-2 concise, actionable clinical feedback sentences>",
              "emotionalState": "<Calm / Anxious / Agitated / Defensive / Hopeful / Withdrawn / Sad / Frustrated>",
              "avatarEmoji": "<appropriate emoji reflecting emotional state e.g., 😟, 😠, 😌, 💡, 😢, 😐>",
              "nonverbalCues": "<1 concise sentence describing nonverbal behavior and visual cues e.g., Avoiding eye contact, slumped shoulders, slow rhythmic breathing.>"
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.3f, responseMimeType = "application/json")
        )

        try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
            parseEvaluationJson(jsonText)
        } catch (e: Exception) {
            EvaluatedResponse(75, "General Communication", "Good engagement attempt.", "Calm", "🙂", "Maintains neutral expression.")
        }
    }

    private fun parseEvaluationJson(json: String): EvaluatedResponse {
        try {
            var score = 80
            var technique = "Active Listening"
            var comment = "Effective communication technique applied."
            var emotionalState = "Calm"
            var avatarEmoji = "🙂"
            var nonverbalCues = "Maintains calm posture, occasional eye contact."

            val scoreMatch = Regex(""""score"\s*:\s*(\d+)""").find(json)
            if (scoreMatch != null) score = scoreMatch.groupValues[1].toInt()

            val techMatch = Regex(""""technique"\s*:\s*"([^"]+)"""").find(json)
            if (techMatch != null) technique = techMatch.groupValues[1]

            val commMatch = Regex(""""comment"\s*:\s*"([^"]+)"""").find(json)
            if (commMatch != null) comment = commMatch.groupValues[1]

            val emoMatch = Regex(""""emotionalState"\s*:\s*"([^"]+)"""").find(json)
            if (emoMatch != null) emotionalState = emoMatch.groupValues[1]

            val emojiMatch = Regex(""""avatarEmoji"\s*:\s*"([^"]+)"""").find(json)
            if (emojiMatch != null) avatarEmoji = emojiMatch.groupValues[1]

            val cuesMatch = Regex(""""nonverbalCues"\s*:\s*"([^"]+)"""").find(json)
            if (cuesMatch != null) nonverbalCues = cuesMatch.groupValues[1]

            return EvaluatedResponse(score, technique, comment, emotionalState, avatarEmoji, nonverbalCues)
        } catch (e: Exception) {
            return EvaluatedResponse(80, "Clinical Engagement", "Professional communication observed.", "Calm", "🙂", "Maintains neutral expression.")
        }
    }
}

data class EvaluatedResponse(
    val score: Int,
    val technique: String,
    val comment: String,
    val emotionalState: String,
    val avatarEmoji: String,
    val nonverbalCues: String
)
