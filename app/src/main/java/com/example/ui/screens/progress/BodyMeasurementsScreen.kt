package com.example.ui.screens.progress

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.TimeUtils
import com.example.core.UnitConverter
import com.example.data.local.entity.BodyMeasurementEntity
import com.example.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isAddDialogOpen by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Body Measurements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("body_measurements_back")
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
                onClick = { isAddDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_measurement_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Measurement")
            }
        },
        modifier = modifier.testTag("body_measurements_screen")
    ) { innerPadding ->
        if (state.measurements.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Scale,
                title = "No Measurements Logged",
                description = "Record your body weight and circumference measurements (waist, chest, arms, hips, thighs) to keep track of your physical changes offline.",
                actionLabel = "+ Log First Entry",
                onActionClick = { isAddDialogOpen = true },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.measurements, key = { it.id }) { measurement ->
                    MeasurementCardItem(
                        item = measurement,
                        weightUnit = state.weightUnit,
                        lengthUnit = state.lengthUnit,
                        onDelete = { viewModel.deleteMeasurement(measurement.id) }
                    )
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AddMeasurementDialog(
            weightUnit = state.weightUnit,
            lengthUnit = state.lengthUnit,
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { weightKg, waistCm, chestCm, hipsCm, armsCm, thighsCm, notes ->
                isAddDialogOpen = false
                viewModel.addMeasurement(weightKg, waistCm, chestCm, hipsCm, armsCm, thighsCm, notes)
            }
        )
    }
}

@Composable
private fun MeasurementCardItem(
    item: BodyMeasurementEntity,
    weightUnit: com.example.data.local.entity.WeightUnit,
    lengthUnit: com.example.data.local.entity.LengthUnit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeUtils.formatDate(item.timestamp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (item.weightKg != null && item.weightKg > 0) {
                    MeasurementMetric("Weight", UnitConverter.formatWeight(item.weightKg, weightUnit))
                }
                if (item.waistCm != null && item.waistCm > 0) {
                    MeasurementMetric("Waist", UnitConverter.formatLength(item.waistCm, lengthUnit))
                }
                if (item.chestCm != null && item.chestCm > 0) {
                    MeasurementMetric("Chest", UnitConverter.formatLength(item.chestCm, lengthUnit))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (item.armsCm != null && item.armsCm > 0) {
                    MeasurementMetric("Arms", UnitConverter.formatLength(item.armsCm, lengthUnit))
                }
                if (item.thighsCm != null && item.thighsCm > 0) {
                    MeasurementMetric("Thighs", UnitConverter.formatLength(item.thighsCm, lengthUnit))
                }
                if (item.hipsCm != null && item.hipsCm > 0) {
                    MeasurementMetric("Hips", UnitConverter.formatLength(item.hipsCm, lengthUnit))
                }
            }

            if (item.notes.isNotBlank()) {
                Text(
                    text = "Note: ${item.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MeasurementMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AddMeasurementDialog(
    weightUnit: com.example.data.local.entity.WeightUnit,
    lengthUnit: com.example.data.local.entity.LengthUnit,
    onDismiss: () -> Unit,
    onConfirm: (Double?, Double?, Double?, Double?, Double?, Double?, String) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }
    var chestInput by remember { mutableStateOf("") }
    var hipsInput by remember { mutableStateOf("") }
    var armsInput by remember { mutableStateOf("") }
    var thighsInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Body Measurements", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Weight (${weightUnit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("measurement_weight_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = waistInput,
                        onValueChange = { waistInput = it },
                        label = { Text("Waist (${lengthUnit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = chestInput,
                        onValueChange = { chestInput = it },
                        label = { Text("Chest (${lengthUnit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = armsInput,
                        onValueChange = { armsInput = it },
                        label = { Text("Arms (${lengthUnit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = thighsInput,
                        onValueChange = { thighsInput = it },
                        label = { Text("Thighs (${lengthUnit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = hipsInput,
                    onValueChange = { hipsInput = it },
                    label = { Text("Hips (${lengthUnit.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightInput.toDoubleOrNull()?.let { UnitConverter.toStoredWeightKg(it, weightUnit) }
                    val waist = waistInput.toDoubleOrNull()?.let { UnitConverter.toStoredLengthCm(it, lengthUnit) }
                    val chest = chestInput.toDoubleOrNull()?.let { UnitConverter.toStoredLengthCm(it, lengthUnit) }
                    val hips = hipsInput.toDoubleOrNull()?.let { UnitConverter.toStoredLengthCm(it, lengthUnit) }
                    val arms = armsInput.toDoubleOrNull()?.let { UnitConverter.toStoredLengthCm(it, lengthUnit) }
                    val thighs = thighsInput.toDoubleOrNull()?.let { UnitConverter.toStoredLengthCm(it, lengthUnit) }

                    onConfirm(w, waist, chest, hips, arms, thighs, notesInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("confirm_add_measurement_button")
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
