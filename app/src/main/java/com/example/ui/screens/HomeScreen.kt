package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerEntity
import com.example.ui.CustomerFilter
import com.example.ui.CustomerSort
import com.example.ui.MainViewModel
import com.example.ui.components.CustomerCard
import com.example.ui.components.SummaryCard
import com.example.ui.theme.CrimsonRedPrimary

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onCustomerClick: (CustomerEntity) -> Unit,
    onAddCustomerClick: () -> Unit
) {
    val storeName by viewModel.storeName.collectAsState()
    val stats by viewModel.summaryStats.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val sortType by viewModel.sortType.collectAsState()

    var isSearchOpen by remember { mutableStateOf(false) }
    var isSortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomerClick,
                containerColor = CrimsonRedPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "নতুন কাস্টমার",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Section
            SurfaceHeader(
                storeName = storeName,
                isSearchOpen = isSearchOpen,
                onSearchToggle = {
                    isSearchOpen = !isSearchOpen
                    if (!isSearchOpen) viewModel.searchQuery.value = ""
                },
                onAddCustomerClick = onAddCustomerClick
            )

            // Search Bar Input if Open
            if (isSearchOpen) {
                PaddingValues(horizontal = 16.dp, vertical = 8.dp).let {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("কাস্টমারের নাম বা ফোন নম্বর দিয়ে খুঁজুন...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
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
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("search_input_field"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonRedPrimary
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Card
                item {
                    SummaryCard(stats = stats)
                }

                // Filter & Sort Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = filterType == CustomerFilter.ALL,
                                onClick = { viewModel.filterType.value = CustomerFilter.ALL },
                                label = { Text("সব") },
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
                                label = { Text("বাকি নেই") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonRedPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Sort Button & Dropdown
                        Box {
                            IconButton(onClick = { isSortMenuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = CrimsonRedPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = isSortMenuOpen,
                                onDismissRequest = { isSortMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("সর্বোচ্চ বাকি আগে") },
                                    onClick = {
                                        viewModel.sortType.value = CustomerSort.DUE_DESC
                                        isSortMenuOpen = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("সর্বনিম্ন বাকি আগে") },
                                    onClick = {
                                        viewModel.sortType.value = CustomerSort.DUE_ASC
                                        isSortMenuOpen = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("নাম অনুযায়ী (অ-আ/A-Z)") },
                                    onClick = {
                                        viewModel.sortType.value = CustomerSort.NAME_ASC
                                        isSortMenuOpen = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("সর্বশেষ যোগ করা") },
                                    onClick = {
                                        viewModel.sortType.value = CustomerSort.NEWEST
                                        isSortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Customer Items List
                if (customers.isEmpty()) {
                    item {
                        EmptyCustomerState(onAddCustomerClick = onAddCustomerClick)
                    }
                } else {
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
}

@Composable
private fun SurfaceHeader(
    storeName: String,
    isSearchOpen: Boolean,
    onSearchToggle: () -> Unit,
    onAddCustomerClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrimsonRedPrimary)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = storeName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = "সহজে রাখুন বাকির হিসাব",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
            }

            Row {
                IconButton(
                    onClick = onSearchToggle,
                    modifier = Modifier.testTag("header_search_button")
                ) {
                    Icon(
                        imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onAddCustomerClick,
                    modifier = Modifier.testTag("header_add_customer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Customer",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCustomerState(onAddCustomerClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = CrimsonRedPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "এখনো কোনো কাস্টমার যোগ করা হয়নি",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "নিচের বাটনে চাপ দিয়ে প্রথম কাস্টমার যোগ করুন এবং বাকির হিসাব রাখা শুরু করুন।",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddCustomerClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
            modifier = Modifier.testTag("empty_add_customer_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "নতুন কাস্টমার যোগ করুন",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
