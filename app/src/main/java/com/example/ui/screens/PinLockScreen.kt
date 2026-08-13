package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRedPrimary
import com.example.util.BanglaFormatter
import com.example.util.PinManager

@Composable
fun PinLockScreen(
    pinManager: PinManager,
    storeName: String,
    onUnlocked: () -> Unit,
    onResetDataAndPin: () -> Unit
) {
    val isPinAlreadySet = remember { pinManager.isPinSet() }

    // States for first time setup
    var setupStage by remember { mutableStateOf(if (isPinAlreadySet) 0 else 1) } // 0 = Unlock Mode, 1 = Setup First, 2 = Setup Confirm
    var firstEnteredPin by remember { mutableStateOf("") }

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }

    val currentTitle = when (setupStage) {
        1 -> "নতুন PIN সেট করুন"
        2 -> "PIN আবার লিখুন"
        else -> storeName
    }

    val currentSubtitle = when (setupStage) {
        1 -> "অ্যাপ সুরক্ষায় ৪ ডিজিটের PIN বানান"
        2 -> "নিশ্চিত করতে একই PIN আবার টাইপ করুন"
        else -> "অ্যাপে প্রবেশ করতে ৪ ডিজিটের PIN দিন"
    }

    fun handleKeyPress(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                when (setupStage) {
                    1 -> {
                        firstEnteredPin = newPin
                        enteredPin = ""
                        setupStage = 2
                    }
                    2 -> {
                        if (newPin == firstEnteredPin) {
                            pinManager.setPin(newPin)
                            onUnlocked()
                        } else {
                            errorMessage = "PIN মিলেনি! আবার চেষ্টা করুন।"
                            enteredPin = ""
                            firstEnteredPin = ""
                            setupStage = 1
                        }
                    }
                    0 -> {
                        if (pinManager.verifyPin(newPin)) {
                            onUnlocked()
                        } else {
                            errorMessage = "ভুল PIN। আবার চেষ্টা করুন।"
                            enteredPin = ""
                        }
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CrimsonRedPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "PIN ভুলে গেছেন?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CrimsonRedPrimary
                    )
                )
            },
            text = {
                Text(
                    text = "নিরাপত্তার স্বার্থে PIN বাইপাস করা সম্ভব নয়। PIN রিসেট করলে অ্যাপের সকল কাস্টমার ও লেনদেনের তথ্য স্থায়ীভাবে মুছে যাবে। আপনি কি নিশ্চিত?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotDialog = false
                        pinManager.clearPin()
                        onResetDataAndPin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                    modifier = Modifier.testTag("reset_pin_data_confirm_button")
                ) {
                    Text("হ্যাঁ, সব ডাটা রিসেট করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pin_lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header & PIN Status Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                // Lock Badge Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CrimsonRedPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = CrimsonRedPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = currentTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CrimsonRedPrimary,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN Dots Indicator [ • • • • ]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) CrimsonRedPrimary else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) CrimsonRedPrimary else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Prompt
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CrimsonRedPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Numeric Keypad 0-9
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(72.dp))
                            } else if (key == "DEL") {
                                KeypadButton(
                                    onClick = { handleBackspace() },
                                    testTag = "keypad_btn_del"
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                KeypadButton(
                                    onClick = { handleKeyPress(key) },
                                    testTag = "keypad_btn_$key"
                                ) {
                                    Text(
                                        text = BanglaFormatter.toBanglaDigits(key),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Forgot PIN option (Only when unlocking)
                if (setupStage == 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showForgotDialog = true },
                        modifier = Modifier.testTag("forgot_pin_button")
                    ) {
                        Text(
                            text = "PIN ভুলে গেছেন?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CrimsonRedPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .testTag(testTag),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
