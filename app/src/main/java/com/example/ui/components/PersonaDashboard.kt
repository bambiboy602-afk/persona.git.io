package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Persona
import com.example.viewmodel.ClinicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaDashboard(
    viewModel: ClinicalViewModel,
    onNavigateBack: () -> Unit,
    onSelectPersona: (Long) -> Unit
) {
    val personas by viewModel.personas.collectAsState()
    var infoPersona by remember { mutableStateOf<Persona?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Persona Grid Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Select a Clinical Persona for Role-Play",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Choose from our 8 pre-loaded psychiatric diagnostic profiles. Tap info to review background, age, family history, and demographics.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(personas) { persona ->
                    PersonaGridCard(
                        persona = persona,
                        onClick = {
                            viewModel.selectPersona(persona)
                            onSelectPersona(persona.id)
                        },
                        onInfoClick = {
                            infoPersona = persona
                        }
                    )
                }
            }
        }
    }

    // Background Info Dialog
    if (infoPersona != null) {
        val p = infoPersona!!
        AlertDialog(
            onDismissRequest = { infoPersona = null },
            title = { Text("Clinical Background: ${p.name}") },
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
                Button(onClick = {
                    val persona = p
                    infoPersona = null
                    viewModel.selectPersona(persona)
                    onSelectPersona(persona.id)
                }) {
                    Text("Start Role-Play")
                }
            },
            dismissButton = {
                TextButton(onClick = { infoPersona = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun PersonaGridCard(persona: Persona, onClick: () -> Unit, onInfoClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable(onClick = onClick)
            .testTag("grid_persona_card_${persona.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (persona.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = persona.imageUrl,
                        contentDescription = persona.name,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(persona.avatarInitial, fontSize = 18.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onInfoClick, modifier = Modifier.size(28.dp).testTag("info_btn_${persona.id}")) {
                        Icon(Icons.Default.Info, contentDescription = "Background Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Badge(
                        containerColor = when (persona.difficulty) {
                            "Beginner" -> MaterialTheme.colorScheme.tertiaryContainer
                            "Intermediate" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ) {
                        Text(persona.difficulty, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    persona.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    persona.diagnosis,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                persona.chiefComplaint,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
