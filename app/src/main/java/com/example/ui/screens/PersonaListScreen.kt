package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Persona
import com.example.viewmodel.ClinicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaListScreen(
    viewModel: ClinicalViewModel,
    onNavigateBack: () -> Unit,
    onSelectPersona: (Long) -> Unit,
    onOpenGrid: () -> Unit
) {
    val personas by viewModel.personas.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredPersonas = personas.filter { persona ->
        val matchesSearch = persona.name.contains(searchQuery, ignoreCase = true) ||
                persona.diagnosis.contains(searchQuery, ignoreCase = true) ||
                persona.summary.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Beginner" -> persona.difficulty == "Beginner"
            "Intermediate" -> persona.difficulty == "Intermediate"
            "Advanced" -> persona.difficulty == "Advanced"
            "Custom" -> persona.isCustom
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical Persona Library", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGrid, modifier = Modifier.testTag("grid_view_btn")) {
                        Icon(Icons.Default.GridView, contentDescription = "Persona Dashboard Grid")
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, diagnosis, or symptoms...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_personas_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Beginner", "Intermediate", "Advanced", "Custom").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        modifier = Modifier.testTag("filter_$filter")
                    )
                }
            }

            // List of Personas
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPersonas) { persona ->
                    PersonaCard(persona = persona, onClick = {
                        viewModel.selectPersona(persona)
                        onSelectPersona(persona.id)
                    })
                }
            }
        }
    }
}

@Composable
fun PersonaCard(persona: Persona, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("persona_card_${persona.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(persona.avatarInitial, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(persona.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${persona.gender}, ${persona.age} yrs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Badge(
                    containerColor = when (persona.difficulty) {
                        "Beginner" -> MaterialSchemeColor.beginnerContainer()
                        "Intermediate" -> MaterialSchemeColor.intermediateContainer()
                        else -> MaterialSchemeColor.advancedContainer()
                    }
                ) {
                    Text(persona.difficulty, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                persona.diagnosis,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                persona.summary,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    persona.symptoms.take(2).forEach { symptom ->
                        AssistChip(
                            onClick = {},
                            label = { Text(symptom, fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                TextButton(onClick = onClick) {
                    Text("Role-Play →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

object MaterialSchemeColor {
    @Composable fun beginnerContainer() = MaterialTheme.colorScheme.tertiaryContainer
    @Composable fun intermediateContainer() = MaterialTheme.colorScheme.secondaryContainer
    @Composable fun advancedContainer() = MaterialTheme.colorScheme.errorContainer
}
