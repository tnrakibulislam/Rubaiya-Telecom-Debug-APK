package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerEntity
import com.example.ui.CustomerFilter
import com.example.ui.MainViewModel
import com.example.ui.components.CustomerCard
import com.example.ui.theme.CrimsonRedPrimary
import com.example.util.BanglaFormatter

@Composable
fun CustomerListScreen(
    viewModel: MainViewModel,
    onCustomerClick: (CustomerEntity) -> Unit,
    onAddCustomerClick: () -> Unit
) {
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Title & Add Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrimsonRedPrimary)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "কাস্টমার তালিকা",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "মোট: ${BanglaFormatter.toBanglaDigits(customers.size.toString())} জন",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
            }

            Button(
                onClick = onAddCustomerClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = CrimsonRedPrimary
                )
                Text(
                    text = " নতুন",
                    color = CrimsonRedPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("নাম বা ফোন নম্বর দিয়ে খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("customer_list_search_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrimsonRedPrimary
            )
        )

        // Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == CustomerFilter.ALL,
                onClick = { viewModel.filterType.value = CustomerFilter.ALL },
                label = { Text("সব কাস্টমার") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrimsonRedPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = filterType == CustomerFilter.DUE_ONLY,
                onClick = { viewModel.filterType.value = CustomerFilter.DUE_ONLY },
                label = { Text("বাকি আছে") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrimsonRedPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = filterType == CustomerFilter.ZERO_ONLY,
                onClick = { viewModel.filterType.value = CustomerFilter.ZERO_ONLY },
                label = { Text("বাকি ০ টাকা") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrimsonRedPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        // Customer Cards
        if (customers.isEmpty()) {
            EmptyCustomerState(onAddCustomerClick = onAddCustomerClick)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = customers,
                    key = { it.id }
                ) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { onCustomerClick(customer) }
                    )
                }
            }
        }
    }
}
