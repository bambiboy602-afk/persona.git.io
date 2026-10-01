package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class TrainingModuleItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val coreCompetency: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingModulesScreen(
    viewModel: ClinicalViewModel,
    onNavigateBack: () -> Unit,
    onSelectModule: (String) -> Unit
) {
    val modules = listOf(
        TrainingModuleItem(
            id = "Diagnosis",
            title = "Differential Diagnosis & Assessment",
            category = "Clinical Evaluation",
            description = "Master symptom inquiry, DSM-5 criteria matching, and differential diagnosis formulation.",
            icon = Icons.Default.Psychology,
            coreCompetency = "Symptom mapping & probing"
        ),
        TrainingModuleItem(
            id = "De-escalation",
            title = "Crisis De-escalation",
            category = "Safety & Crisis",
            description = "Learn verbal de-escalation techniques, lowering agitation, and ensuring physical safety in acute distress.",
            icon = Icons.Default.Warning,
            coreCompetency = "Affect regulation & safety"
        ),
        TrainingModuleItem(
            id = "TIC",
            title = "Trauma-Informed Care (TIC)",
            category = "Core Principles",
            description = "Practice the 6 key principles: safety, trustworthiness, peer support, collaboration, empowerment.",
            icon = Icons.Default.Shield,
            coreCompetency = "Safety & empowerment"
        ),
        TrainingModuleItem(
            id = "CBT",
            title = "Cognitive Behavioral Therapy (CBT)",
            category = "Intervention",
            description = "Identify cognitive distortions, automatic thoughts, and conduct collaborative empiricism.",
            icon = Icons.Default.Lightbulb,
            coreCompetency = "Cognitive reframing"
        ),
        TrainingModuleItem(
            id = "Motivational Interviewing",
            title = "Motivational Interviewing (MI)",
            category = "Engagement",
            description = "Master OARS (Open questions, Affirmations, Reflections, Summaries) and rolling with resistance.",
            icon = Icons.Default.RecordVoiceOver,
            coreCompetency = "OARS & ambivalence resolution"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical Training Modules", fontWeight = FontWeight.Bold) },
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
                Text(
                    "Structured Skill Curriculums",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Select a module to focus your role-play practice and receive automated rubric grading.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(modules) { module ->
                ModuleCard(module = module, onClick = { onSelectModule(module.id) })
            }
        }
    }
}

@Composable
fun ModuleCard(module: TrainingModuleItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module_card_${module.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = module.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(module.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(module.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text(
                module.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("Focus: ${module.coreCompetency}", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 11.sp)
                }
                Button(
                    onClick = onClick,
                    modifier = Modifier.testTag("start_module_${module.id}")
                ) {
                    Text("Launch Module")
                }
            }
        }
    }
}
