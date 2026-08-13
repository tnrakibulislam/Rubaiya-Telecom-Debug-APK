package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerEntity
import com.example.data.TransactionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.RedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.util.BanglaFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val customer by viewModel.selectedCustomer.collectAsState()
    val transactions by viewModel.selectedCustomerTransactions.collectAsState()

    var showAddTxType by remember { mutableStateOf<String?>(null) } // "DUE" or "PAYMENT"
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showDeleteCustomerConfirm by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    if (customer == null) {
        onBackClick()
        return
    }

    val cust = customer!!

    // Dialogs
    if (showEditCustomerDialog) {
        AddCustomerDialog(
            existingCustomer = cust,
            onDismiss = { showEditCustomerDialog = false },
            onSave = { name, phone, address, _, notes ->
                viewModel.updateCustomer(cust, name, phone, address, notes) {
                    showEditCustomerDialog = false
                }
            }
        )
    }

    if (showDeleteCustomerConfirm) {
        DeleteConfirmDialog(
            title = "কাস্টমার মুছে ফেলুন",
            message = "আপনি কি নিশ্চিত যে \"${cust.name}\" এবং তার সকল লেনদেনের হিসাব মুছে ফেলতে চান?",
            onConfirm = {
                showDeleteCustomerConfirm = false
                viewModel.deleteCustomer(cust.id) {
                    onBackClick()
                }
            },
            onDismiss = { showDeleteCustomerConfirm = false }
        )
    }

    if (showAddTxType != null) {
        AddTransactionDialog(
            type = showAddTxType!!,
            currentCustomerDue = cust.currentBalance,
            onDismiss = { showAddTxType = null },
            onSave = { amountStr, desc ->
                viewModel.addTransaction(cust.id, showAddTxType!!, amountStr, desc) {
                    showAddTxType = null
                }
            }
        )
    }

    if (txToEdit != null) {
        AddTransactionDialog(
            type = txToEdit!!.type,
            currentCustomerDue = cust.currentBalance,
            existingTransaction = txToEdit,
            onDismiss = { txToEdit = null },
            onSave = { amountStr, desc ->
                val targetTx = txToEdit!!
                txToEdit = null
                viewModel.updateTransaction(targetTx, amountStr, desc) {
                    // Success
                }
            }
        )
    }

    if (txToDelete != null) {
        DeleteConfirmDialog(
            title = "লেনদেন মুছে ফেলুন",
            message = "আপনি কি এই লেনদেনটি (${if (txToDelete!!.type == "DUE") "বাকি +" else "জমা -"}${BanglaFormatter.formatCurrency(txToDelete!!.amount)}) মুছে ফেলতে চান?",
            onConfirm = {
                val tId = txToDelete!!.id
                txToDelete = null
                viewModel.deleteTransaction(tId, cust.id)
            },
            onDismiss = { txToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = cust.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        if (cust.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = BanglaFormatter.toBanglaDigits(cust.phone),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditCustomerDialog = true },
                        modifier = Modifier.testTag("edit_customer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Customer",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { showDeleteCustomerConfirm = true },
                        modifier = Modifier.testTag("delete_customer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Customer",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CrimsonRedPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Info Details Card
                if (cust.address.isNotBlank() || cust.notes.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                if (cust.address.isNotBlank()) {
                                    Text(
                                        text = "ঠিকানা: ${cust.address}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                if (cust.notes.isNotBlank()) {
                                    if (cust.address.isNotBlank()) Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "নোট: ${cust.notes}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Balance Card
                item {
                    BigBalanceCard(due = cust.currentBalance)
                }

                // Action Buttons Row: ➕ বাকি যোগ করুন | 💰 টাকা জমা
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Add Due Button
                        Button(
                            onClick = { showAddTxType = "DUE" },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("add_due_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "বাকি যোগ করুন",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        // Add Payment Button
                        Button(
                            onClick = { showAddTxType = "PAYMENT" },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("receive_payment_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "টাকা জমা",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }

                // Transaction History Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = CrimsonRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "লেনদেনের ইতিহাস (${BanglaFormatter.toBanglaDigits(transactions.size.toString())})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Transaction History Items
                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "এখনো কোনো লেনদেন নেই",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                } else {
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { tx ->
                        TransactionItemCard(
                            transaction = tx,
                            onEditClick = { txToEdit = tx },
                            onDeleteClick = { txToDelete = tx }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BigBalanceCard(due: Double) {
    val isDuePositive = due > 0.01
    val isDueZero = due >= -0.01 && due <= 0.01

    val cardBg = when {
        isDuePositive -> RedContainer
        isDueZero -> SuccessGreenContainer
        else -> Color(0xFFE3F2FD)
    }

    val textColor = when {
        isDuePositive -> CrimsonRedPrimary
        isDueZero -> SuccessGreen
        else -> Color(0xFF1565C0)
    }

    val labelText = when {
        isDuePositive -> "বর্তমান বাকি"
        isDueZero -> "বর্তমান বাকি (পরিশোধিত)"
        else -> "অগ্রিম / অতিরিক্ত জমা"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("big_balance_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = labelText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.85f)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = BanglaFormatter.formatCurrency(if (due < 0) -due else due),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    fontSize = 32.sp
                )
            )
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDue = transaction.type == "DUE"
    val badgeBg = if (isDue) RedContainer else SuccessGreenContainer
    val badgeColor = if (isDue) CrimsonRedPrimary else SuccessGreen
    val amountPrefix = if (isDue) "বাকি +" else "জমা -"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Type Icon Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDue) "+" else "-",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Description & Time
                Column {
                    Text(
                        text = transaction.description.ifBlank { if (isDue) "বাকি" else "জমা" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = BanglaFormatter.formatFullDateTime(transaction.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Amount & Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amountPrefix${BanglaFormatter.formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("edit_tx_button_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_tx_button_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
