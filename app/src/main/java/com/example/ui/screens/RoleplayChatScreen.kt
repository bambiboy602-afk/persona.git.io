package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.Persona
import com.example.viewmodel.ClinicalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleplayChatScreen(
    viewModel: ClinicalViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedPersona by viewModel.selectedPersona.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastFeedback by viewModel.lastFeedback.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showCriteriaDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Current emotional state from last feedback or default
    val currentEmotion = lastFeedback?.emotionalState ?: "Calm"
    val currentEmoji = lastFeedback?.avatarEmoji ?: selectedPersona?.avatarInitial ?: "🧑"

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedPersona?.name ?: "Client Session", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(currentEmotion, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                            Text(selectedPersona?.diagnosis ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCriteriaDialog = true }, modifier = Modifier.testTag("criteria_button")) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Diagnostic Criteria")
                    }
                    IconButton(onClick = { showInfoDialog = true }, modifier = Modifier.testTag("info_button")) {
                        Icon(Icons.Default.Info, contentDescription = "Clinical Info")
                    }
                    IconButton(onClick = { showExportDialog = true }, modifier = Modifier.testTag("export_button")) {
                        Icon(Icons.Default.Share, contentDescription = "Export Chat Log")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Real-time Grading Feedback Bar if available
                    if (lastFeedback != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("Score: ${lastFeedback?.score}/100", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(lastFeedback?.technique ?: "", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Text("Emotion: $currentEmotion", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(lastFeedback?.comment ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Type therapeutic response (Empathy, CBT reframing, MI)...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val text = inputText
                                    inputText = ""
                                    viewModel.sendUserMessage(text)
                                }
                            },
                            enabled = !isLoading && inputText.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("send_chat_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Visual Cues & Avatar Emotional State Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentEmoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Observed Visual & Nonverbal Cues", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(currentEmotion, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                lastFeedback?.nonverbalCues ?: selectedPersona?.communicationStyle ?: "Observing client posture and responsiveness...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(messages) { message ->
                MessageBubble(message = message, onSpeak = { viewModel.speak(message.text) })
            }
        }
    }

    // Diagnostic Criteria Reference Dialog
    if (showCriteriaDialog && selectedPersona != null) {
        val p = selectedPersona!!
        AlertDialog(
            onDismissRequest = { showCriteriaDialog = false },
            title = { Text("DSM-5 Criteria Reference: ${p.diagnosis}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Core Diagnostic Features:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    p.symptoms.forEach { symptom ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("•", fontWeight = FontWeight.Bold)
                            Text(symptom, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Behavioral Triggers:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    p.triggers.forEach { trigger ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("•", fontWeight = FontWeight.Bold)
                            Text(trigger, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Clinical Guidance:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(p.clinicalNotes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCriteriaDialog = false }) { Text("Got it") }
            }
        )
    }

    // Clinical Info Dialog
    if (showInfoDialog && selectedPersona != null) {
        val p = selectedPersona!!
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Clinical Profile: ${p.name}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Diagnosis: ${p.diagnosis}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Age & Gender: ${p.age} yrs, ${p.gender}", fontSize = 13.sp)
                    Text("Chief Complaint: \"${p.chiefComplaint}\"", fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Comprehensive Background & Demographics:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(p.backgroundInfo.ifBlank { p.summary }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Communication Style:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(p.communicationStyle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Close") }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Session Transcript") },
            text = {
                Text("Export chat log and clinical grading scores for instructor review or supervisor portfolio.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    val transcript = messages.joinToString("\n") { "${it.sender.uppercase()}: ${it.text}" }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Clinical Role-Play Transcript: ${selectedPersona?.name}")
                        putExtra(Intent.EXTRA_TEXT, transcript)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Chat Log"))
                }) { Text("Share / Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MessageBubble(message: ChatMessage, onSpeak: () -> Unit) {
    val isUser = message.sender == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        message.text,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    if (!isUser) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Read aloud", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (isUser && message.feedbackScore != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Score: ${message.feedbackScore} | ${message.feedbackTechnique}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
