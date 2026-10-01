package com.example.data

import android.content.Context
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClinicalRepository(context: Context) {
    private val dao = AppDatabase.getDatabase(context).clinicalDao()

    val allPersonas: Flow<List<Persona>> = dao.getAllPersonas().map { list ->
        val defaults = getDefaultPersonas()
        if (list.isEmpty()) {
            defaults
        } else {
            val dbPersonas = list.map { it.toDomain() }
            // Merge defaults and DB personas ensuring no duplicate IDs
            val dbIds = dbPersonas.map { it.id }.toSet()
            val missingDefaults = defaults.filter { it.id !in dbIds }
            missingDefaults + dbPersonas
        }
    }

    suspend fun getPersona(id: Long): Persona? {
        val entity = dao.getPersonaById(id)
        if (entity != null) return entity.toDomain()
        return getDefaultPersonas().find { it.id == id }
    }

    suspend fun insertPersona(persona: Persona): Long {
        return dao.insertPersona(persona.toEntity())
    }

    suspend fun deletePersona(id: Long) {
        dao.deletePersona(id)
    }

    fun getMessages(personaId: Long): Flow<List<ChatMessage>> {
        return dao.getMessagesForPersona(personaId).map { entities ->
            entities.map {
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

    suspend fun saveMessage(message: ChatMessage): Long {
        val entity = ChatMessageEntity(
            personaId = message.personaId,
            sender = message.sender,
            text = message.text,
            timestamp = message.timestamp,
            feedbackScore = message.feedbackScore,
            feedbackTechnique = message.feedbackTechnique,
            feedbackComment = message.feedbackComment
        )
        return dao.insertMessage(entity)
    }

    fun getAllGrades(): Flow<List<SessionGradeEntity>> {
        return dao.getAllGrades()
    }

    suspend fun saveGrade(grade: SessionGradeEntity) {
        dao.insertGrade(grade)
    }

    companion object {
        fun getDefaultPersonas(): List<Persona> {
            return listOf(
                Persona(
                    id = 1L,
                    name = "Eleanor Vance",
                    age = 42,
                    gender = "Female",
                    diagnosis = "Major Depressive Disorder (MDD)",
                    summary = "Experiencing profound persistent sadness, psychomotor slowing, and feelings of worthlessness following job loss.",
                    chiefComplaint = "I just don't see the point in getting out of bed anymore. Everything feels heavy.",
                    symptoms = listOf("Depressed mood", "Anhedonia", "Fatigue", "Insomnia or hypersomnia", "Feelings of worthlessness"),
                    triggers = listOf("Job layoff", "Social isolation", "Anniversary of divorce"),
                    communicationStyle = "Soft-spoken, hesitant, monosyllabic answers initially, highly responsive to warmth and patience.",
                    clinicalNotes = "Student should practice active listening, validation, and screening for suicidal ideation using gentle probes.",
                    difficulty = "Intermediate",
                    isCustom = false,
                    avatarInitial = "👩‍🦰",
                    backgroundInfo = "Demographics: 42-year-old Caucasian female, divorced 3 years ago.\nStatus: Unemployed former senior accountant, lives alone in a suburban apartment.\nFamily History: Maternal history of clinical depression and alcoholism.\nPsychosocial: Laid off 4 months ago after 15 years at her firm; experienced loss of social support and daily structure.",
                    imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&h=400&fit=crop&crop=faces"
                ),
                Persona(
                    id = 2L,
                    name = "Marcus Brody",
                    age = 29,
                    gender = "Male",
                    diagnosis = "Generalized Anxiety Disorder (GAD) & Panic Attacks",
                    summary = "Pacing, catastrophizing about career and health, somatic chest tightness, hyper-fixation on worst-case scenarios.",
                    chiefComplaint = "My heart won't stop racing. What if I fail my upcoming audit and lose everything?",
                    symptoms = listOf("Excessive worry", "Restlessness", "Muscle tension", "Sleep disturbance", "Catastrophizing"),
                    triggers = listOf("Uncertainty", "Performance reviews", "Health sensations"),
                    communicationStyle = "Rapid speech, interruptive, seeking immediate reassurance, visibly tense.",
                    clinicalNotes = "Student should practice grounding techniques, de-escalation, and identifying cognitive distortions.",
                    difficulty = "Beginner",
                    isCustom = false,
                    avatarInitial = "👨‍💼",
                    backgroundInfo = "Demographics: 29-year-old Hispanic male, single.\nStatus: Junior financial analyst living in a downtown loft with a roommate.\nFamily History: Paternal history of hypertension and severe generalized anxiety.\nPsychosocial: High-pressure corporate environment, perfectionistic tendencies since college, consumes excessive caffeine.",
                    imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop&crop=faces"
                ),
                Persona(
                    id = 3L,
                    name = "Chloe Sullivan",
                    age = 24,
                    gender = "Female",
                    diagnosis = "Borderline Personality Disorder (BPD)",
                    summary = "Intense fear of abandonment, rapidly shifting mood states, idealization and devaluation dynamics.",
                    chiefComplaint = "You're just like everyone else, you don't actually care about helping me!",
                    symptoms = listOf("Emotional dysregulation", "Fear of abandonment", "Unstable interpersonal relationships", "Impulsivity"),
                    triggers = listOf("Perceived rejection", "Delayed replies", "Boundary setting"),
                    communicationStyle = "Polarized language ('always', 'never'), emotionally intense, testing boundaries.",
                    clinicalNotes = "Student must maintain calm, compassionate boundaries, validate emotions without reinforcing splitting.",
                    difficulty = "Advanced",
                    isCustom = false,
                    avatarInitial = "👩‍🎤",
                    backgroundInfo = "Demographics: 24-year-old multiracial female, single, freelance graphic designer.\nStatus: Lives alone; frequent tumultuous romantic relationships.\nFamily History: History of childhood emotional neglect and unstable household dynamics.\nPsychosocial: Chronic feelings of emptiness, impulsive spending, and recurrent self-harm history during extreme distress.",
                    imageUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop&crop=faces"
                ),
                Persona(
                    id = 4L,
                    name = "Dr. Arthur Thorne",
                    age = 51,
                    gender = "Male",
                    diagnosis = "Bipolar I Disorder (Manic Episode)",
                    summary = "Pressured speech, grandiose ideas about saving the global economy overnight, reduced need for sleep (2 hours/night).",
                    chiefComplaint = "I have unlocked the unified theory of finance! I don't need sleep, sleep is for the stagnant!",
                    symptoms = listOf("Grandiosity", "Pressured speech", "Flight of ideas", "Decreased need for sleep", "Distractibility"),
                    triggers = listOf("Caffeine intake", "Stressor events", "Circadian disruption"),
                    communicationStyle = "Rapid-fire monologue, highly enthusiastic, easily irritated if questioned.",
                    clinicalNotes = "Student should practice gentle redirection, reality testing without direct confrontation, and safety assessment.",
                    difficulty = "Advanced",
                    isCustom = false,
                    avatarInitial = "👨‍🔬",
                    backgroundInfo = "Demographics: 51-year-old male, tenured economics professor, married with two college-age children.\nStatus: Currently on administrative leave due to erratic behavior on campus.\nFamily History: Paternal grandfather diagnosed with bipolar disorder.\nPsychosocial: First full manic episode in 5 years precipitated by international conference travel and sleep deprivation.",
                    imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&h=400&fit=crop&crop=faces"
                ),
                Persona(
                    id = 5L,
                    name = "Sam Miller",
                    age = 35,
                    gender = "Male",
                    diagnosis = "Post-Traumatic Stress Disorder (PTSD)",
                    summary = "Hypervigilant, sitting facing the door, startled by sudden noises, intrusive memories from military service.",
                    chiefComplaint = "Every time a car backfires, I'm back in the dust. I can't turn my brain off.",
                    symptoms = listOf("Intrusive memories", "Hypervigilance", "Exaggerated startle response", "Emotional numbing", "Avoidance"),
                    triggers = listOf("Loud sudden noises", "Crowded rooms", "Smells like diesel"),
                    communicationStyle = "Guarded, scanning the environment, brief answers, protective posture.",
                    clinicalNotes = "Student should utilize Trauma-Informed Care (TIC) principles: safety, choice, collaboration, empowerment.",
                    difficulty = "Intermediate",
                    isCustom = false,
                    avatarInitial = "💂‍♂️",
                    backgroundInfo = "Demographics: 35-year-old veteran (former Army Sergeant), married with one young child.\nStatus: Works as a warehouse logistics coordinator.\nFamily History: No prior psychiatric history before military deployment.\nPsychosocial: Served two tours overseas; experiences nightmares, survivor guilt, and difficulty maintaining emotional connection with spouse.",
                    imageUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop&crop=faces"
                ),
                Persona(
                    id = 6L,
                    name = "Clara Dupont",
                    age = 38,
                    gender = "Female",
                    diagnosis = "Obsessive-Compulsive Disorder (OCD)",
                    summary = "Intrusive contamination fears, repetitive hand-washing rituals, distress when symmetry is disrupted.",
                    chiefComplaint = "If I don't check the stove seven times before leaving, something terrible will happen to my children.",
                    symptoms = listOf("Obsessive intrusive thoughts", "Compulsive rituals", "Severe anxiety when prevented from checking", "Time-consuming routines"),
                    triggers = listOf("Touching doorknobs", "Unstructured environments", "Responsibility themes"),
                    communicationStyle = "Anxious, apologetic about rituals, seeking reassurance.",
                    clinicalNotes = "Student should avoid giving false reassurance; practice exposure and response prevention (ERP) principles gently.",
                    difficulty = "Intermediate",
                    isCustom = false,
                    avatarInitial = "👩‍⚕️",
                    backgroundInfo = "Demographics: 38-year-old elementary school teacher, married with two children.\nStatus: Lives in suburban home with family.\nFamily History: Mother had perfectionistic traits and mild washing rituals.\nPsychosocial: Symptoms escalated significantly after the birth of her second child; spends 3+ hours daily on checking rituals.",
                    imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&h=400&fit=crop&crop=faces"
                ),
                Persona(
                    id = 7L,
                    name = "Liam Hayes",
                    age = 31,
                    gender = "Male",
                    diagnosis = "Schizophrenia (Paranoid Subtype)",
                    summary = "Experiencing persecutory delusions, auditory hallucinations commenting on his actions, and guarded interpersonal style.",
                    chiefComplaint = "They are monitoring my apartment through the smart TV and smart bulbs. You have to help me block the signal!",
                    symptoms = listOf("Persecutory delusions", "Auditory hallucinations", "Disorganized thinking", "Social withdrawal", "Suspiciousness"),
                    triggers = listOf("Unfamiliar environments", "Direct questioning about beliefs", "Electronic devices"),
                    communicationStyle = "Guarded, whispering, scanning the room, defensive when challenged.",
                    clinicalNotes = "Student should practice reality-acceptance without reinforcing delusions, validating emotional distress while maintaining safe boundaries.",
                    difficulty = "Advanced",
                    isCustom = false,
                    avatarInitial = "👨‍💻",
                    backgroundInfo = "Demographics: 31-year-old former software engineer, single.\nStatus: Unemployed, living in a small studio apartment with taped-over webcams.\nFamily History: Older brother diagnosed with schizophrenia in his mid-20s.\nPsychosocial: Gradual onset of social withdrawal over 2 years, culminating in quitting his job and isolating from friends.",
                    imageUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&h=400&fit=crop&crop=faces"
                ),
                Persona(
                    id = 8L,
                    name = "Sofia Martinez",
                    age = 20,
                    gender = "Female",
                    diagnosis = "Anorexia Nervosa (Restricting Type)",
                    summary = "Severe body image distortion, intense fear of weight gain, rigorous calorie counting, and denial of emaciation.",
                    chiefComplaint = "I don't need to eat lunch, I already had a glass of water. Everyone is making a big deal out of nothing.",
                    symptoms = listOf("Restricted caloric intake", "Distorted body image", "Intense fear of gaining weight", "Denial of low body weight", "Perfectionism"),
                    triggers = listOf("Family meal times", "Comments about food/weight", "Mirror checks"),
                    communicationStyle = "Defensive, deflective, intellectualizing food and calories, smiling masking inner distress.",
                    clinicalNotes = "Student should build rapport without focusing solely on weight, exploring underlying emotional needs and control.",
                    difficulty = "Advanced",
                    isCustom = false,
                    avatarInitial = "👩‍🎓",
                    backgroundInfo = "Demographics: 20-year-old college sophomore studying pre-med, single.\nStatus: Lives in university dormitory, estranged from campus social life.\nFamily History: Mother has history of chronic dieting and aesthetic scrutiny.\nPsychosocial: High academic pressure, competitive academic environment, controlling food intake as a mechanism for coping with perceived lack of control.",
                    imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&h=400&fit=crop&crop=faces"
                )
            )
        }
    }
}

fun Persona.toEntity(): PersonaEntity {
    return PersonaEntity(
        id = if (id == 0L) 0 else id,
        name = name,
        age = age,
        gender = gender,
        diagnosis = diagnosis,
        summary = summary,
        chiefComplaint = chiefComplaint,
        symptoms = symptoms.joinToString(","),
        triggers = triggers.joinToString(","),
        communicationStyle = communicationStyle,
        clinicalNotes = clinicalNotes,
        difficulty = difficulty,
        isCustom = isCustom,
        avatarInitial = avatarInitial,
        backgroundInfo = backgroundInfo,
        imageUrl = imageUrl
    )
}

fun PersonaEntity.toDomain(): Persona {
    return Persona(
        id = id,
        name = name,
        age = age,
        gender = gender,
        diagnosis = diagnosis,
        summary = summary,
        chiefComplaint = chiefComplaint,
        symptoms = symptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        triggers = triggers.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        communicationStyle = communicationStyle,
        clinicalNotes = clinicalNotes,
        difficulty = difficulty,
        isCustom = isCustom,
        avatarInitial = avatarInitial,
        backgroundInfo = backgroundInfo,
        imageUrl = imageUrl
    )
}
