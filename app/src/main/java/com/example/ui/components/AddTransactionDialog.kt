package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.TransactionEntity
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.SuccessGreen
import com.example.util.BanglaFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    type: String, // "DUE" or "PAYMENT"
    currentCustomerDue: Double,
    existingTransaction: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onSave: (amount: String, description: String) -> Unit
) {
    var amount by remember {
        mutableStateOf(
            existingTransaction?.amount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }
    var showOverpaymentPrompt by remember { mutableStateOf(false) }

    val isDue = type == "DUE"
    val primaryColor = if (isDue) CrimsonRedPrimary else SuccessGreen
    val titleText = if (existingTransaction != null) {
        if (isDue) "বাকি পরিবর্তন করুন" else "জমা পরিবর্তন করুন"
    } else {
        if (isDue) "বাকি যোগ করুন" else "টাকা জমা নিন"
    }

    val quickChips = if (isDue) {
        listOf("মোবাইল রিচার্জ", "পণ্য", "সিম", "চার্জার", "অন্যান্য")
    } else {
        listOf("নগদ", "বিকাশ/নগদ", "অন্যান্য")
    }

    if (showOverpaymentPrompt) {
        val enteredAmt = BanglaFormatter.toEnglishDigits(amount).toDoubleOrNull() ?: 0.0
        val extraAmt = enteredAmt - currentCustomerDue

        AlertDialog(
            onDismissRequest = { showOverpaymentPrompt = false },
            title = {
                Text(
                    text = "অতিরিক্ত জমা নিশ্চিতকরণ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "আপনার বর্তমান বাকি ${BanglaFormatter.formatCurrency(currentCustomerDue)}। আপনি ${BanglaFormatter.formatCurrency(enteredAmt)} জমা দিতে চেয়েছেন।\n\nঅতিরিক্ত ${BanglaFormatter.formatCurrency(extraAmt)} অগ্রিম/ফেরত হিসেবে রাখতে চান?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverpaymentPrompt = false
                        onSave(amount, description)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("হ্যাঁ, জমা রাখুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverpaymentPrompt = false }) {
                    Text("সংশোধন করুন")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("টাকার পরিমাণ *") },
                    prefix = { Text("৳ ") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Chips
                Text(
                    text = "দ্রুত বিবরণ:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickChips.forEach { chip ->
                        FilterChip(
                            selected = description == chip,
                            onClick = { description = chip },
                            label = { Text(chip) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                                selectedLabelColor = primaryColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description Custom Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ফ্ল্যাক্সিলোড / নগদ") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_desc_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        val enteredAmt = BanglaFormatter.toEnglishDigits(amount).toDoubleOrNull() ?: 0.0
                        if (!isDue && currentCustomerDue > 0 && enteredAmt > currentCustomerDue) {
                            showOverpaymentPrompt = true
                        } else {
                            onSave(amount, description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = if (isDue) "বাকি যোগ করুন" else "জমা সংরক্ষণ করুন",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "বাতিল",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
