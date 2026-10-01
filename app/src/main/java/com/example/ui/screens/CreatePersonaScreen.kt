package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ClinicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePersonaScreen(
    viewModel: ClinicalViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Female") }
    var diagnosis by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var chiefComplaint by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var triggers by remember { mutableStateOf("") }
    var communicationStyle by remember { mutableStateOf("") }
    var clinicalNotes by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Intermediate") }
    var avatarInitial by remember { mutableStateOf("🧑") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Custom Persona", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (name.isNotBlank() && diagnosis.isNotBlank()) {
                        viewModel.createCustomPersona(
                            name = name,
                            age = age.toIntOrNull() ?: 30,
                            gender = gender,
                            diagnosis = diagnosis,
                            summary = summary.ifBlank { "Custom clinical case simulation." },
                            chiefComplaint = chiefComplaint.ifBlank { "Hello, I wanted to talk about what's been happening." },
                            symptoms = symptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            triggers = triggers.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            communicationStyle = communicationStyle.ifBlank { "Open and conversational." },
                            clinicalNotes = clinicalNotes.ifBlank { "Practice active listening." },
                            difficulty = difficulty,
                            avatarInitial = avatarInitial
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.testTag("save_persona_fab")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Persona")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Define Custom Client Persona",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Enter detailed clinical characteristics, symptoms, and behavioral presentation for specialized training scenarios.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Client Full Name *") },
                modifier = Modifier.fillMaxWidth().testTag("input_name"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f).testTag("input_age"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.weight(1f).testTag("input_gender"),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = diagnosis,
                onValueChange = { diagnosis = it },
                label = { Text("Psychological Diagnosis / Condition *") },
                modifier = Modifier.fillMaxWidth().testTag("input_diagnosis"),
                singleLine = true
            )

            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("Clinical Case Summary") },
                modifier = Modifier.fillMaxWidth().testTag("input_summary"),
                maxLines = 3
            )

            OutlinedTextField(
                value = chiefComplaint,
                onValueChange = { chiefComplaint = it },
                label = { Text("Chief Complaint (Opening Quote)") },
                modifier = Modifier.fillMaxWidth().testTag("input_chief_complaint"),
                maxLines = 2
            )

            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                label = { Text("Symptoms (comma separated)") },
                modifier = Modifier.fillMaxWidth().testTag("input_symptoms"),
                singleLine = true
            )

            OutlinedTextField(
                value = triggers,
                onValueChange = { triggers = it },
                label = { Text("Behavioral Triggers (comma separated)") },
                modifier = Modifier.fillMaxWidth().testTag("input_triggers"),
                singleLine = true
            )

            OutlinedTextField(
                value = communicationStyle,
                onValueChange = { communicationStyle = it },
                label = { Text("Communication Style & Tone") },
                modifier = Modifier.fillMaxWidth().testTag("input_communication_style"),
                maxLines = 2
            )

            OutlinedTextField(
                value = clinicalNotes,
                onValueChange = { clinicalNotes = it },
                label = { Text("Student Training Notes & Objectives") },
                modifier = Modifier.fillMaxWidth().testTag("input_clinical_notes"),
                maxLines = 2
            )

            Text("Difficulty Level", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                    FilterChip(
                        selected = difficulty == level,
                        onClick = { difficulty = level },
                        label = { Text(level) },
                        modifier = Modifier.testTag("difficulty_$level")
                    )
                }
            }

            OutlinedTextField(
                value = avatarInitial,
                onValueChange = { avatarInitial = it },
                label = { Text("Avatar Emoji Icon") },
                modifier = Modifier.fillMaxWidth().testTag("input_avatar"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
