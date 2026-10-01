package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ClinicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstAidKitScreen(
    viewModel: ClinicalViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical First Aid & Crisis Kit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Emergency Protocol Notice", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 16.sp)
                            Text("If a client expresses active suicidal ideation with intent/plan, immediately follow emergency crisis protocols and contact 988 or emergency services.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            item {
                ProtocolCard(
                    title = "1. Acute De-escalation (LEAPS)",
                    subtitle = "Verbal De-escalation Protocol",
                    steps = listOf(
                        "Listen: Allow the client to vent without interruption.",
                        "Empathize: Acknowledge their emotional pain and validate reality from their perspective.",
                        "Ask questions: Use open-ended inquiry to clarify immediate needs.",
                        "Paraphrase: Reflect back key statements to ensure mutual understanding.",
                        "Summarize & Solve: Collaborate on immediate safety and stabilization steps."
                    ),
                    icon = Icons.Default.RecordVoiceOver
                )
            }

            item {
                ProtocolCard(
                    title = "2. Suicide Risk Assessment (Columbia Protocol)",
                    subtitle = "Direct Ideation & Intent Inquiry",
                    steps = listOf(
                        "Wish to be dead: 'In the past month, have you wished you were dead or could sleep and not wake up?'",
                        "Suicidal thoughts: 'Have you actually thought about suicide?'",
                        "Intent: 'Have you thought about how you might do this?'",
                        "Plan & Means: 'Do you have access to pills, weapons, or means to carry this out?'",
                        "Past behavior: 'Have you ever made a suicide attempt in the past?'"
                    ),
                    icon = Icons.Default.MedicalServices
                )
            }

            item {
                ProtocolCard(
                    title = "3. 5-4-3-2-1 Grounding Technique",
                    subtitle = "For Panic Attacks & Severe Anxiety",
                    steps = listOf(
                        "5 things you can SEE around you.",
                        "4 things you can physically FEEL (e.g., chair, clothing fabric).",
                        "3 things you can HEAR (e.g., traffic, hum of air conditioning).",
                        "2 things you can SMELL (e.g., coffee, soap).",
                        "1 thing you can TASTE (e.g., mint, water)."
                    ),
                    icon = Icons.Default.Psychology
                )
            }

            item {
                ProtocolCard(
                    title = "4. Trauma-Informed Care (TIC) Pillars",
                    subtitle = "Core Clinical Safety Framework",
                    steps = listOf(
                        "Physical and Emotional Safety for both client and clinician.",
                        "Trustworthiness and Transparency in all communications.",
                        "Peer Support and mutual lived-experience validation.",
                        "Collaboration and mutuality in treatment planning.",
                        "Empowerment, voice, and choice at every stage."
                    ),
                    icon = Icons.Default.Shield
                )
            }
        }
    }
}

@Composable
fun ProtocolCard(title: String, subtitle: String, steps: List<String>, icon: ImageVector? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${index + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    Text(step, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
