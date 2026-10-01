package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: String,
    val diagnosis: String,
    val summary: String,
    val chiefComplaint: String,
    val symptoms: String,
    val triggers: String,
    val communicationStyle: String,
    val clinicalNotes: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val isCustom: Boolean = false,
    val avatarInitial: String = "👤",
    val backgroundInfo: String = "",
    val imageUrl: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personaId: Long,
    val sender: String, // "user" or "persona"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val feedbackScore: Int? = null, // 0-100 grading for user messages
    val feedbackTechnique: String? = null, // e.g., "Motivational Interviewing - Reflection", "CBT - Reframing"
    val feedbackComment: String? = null
)

@Entity(tableName = "session_grades")
data class SessionGradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personaId: Long,
    val moduleName: String, // Diagnosis, De-escalation, TIC, CBT, Motivational Interviewing
    val averageScore: Int,
    val summaryFeedback: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Persona(
    val id: Long,
    val name: String,
    val age: Int,
    val gender: String,
    val diagnosis: String,
    val summary: String,
    val chiefComplaint: String,
    val symptoms: List<String>,
    val triggers: List<String>,
    val communicationStyle: String,
    val clinicalNotes: String,
    val difficulty: String,
    val isCustom: Boolean,
    val avatarInitial: String,
    val backgroundInfo: String,
    val imageUrl: String
)

data class ChatMessage(
    val id: Long = 0,
    val personaId: Long,
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val feedbackScore: Int? = null,
    val feedbackTechnique: String? = null,
    val feedbackComment: String? = null
)
