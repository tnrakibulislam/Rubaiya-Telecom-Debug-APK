package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import com.example.ui.components.AddCustomerDialog
import com.example.ui.screens.CustomerDetailsScreen
import com.example.ui.screens.CustomerListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CrimsonRedPrimary

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()
    val storeName by viewModel.storeName.collectAsState()
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.lockApp()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!isAppUnlocked || !viewModel.pinManager.isPinSet()) {
        PinLockScreen(
            pinManager = viewModel.pinManager,
            storeName = storeName,
            onUnlocked = { viewModel.unlockApp() },
            onResetDataAndPin = { viewModel.resetDataAndPin() }
        )
        return
    }

    // Intercept hardware or software back button if on CustomerDetails
    if (selectedCustomer != null) {
        BackHandler {
            viewModel.selectCustomer(null)
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, address, initialDue, notes ->
                viewModel.addCustomer(name, phone, address, initialDue, notes) {
                    showAddCustomerDialog = false
                }
            }
        )
    }

    if (selectedCustomer != null) {
        CustomerDetailsScreen(
            viewModel = viewModel,
            onBackClick = { viewModel.selectCustomer(null) }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    // Tab 0: Home
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = {
                            Text(
                                text = "হোম",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "হোম"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonRedPrimary,
                            selectedTextColor = CrimsonRedPrimary,
                            indicatorColor = CrimsonRedPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // Tab 1: Customers
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = {
                            Text(
                                text = "কাস্টমার",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.People else Icons.Outlined.People,
                                contentDescription = "কাস্টমার"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonRedPrimary,
                            selectedTextColor = CrimsonRedPrimary,
                            indicatorColor = CrimsonRedPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_customers")
                    )

                    // Tab 2: Settings
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = {
                            Text(
                                text = "সেটিংস",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "সেটিংস"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonRedPrimary,
                            selectedTextColor = CrimsonRedPrimary,
                            indicatorColor = CrimsonRedPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onCustomerClick = { customer -> viewModel.selectCustomer(customer.id) },
                        onAddCustomerClick = { showAddCustomerDialog = true }
                    )
                    1 -> CustomerListScreen(
                        viewModel = viewModel,
                        onCustomerClick = { customer -> viewModel.selectCustomer(customer.id) },
                        onAddCustomerClick = { showAddCustomerDialog = true }
                    )
                    2 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
