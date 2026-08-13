package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Store
import com.example.ui.components.ChangePinDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.theme.CrimsonRedPrimary

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val storeName by viewModel.storeName.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    var showStoreNameDialog by remember { mutableStateOf(false) }
    var newStoreNameInput by remember { mutableStateOf(storeName) }

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }

    var showResetAllConfirm by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    if (showChangePinDialog) {
        ChangePinDialog(
            pinManager = viewModel.pinManager,
            onDismiss = { showChangePinDialog = false },
            onSuccess = { showChangePinDialog = false }
        )
    }

    // Store Name Dialog
    if (showStoreNameDialog) {
        AlertDialog(
            onDismissRequest = { showStoreNameDialog = false },
            title = {
                Text(
                    text = "দোকানের নাম পরিবর্তন",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                OutlinedTextField(
                    value = newStoreNameInput,
                    onValueChange = { newStoreNameInput = it },
                    label = { Text("দোকানের নাম") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_name_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStoreName(newStoreNameInput)
                        showStoreNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                    modifier = Modifier.testTag("save_store_name_button")
                ) {
                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStoreNameDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Backup Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Text(
                    text = "ডাটা ব্যাকআপ (JSON)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "নিচের ব্যাকআপ কোডটি কপি করে নিরাপদ স্থানে সংরক্ষণ করুন। পরে যেকোনো সময় রিস্টোর করতে পারবেন:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Backup JSON", backupJsonText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "ব্যাকআপ কোড ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কপি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = "ডাটা রিস্টোর",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "পূর্বে ব্যাকআপ করা JSON কোডটি নিচে পেস্ট করুন:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("JSON কোড পেস্ট করুন...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("restore_json_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank()) {
                            viewModel.importBackup(restoreJsonInput) {
                                showRestoreDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                    modifier = Modifier.testTag("execute_restore_button")
                ) {
                    Text("রিস্টোর করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Reset Confirmation
    if (showResetAllConfirm) {
        DeleteConfirmDialog(
            title = "সব ডাটা মুছে ফেলুন",
            message = "আপনি কি নিশ্চিত যে সকল কাস্টমার এবং তাদের লেনদেনের হিসাব স্থায়ীভাবে মুছে ফেলতে চান? এটি আর ফিরিয়ে আনা যাবে না!",
            confirmButtonText = "হ্যাঁ, সব মুছে ফেলুন",
            onConfirm = {
                showResetAllConfirm = false
                viewModel.clearAllData()
            },
            onDismiss = { showResetAllConfirm = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrimsonRedPrimary)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "অ্যাপ সেটিংস",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 22.sp
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Store Name Option
            SettingOptionCard(
                icon = Icons.Default.Store,
                title = "দোকানের নাম",
                subtitle = storeName,
                onClick = {
                    newStoreNameInput = storeName
                    showStoreNameDialog = true
                },
                testTag = "setting_store_name"
            )

            // 2. Dark Mode Toggle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setting_dark_mode"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = CrimsonRedPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "ডার্ক মোড (Dark Theme)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isDarkMode) "চালু আছে" else "বন্ধ আছে",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CrimsonRedPrimary)
                    )
                }
            }

            // 3. App PIN Lock
            SettingOptionCard(
                icon = Icons.Default.Lock,
                title = "App PIN Lock (সুরক্ষা)",
                subtitle = if (viewModel.pinManager.isPinSet()) "PIN পরিবর্তন বা বন্ধ করুন (চালু আছে)" else "PIN লক সেট করুন (বন্ধ আছে)",
                onClick = { showChangePinDialog = true },
                testTag = "setting_pin_lock"
            )

            // 3. Backup Data
            SettingOptionCard(
                icon = Icons.Default.Backup,
                title = "ডাটা ব্যাকআপ (Export)",
                subtitle = "কাস্টমার ও লেনদেনের কোড কপি করুন",
                onClick = {
                    viewModel.exportBackup { json ->
                        if (json != null) {
                            backupJsonText = json
                            showBackupDialog = true
                        }
                    }
                },
                testTag = "setting_backup_data"
            )

            // 4. Restore Data
            SettingOptionCard(
                icon = Icons.Default.Restore,
                title = "ডাটা রিস্টোর (Import)",
                subtitle = "JSON কোড দিয়ে ডাটা পুনরুদ্ধার করুন",
                onClick = {
                    restoreJsonInput = ""
                    showRestoreDialog = true
                },
                testTag = "setting_restore_data"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Clear All Data (Danger)
            Card(
                onClick = { showResetAllConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setting_clear_data"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = CrimsonRedPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "সব ডাটা মুছে ফেলুন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CrimsonRedPrimary
                            )
                        )
                        Text(
                            text = "সকল কাস্টমার এবং লেনদেনের তথ্য স্থায়ীভাবে মুছে ফেলা হবে",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CrimsonRedPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}
