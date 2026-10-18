package com.example.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ThemeMode
import com.example.data.local.entity.WeightUnit
import com.example.ui.components.SectionHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pendingExportType by remember { mutableStateOf<String?>(null) } // "JSON", "CSV_WORKOUTS", "CSV_MEASUREMENTS"

    // Document Picker for Exporting
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && pendingExportType != null) {
            coroutineScope.launch {
                val content = when (pendingExportType) {
                    "JSON" -> viewModel.getJsonBackupString()
                    "CSV_WORKOUTS" -> viewModel.getWorkoutsCsvString()
                    "CSV_MEASUREMENTS" -> viewModel.getMeasurementsCsvString()
                    else -> ""
                }
                viewModel.writeTextToUri(
                    context = context,
                    uri = uri,
                    content = content,
                    onSuccess = {
                        Toast.makeText(context, "Export saved successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { msg ->
                        Toast.makeText(context, "Export error: $msg", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    // Document Picker for Importing
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.readBackupFromUri(context, uri)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Units Section
            item {
                SectionHeader(title = "Units of Measurement")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Weight & Distance Units",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = state.preferences.weightUnit == WeightUnit.KG,
                                onClick = { viewModel.setWeightUnit(WeightUnit.KG) },
                                label = { Text("Metric (kg, cm, km)") },
                                modifier = Modifier.weight(1f).testTag("unit_metric_chip")
                            )
                            FilterChip(
                                selected = state.preferences.weightUnit == WeightUnit.LBS,
                                onClick = { viewModel.setWeightUnit(WeightUnit.LBS) },
                                label = { Text("Imperial (lbs, in, mi)") },
                                modifier = Modifier.weight(1f).testTag("unit_imperial_chip")
                            )
                        }
                    }
                }
            }

            // Theme Appearance
            item {
                SectionHeader(title = "Appearance")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.values().forEach { mode ->
                                FilterChip(
                                    selected = state.preferences.themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.weight(1f).testTag("theme_${mode.name.lowercase()}_chip")
                                )
                            }
                        }
                    }
                }
            }

            // Rest Timer Preferences
            item {
                SectionHeader(title = "Rest Timer Defaults")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Default Rest Duration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 60, 90, 120, 180).forEach { sec ->
                                FilterChip(
                                    selected = state.preferences.defaultRestSeconds == sec,
                                    onClick = { viewModel.setDefaultRestSeconds(sec) },
                                    label = { Text("${sec}s") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("Vibrate on Timer End", fontWeight = FontWeight.SemiBold)
                                    Text("Gentle pulse when rest ends", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = state.preferences.vibrateOnTimerEnd,
                                onCheckedChange = { viewModel.setVibrate(it) },
                                modifier = Modifier.testTag("vibrate_timer_switch")
                            )
                        }
                    }
                }
            }

            // Data Backup & Export
            item {
                SectionHeader(title = "Data Management & Backup")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Export & Import",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Your fitness data is stored locally. Export regular backups to safeguard your workout logs, custom exercises, and measurements.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Export JSON
                        Button(
                            onClick = {
                                pendingExportType = "JSON"
                                exportLauncher.launch("offline_fitness_backup_${System.currentTimeMillis()}.json")
                            },
                            modifier = Modifier.fillMaxWidth().testTag("export_json_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Complete Backup (JSON)")
                        }

                        // Export CSV Workouts
                        OutlinedButton(
                            onClick = {
                                pendingExportType = "CSV_WORKOUTS"
                                exportLauncher.launch("workout_history_${System.currentTimeMillis()}.csv")
                            },
                            modifier = Modifier.fillMaxWidth().testTag("export_workouts_csv_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Export Workouts (CSV)")
                        }

                        // Export CSV Measurements
                        OutlinedButton(
                            onClick = {
                                pendingExportType = "CSV_MEASUREMENTS"
                                exportLauncher.launch("body_measurements_${System.currentTimeMillis()}.csv")
                            },
                            modifier = Modifier.fillMaxWidth().testTag("export_measurements_csv_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Export Measurements (CSV)")
                        }

                        // Import Backup
                        Button(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            modifier = Modifier.fillMaxWidth().testTag("import_backup_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Backup File", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Privacy & Audit
            item {
                SectionHeader(title = "Privacy & Verification")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openPrivacyDialog() }
                        .testTag("privacy_audit_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Offline Guarantee & Privacy Policy", fontWeight = FontWeight.SemiBold)
                                Text("100% on-device • Zero internet permission", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Danger Zone: Clear Data
            item {
                SectionHeader(title = "Danger Zone")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Erase All User Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Permanently removes all saved workout sessions, custom routines, and body measurements. Built-in exercises and templates will remain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { viewModel.openClearDataDialog() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("clear_data_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wipe User Data")
                        }
                    }
                }
            }

            // App Version Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Offline Fitness Tracker v1.0",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Native Android • Room Database • Jetpack Compose",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // Import Preview Dialog
    if (state.isImportPreviewDialogOpen && state.importPreview != null) {
        val preview = state.importPreview!!
        var clearExistingData by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissImportPreviewDialog() },
            title = {
                Text(
                    text = if (preview.valid) "Backup Preview" else "Invalid Backup File",
                    fontWeight = FontWeight.Bold,
                    color = if (preview.valid) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            },
            text = {
                if (!preview.valid) {
                    Text("Could not parse this file: ${preview.errorMessage ?: "Incompatible format"}")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Schema Version: ${preview.schemaVersion}")
                        Text("Found in file:")
                        Text("• Completed Workouts: ${preview.workoutsCount}")
                        Text("• Custom Routines: ${preview.routinesCount}")
                        Text("• Custom Exercises: ${preview.customExercisesCount}")
                        Text("• Body Measurements: ${preview.measurementsCount}")

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(
                                checked = clearExistingData,
                                onCheckedChange = { clearExistingData = it }
                            )
                            Text("Replace all existing data with this backup", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                if (preview.valid) {
                    Button(
                        onClick = {
                            viewModel.confirmRestore(clearExistingData) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("confirm_restore_button")
                    ) {
                        Text("Confirm Restore", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissImportPreviewDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (state.isClearDataDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearDataDialog() },
            title = { Text("Permanently Erase All Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This action cannot be undone. All workout logs, personal records, and measurements will be permanently deleted from your device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmClearAllData {
                            Toast.makeText(context, "User records erased.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_wipe_button")
                ) {
                    Text("Permanently Erase")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearDataDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Privacy & Permissions Dialog
    if (state.isPrivacyDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPrivacyDialog() },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Privacy & Security Audit", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "100% Offline Architecture",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• Zero Internet Permission: The app does NOT request or declare android.permission.INTERNET. It cannot make network or HTTP requests, and cannot access the internet.\n\n" +
                                "• Completely Local Database: 100% of your workouts, routines, exercise catalog, personal records, and measurements are stored on-device in a local SQLite database (offline_fitness.db).\n\n" +
                                "• No Third-Party Cloud SDKs: All Firebase, Google Play services, telemetry, analytics, and advertising libraries have been completely removed.\n\n" +
                                "• Zero Account & Zero Tracking: No login, no account creation, no cookies, and no device fingerprinting. Your fitness data never leaves your device.\n\n" +
                                "• User-Controlled Backups: Export and import standard JSON and CSV files on demand using Android's native system file picker directly to your local storage or SD card.\n\n" +
                                "• Single System Permission: Only android.permission.VIBRATE is used to provide haptic feedback when your rest timer finishes."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissPrivacyDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Got It", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
