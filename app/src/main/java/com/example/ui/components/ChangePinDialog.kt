package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonRedPrimary
import com.example.util.BanglaFormatter
import com.example.util.PinManager

@Composable
fun ChangePinDialog(
    pinManager: PinManager,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isPinSet = remember { pinManager.isPinSet() }

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleSavePin() {
        val engOld = BanglaFormatter.toEnglishDigits(oldPin.trim())
        val engNew = BanglaFormatter.toEnglishDigits(newPin.trim())
        val engConfirm = BanglaFormatter.toEnglishDigits(confirmPin.trim())

        if (isPinSet) {
            if (!pinManager.verifyPin(engOld)) {
                errorMessage = "পুরাতন PIN সঠিক নয়!"
                return
            }
        }

        if (engNew.length != 4 || !engNew.all { it.isDigit() }) {
            errorMessage = "নতুন PIN অবশ্যই ৪ ডিজিটের হতে হবে!"
            return
        }

        if (engNew != engConfirm) {
            errorMessage = "নতুন দুটি PIN মিলছে না!"
            return
        }

        pinManager.setPin(engNew)
        Toast.makeText(context, "PIN সফলভাবে পরিবর্তন করা হয়েছে", Toast.LENGTH_SHORT).show()
        onSuccess()
    }

    fun handleDisablePin() {
        val engOld = BanglaFormatter.toEnglishDigits(oldPin.trim())
        if (!pinManager.verifyPin(engOld)) {
            errorMessage = "পুরাতন PIN সঠিক নয়!"
            return
        }
        pinManager.clearPin()
        Toast.makeText(context, "PIN লক নিষ্ক্রিয় করা হয়েছে", Toast.LENGTH_SHORT).show()
        onSuccess()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = CrimsonRedPrimary
            )
        },
        title = {
            Text(
                text = if (isPinSet) "PIN পরিবর্তন করুন" else "PIN সেট করুন",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isPinSet) {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { if (it.length <= 4) oldPin = it },
                        label = { Text("পুরাতন PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("old_pin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text("নতুন ৪ ডিজিটের PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_pin_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4) confirmPin = it },
                    label = { Text("নতুন PIN নিশ্চিত করুন") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_pin_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = CrimsonRedPrimary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { handleSavePin() },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_pin_button")
                ) {
                    Text("PIN পরিবর্তন নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                }

                if (isPinSet) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { handleDisablePin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("disable_pin_button")
                    ) {
                        Text("PIN লক নিষ্ক্রিয় করুন", color = CrimsonRedPrimary)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_pin_button")
            ) {
                Text("বাতিল")
            }
        }
    )
}
