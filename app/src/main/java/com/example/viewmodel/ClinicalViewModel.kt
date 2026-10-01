package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ClinicalRepository
import com.example.model.*
import com.example.network.EvaluatedResponse
import com.example.network.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class ClinicalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ClinicalRepository(application)
    private val dao = AppDatabase.getDatabase(application).clinicalDao()

    private var tts: TextToSpeech? = null
    var isTtsInitialized = false
        private set

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsInitialized = true
            }
        }
    }

    val personas: StateFlow<List<Persona>> = repository.allPersonas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ClinicalRepository.getDefaultPersonas()
        )

    val grades: StateFlow<List<SessionGradeEntity>> = repository.getAllGrades()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPersona = MutableStateFlow<Persona?>(null)
    val selectedPersona: StateFlow<Persona?> = _selectedPersona

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _lastFeedback = MutableStateFlow<EvaluatedResponse?>(null)
    val lastFeedback: StateFlow<EvaluatedResponse?> = _lastFeedback

    fun selectPersona(persona: Persona) {
        _selectedPersona.value = persona
        _lastFeedback.value = null
        viewModelScope.launch {
            dao.getMessagesForPersona(persona.id).collect { entities ->
                _messages.value = entities.map {
                    ChatMessage(
                        id = it.id,
                        personaId = it.personaId,
                        sender = it.sender,
                        text = it.text,
                        timestamp = it.timestamp,
                        feedbackScore = it.feedbackScore,
                        feedbackTechnique = it.feedbackTechnique,
                        feedbackComment = it.feedbackComment
                    )
                }
            }
        }
        viewModelScope.launch {
            val current = _messages.value
            if (current.isEmpty()) {
                val openingText = persona.chiefComplaint
                val msg = ChatMessage(
                    personaId = persona.id,
                    sender = "persona",
                    text = openingText
                )
                repository.saveMessage(msg)
                speak(openingText)
            }
        }
    }

    fun sendUserMessage(text: String) {
        val persona = _selectedPersona.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Evaluate user message using Gemini
            val evaluation = GeminiClient.evaluateUserMessage(persona.diagnosis, text)
            _lastFeedback.value = evaluation

            // 2. Save user message with feedback
            val userMsg = ChatMessage(
                personaId = persona.id,
                sender = "user",
                text = text,
                feedbackScore = evaluation.score,
                feedbackTechnique = evaluation.technique,
                feedbackComment = evaluation.comment
            )
            repository.saveMessage(userMsg)

            // Save grade entity for analytics
            val gradeEntity = SessionGradeEntity(
                personaId = persona.id,
                moduleName = evaluation.technique,
                averageScore = evaluation.score,
                summaryFeedback = evaluation.comment
            )
            repository.saveGrade(gradeEntity)

            // 3. Build conversation history for persona prompt
            val history = _messages.value.map { it.sender to it.text }

            val systemPrompt = """
                You are role-playing as a psychiatric patient in a clinical training session for mental health professionals.
                Name: ${persona.name}
                Age: ${persona.age}
                Gender: ${persona.gender}
                Diagnosis: ${persona.diagnosis}
                Summary: ${persona.summary}
                Symptoms: ${persona.symptoms.joinToString(", ")}
                Triggers: ${persona.triggers.joinToString(", ")}
                Communication Style: ${persona.communicationStyle}
                Clinical Notes for student context: ${persona.clinicalNotes}
                Current Emotional State: ${evaluation.emotionalState}
                
                Instructions:
                - Stay strictly in character as ${persona.name}. Embody the behavioral patterns, emotional tone, and symptoms of ${persona.diagnosis}.
                - Reflect your current emotional state (${evaluation.emotionalState}) in your words and tone.
                - Respond realistically and conversationally to what the student/user just said. Do not break character or reveal that you are an AI.
                - Keep responses concise (2-4 sentences) like a real therapy session.
            """.trimIndent()

            val replyText = GeminiClient.sendMessage(systemPrompt, history, text)

            // 4. Save persona reply
            val personaMsg = ChatMessage(
                personaId = persona.id,
                sender = "persona",
                text = replyText
            )
            repository.saveMessage(personaMsg)
            _isLoading.value = false

            speak(replyText)
        }
    }

    fun createCustomPersona(
        name: String,
        age: Int,
        gender: String,
        diagnosis: String,
        summary: String,
        chiefComplaint: String,
        symptoms: List<String>,
        triggers: List<String>,
        communicationStyle: String,
        clinicalNotes: String,
        difficulty: String,
        avatarInitial: String
    ) {
        viewModelScope.launch {
            val persona = Persona(
                id = 0L,
                name = name,
                age = age,
                gender = gender,
                diagnosis = diagnosis,
                summary = summary,
                chiefComplaint = chiefComplaint,
                symptoms = symptoms,
                triggers = triggers,
                communicationStyle = communicationStyle,
                clinicalNotes = clinicalNotes,
                difficulty = difficulty,
                isCustom = true,
                avatarInitial = avatarInitial,
                backgroundInfo = "Custom user-defined clinical case simulation.",
                imageUrl = ""
            )
            repository.insertPersona(persona)
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
