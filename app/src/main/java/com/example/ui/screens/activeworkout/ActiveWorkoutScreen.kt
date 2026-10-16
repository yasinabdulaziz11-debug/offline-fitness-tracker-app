package com.example.ui.screens.activeworkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.TimeUtils
import com.example.core.UnitConverter
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.SetType
import com.example.data.local.entity.TrackingType
import com.example.data.local.entity.WeightUnit
import com.example.data.local.entity.WorkoutSetEntity
import com.example.data.repository.SessionExerciseDetail
import com.example.ui.components.RestTimerOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val timerState by viewModel.restTimerManager.timerState.collectAsStateWithLifecycle()

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.workoutTitle.ifBlank { "Active Workout" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.togglePause() }
                        ) {
                            Icon(
                                imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (state.isPaused) "Resume" else "Pause",
                                tint = if (state.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = TimeUtils.formatDuration(state.elapsedSeconds),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            if (state.isPaused) {
                                Text(
                                    text = "(PAUSED)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("active_workout_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.finishWorkout(onNavigateBack) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("finish_workout_button")
                    ) {
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Floating Rest Timer
            RestTimerOverlay(
                timerState = timerState,
                onPause = { viewModel.restTimerManager.pauseTimer() },
                onResume = { viewModel.restTimerManager.resumeTimer() },
                onAddTime = { viewModel.restTimerManager.addTime(it) },
                onReset = { viewModel.restTimerManager.resetTimer() },
                onDismiss = { viewModel.restTimerManager.dismissTimer() }
            )
        },
        modifier = modifier.testTag("active_workout_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workout Name & Notes inputs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.workoutTitle,
                            onValueChange = { viewModel.updateWorkoutTitle(it) },
                            label = { Text("Workout Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("workout_title_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        OutlinedTextField(
                            value = state.workoutNotes,
                            onValueChange = { viewModel.updateWorkoutNotes(it) },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("workout_notes_input"),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Exercise Cards
            val exercises = state.sessionDetail?.exercises ?: emptyList()
            if (exercises.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "No Exercises Added",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap the button below to add exercises from your library.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(exercises, key = { it.sessionExercise.id }) { itemDetail ->
                    ExerciseCard(
                        detail = itemDetail,
                        weightUnit = state.weightUnit,
                        onAddSet = { viewModel.addSet(itemDetail.sessionExercise.id) },
                        onRemoveSet = { viewModel.removeSet(it) },
                        onToggleSetCompleted = { viewModel.toggleSetCompleted(it) },
                        onChangeSetType = { set, type -> viewModel.changeSetType(set, type) },
                        onUpdateSetValues = { set, w, r, d, dist ->
                            viewModel.updateSetValues(set, w, r, d, dist)
                        },
                        onRemoveExercise = { viewModel.removeExercise(itemDetail.sessionExercise.id) }
                    )
                }
            }

            // Action Buttons: Add Exercise & Cancel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.openExercisePicker() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_exercise_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Exercise", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.requestCancelWorkout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cancel_workout_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Workout")
                    }
                }
            }
        }
    }

    // Exercise Picker Dialog
    if (state.isExercisePickerOpen) {
        ExercisePickerDialog(
            exercises = state.availableExercises,
            onSelectExercise = { viewModel.addExercise(it) },
            onDismiss = { viewModel.closeExercisePicker() }
        )
    }

    // Cancel Workout Confirmation Dialog
    if (state.isCancelConfirmationOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelConfirmation() },
            title = { Text("Cancel Workout?") },
            text = { Text("Are you sure you want to cancel this workout? All sets and progress logged during this session will be discarded.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmCancelWorkout(onNavigateBack) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_cancel_workout_button")
                ) {
                    Text("Discard Workout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissCancelConfirmation() }
                ) {
                    Text("Resume")
                }
            }
        )
    }

    // Workout Complete Summary Dialog
    if (state.isFinishSummaryOpen && state.workoutSummary != null) {
        val summary = state.workoutSummary!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissFinishSummary(onNavigateBack) },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text("Workout Complete!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = summary.workoutName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• Duration: ${TimeUtils.formatDurationReadable(summary.durationSeconds)}")
                    Text("• Completed Exercises: ${summary.completedExercisesCount}")
                    Text("• Completed Sets: ${summary.completedSetsCount}")
                    Text("• Total Reps: ${summary.totalReps}")
                    Text("• Total Volume: ${summary.totalVolumeKg.toInt()} kg")

                    if (summary.newRecordsAchieved.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New Personal Records! 🎉",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        summary.newRecordsAchieved.forEach { pr ->
                            Text(
                                "🏆 ${pr.exerciseName}: ${UnitConverter.formatWeight(pr.maxWeightKg, state.weightUnit)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissFinishSummary(onNavigateBack) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("finish_summary_done_button")
                ) {
                    Text("Done", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ExerciseCard(
    detail: SessionExerciseDetail,
    weightUnit: WeightUnit,
    onAddSet: () -> Unit,
    onRemoveSet: (Long) -> Unit,
    onToggleSetCompleted: (WorkoutSetEntity) -> Unit,
    onChangeSetType: (WorkoutSetEntity, SetType) -> Unit,
    onUpdateSetValues: (WorkoutSetEntity, String, String, String, String) -> Unit,
    onRemoveExercise: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_exercise_card_${detail.sessionExercise.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Exercise Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = detail.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${detail.exercise.muscleGroup.displayName} • ${detail.exercise.equipment.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRemoveExercise,
                        modifier = Modifier.testTag("remove_exercise_${detail.sessionExercise.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove exercise",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Sets Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SET",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(44.dp)
                )
                if (detail.exercise.trackingType == TrackingType.WEIGHT_AND_REPS) {
                    Text(
                        text = weightUnit.symbol.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "REPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                } else if (detail.exercise.trackingType == TrackingType.REPS_ONLY) {
                    Text(
                        text = "REPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "SECS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "DONE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Set Rows
            detail.sets.forEach { setItem ->
                SetRow(
                    set = setItem,
                    trackingType = detail.exercise.trackingType,
                    weightUnit = weightUnit,
                    onToggleCompleted = { onToggleSetCompleted(setItem) },
                    onChangeType = { onChangeSetType(setItem, it) },
                    onUpdateValues = { w, r, d, dist ->
                        onUpdateSetValues(setItem, w, r, d, dist)
                    },
                    onDelete = { onRemoveSet(setItem.id) }
                )
            }

            // Add Set Button
            OutlinedButton(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_set_button_${detail.sessionExercise.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Set",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    set: WorkoutSetEntity,
    trackingType: TrackingType,
    weightUnit: WeightUnit,
    onToggleCompleted: () -> Unit,
    onChangeType: (SetType) -> Unit,
    onUpdateValues: (String, String, String, String) -> Unit,
    onDelete: () -> Unit
) {
    var weightInput by remember(set.weightKg) {
        val disp = UnitConverter.displayWeight(set.weightKg, weightUnit)
        mutableStateOf(disp?.let { if (it % 1.0 == 0.0) it.toLong().toString() else String.format(java.util.Locale.US, "%.1f", it) } ?: "")
    }
    var repsInput by remember(set.reps) {
        mutableStateOf(set.reps?.toString() ?: "")
    }
    var durationInput by remember(set.durationSeconds) {
        mutableStateOf(set.durationSeconds?.toString() ?: "")
    }
    var distanceInput by remember(set.distanceMeters) {
        mutableStateOf(set.distanceMeters?.toString() ?: "")
    }

    var typeMenuOpen by remember { mutableStateOf(false) }

    val rowBackground = if (set.isCompleted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .testTag("set_row_${set.id}"),
        color = rowBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set tag / type button
            Box {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clickable { typeMenuOpen = true }
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = if (set.setType == SetType.NORMAL) "${set.setNumber}" else set.setType.shortName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (set.setType == SetType.WARMUP) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                DropdownMenu(
                    expanded = typeMenuOpen,
                    onDismissRequest = { typeMenuOpen = false }
                ) {
                    SetType.values().forEach { st ->
                        DropdownMenuItem(
                            text = { Text("${st.shortName} - ${st.displayName}") },
                            onClick = {
                                typeMenuOpen = false
                                onChangeType(st)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete Set", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            typeMenuOpen = false
                            onDelete()
                        }
                    )
                }
            }

            // Input Fields based on TrackingType
            if (trackingType == TrackingType.WEIGHT_AND_REPS) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = {
                        weightInput = it
                        onUpdateValues(weightInput, repsInput, durationInput, distanceInput)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("set_weight_input_${set.id}"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = repsInput,
                    onValueChange = {
                        repsInput = it
                        onUpdateValues(weightInput, repsInput, durationInput, distanceInput)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("set_reps_input_${set.id}"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )
            } else if (trackingType == TrackingType.REPS_ONLY) {
                OutlinedTextField(
                    value = repsInput,
                    onValueChange = {
                        repsInput = it
                        onUpdateValues(weightInput, repsInput, durationInput, distanceInput)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .padding(horizontal = 4.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                OutlinedTextField(
                    value = durationInput,
                    onValueChange = {
                        durationInput = it
                        onUpdateValues(weightInput, repsInput, durationInput, distanceInput)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .padding(horizontal = 4.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Complete Checkbox Button (48dp touch target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onToggleCompleted() }
                    .testTag("set_complete_button_${set.id}"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (set.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (!set.isCompleted) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (set.isCompleted) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<ExerciseEntity>,
    onSelectExercise: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<MuscleGroup?>(null) }

    val filtered = exercises.filter { ex ->
        val matchesSearch = ex.name.contains(searchQuery, ignoreCase = true)
        val matchesMuscle = selectedMuscle == null || ex.muscleGroup == selectedMuscle
        matchesSearch && matchesMuscle
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Exercise", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercise...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_picker_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Muscle Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val allMuscles = MuscleGroup.values()
                    val displayedChips = allMuscles.take(4) // show top muscle categories
                    displayedChips.forEach { muscle ->
                        val isSelected = selectedMuscle == muscle
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                selectedMuscle = if (isSelected) null else muscle
                            }
                        ) {
                            Text(
                                text = muscle.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { exercise ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectExercise(exercise.id) }
                                .testTag("exercise_picker_item_${exercise.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${exercise.muscleGroup.displayName} • ${exercise.equipment.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onSelectExercise(exercise.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
