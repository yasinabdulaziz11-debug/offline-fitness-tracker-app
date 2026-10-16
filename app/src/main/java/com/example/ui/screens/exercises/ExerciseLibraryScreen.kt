package com.example.ui.screens.exercises

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Equipment
import com.example.data.local.entity.ExerciseCategory
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.TrackingType
import com.example.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseLibraryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        onNavigateBack()
    }

    val filtered = state.exercises.filter { ex ->
        val matchesSearch = ex.name.contains(state.searchQuery, ignoreCase = true)
        val matchesMuscle = state.selectedMuscle == null || ex.muscleGroup == state.selectedMuscle
        val matchesEquipment = state.selectedEquipment == null || ex.equipment == state.selectedEquipment
        matchesSearch && matchesMuscle && matchesEquipment
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exercise Library", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("exercise_library_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_custom_exercise_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Custom Exercise")
            }
        },
        modifier = modifier.testTag("exercise_library_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("exercise_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Muscle Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    val isAll = state.selectedMuscle == null
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { viewModel.setSelectedMuscle(null) }
                    ) {
                        Text(
                            text = "All Muscles",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isAll) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                items(MuscleGroup.values()) { muscle ->
                    val isSelected = state.selectedMuscle == muscle
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            viewModel.setSelectedMuscle(if (isSelected) null else muscle)
                        }
                    ) {
                        Text(
                            text = muscle.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Exercise List
            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.FilterList,
                    title = "No Exercises Found",
                    description = "No exercise matches your current search or filter. You can add a custom exercise using the button below."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { ex ->
                        ExerciseCardItem(
                            exercise = ex,
                            onClick = { viewModel.openExerciseDetail(ex) }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (state.selectedExerciseForDetail != null) {
        val ex = state.selectedExerciseForDetail!!
        AlertDialog(
            onDismissRequest = { viewModel.closeExerciseDetail() },
            title = {
                Text(ex.name, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgeChip(ex.muscleGroup.displayName)
                        BadgeChip(ex.equipment.displayName)
                        BadgeChip(ex.category.displayName)
                    }
                    if (ex.instructions.isNotBlank()) {
                        Text(
                            text = "Instructions & Form:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = ex.instructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ex.isCustom) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.archiveExercise(ex.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Archive Custom Exercise")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeExerciseDetail() }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Custom Exercise Dialog
    if (state.isAddDialogOpen) {
        AddCustomExerciseDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { name, muscle, equip, cat, track, instr ->
                viewModel.addCustomExercise(name, muscle, equip, cat, track, instr)
            }
        )
    }
}

@Composable
private fun ExerciseCardItem(
    exercise: ExerciseEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("exercise_item_${exercise.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (exercise.isCustom) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Custom",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${exercise.muscleGroup.displayName} • ${exercise.equipment.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BadgeChip(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, MuscleGroup, Equipment, ExerciseCategory, TrackingType, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedEquipment by remember { mutableStateOf(Equipment.DUMBBELL) }
    var selectedCategory by remember { mutableStateOf(ExerciseCategory.STRENGTH) }
    var selectedTracking by remember { mutableStateOf(TrackingType.WEIGHT_AND_REPS) }
    var instructions by remember { mutableStateOf("") }

    var muscleMenuOpen by remember { mutableStateOf(false) }
    var equipMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Custom Exercise", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g. Incline Cable Fly") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_exercise_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Muscle Group Dropdown
                ExposedDropdownMenuBox(
                    expanded = muscleMenuOpen,
                    onExpandedChange = { muscleMenuOpen = !muscleMenuOpen }
                ) {
                    OutlinedTextField(
                        value = selectedMuscle.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Muscle Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleMenuOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = muscleMenuOpen,
                        onDismissRequest = { muscleMenuOpen = false }
                    ) {
                        MuscleGroup.values().forEach { mg ->
                            DropdownMenuItem(
                                text = { Text(mg.displayName) },
                                onClick = {
                                    selectedMuscle = mg
                                    muscleMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Equipment Dropdown
                ExposedDropdownMenuBox(
                    expanded = equipMenuOpen,
                    onExpandedChange = { equipMenuOpen = !equipMenuOpen }
                ) {
                    OutlinedTextField(
                        value = selectedEquipment.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipMenuOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = equipMenuOpen,
                        onDismissRequest = { equipMenuOpen = false }
                    ) {
                        Equipment.values().forEach { eq ->
                            DropdownMenuItem(
                                text = { Text(eq.displayName) },
                                onClick = {
                                    selectedEquipment = eq
                                    equipMenuOpen = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (optional)") },
                    placeholder = { Text("Key form cues and safety points") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedMuscle, selectedEquipment, selectedCategory, selectedTracking, instructions)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_exercise_button")
            ) {
                Text("Create", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
