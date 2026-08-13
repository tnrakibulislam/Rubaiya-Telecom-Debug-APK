package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.CustomerEntity
import com.example.data.SummaryStats
import com.example.data.TransactionEntity
import com.example.util.BanglaFormatter
import com.example.util.PinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CustomerFilter {
    ALL, DUE_ONLY, ZERO_ONLY
}

enum class CustomerSort {
    DUE_DESC, DUE_ASC, NAME_ASC, NEWEST
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow(CustomerFilter.ALL)
    val sortType = MutableStateFlow(CustomerSort.DUE_DESC)

    val selectedCustomerId = MutableStateFlow<Long?>(null)

    val storeName = MutableStateFlow("রুবাইয়া টেলিকম")
    val isDarkMode = MutableStateFlow(false)

    val pinManager: PinManager by lazy { PinManager(application) }
    val isAppUnlocked = MutableStateFlow(false)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db.appDao(), application)
        storeName.value = repository.getStoreName()
        isDarkMode.value = repository.isDarkMode()
        isAppUnlocked.value = !pinManager.isPinSet()

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val summaryStats: StateFlow<SummaryStats> = repository.getSummaryStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryStats())

    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        repository.getAllCustomersFlow(),
        searchQuery,
        filterType,
        sortType
    ) { rawCustomers, query, filter, sort ->
        var list = rawCustomers

        // Search Filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            val engQ = BanglaFormatter.toEnglishDigits(q)
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.phone.contains(engQ)
            }
        }

        // Filter Type
        list = when (filter) {
            CustomerFilter.ALL -> list
            CustomerFilter.DUE_ONLY -> list.filter { it.currentBalance > 0 }
            CustomerFilter.ZERO_ONLY -> list.filter { it.currentBalance <= 0 }
        }

        // Sort Type
        when (sort) {
            CustomerSort.DUE_DESC -> list.sortedByDescending { it.currentBalance }
            CustomerSort.DUE_ASC -> list.sortedBy { it.currentBalance }
            CustomerSort.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            CustomerSort.NEWEST -> list.sortedByDescending { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCustomer: StateFlow<CustomerEntity?> = selectedCustomerId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getCustomerByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedCustomerTransactions: StateFlow<List<TransactionEntity>> = selectedCustomerId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getTransactionsForCustomerFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCustomer(id: Long?) {
        selectedCustomerId.value = id
    }

    fun updateStoreName(newName: String) {
        if (newName.isNotBlank()) {
            storeName.value = newName.trim()
            repository.setStoreName(newName.trim())
            Toast.makeText(getApplication(), "দোকানের নাম পরিবর্তন করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        repository.setDarkMode(enabled)
    }

    fun addCustomer(
        name: String,
        phone: String,
        address: String,
        initialDueStr: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank()) {
            Toast.makeText(getApplication(), "দয়া করে কাস্টমারের নাম লিখুন", Toast.LENGTH_SHORT).show()
            return
        }
        val engPhone = BanglaFormatter.toEnglishDigits(phone.trim())
        if (phone.isNotBlank() && !BanglaFormatter.isValidBdPhone(engPhone)) {
            Toast.makeText(getApplication(), "সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন (যেমন: 017XXXXXXXX)", Toast.LENGTH_SHORT).show()
            return
        }

        val initialDue = BanglaFormatter.toEnglishDigits(initialDueStr).toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            repository.addCustomer(
                name = name,
                phone = engPhone,
                address = address,
                initialDue = initialDue,
                notes = notes
            )
            Toast.makeText(getApplication(), "নতুন কাস্টমার সফলভাবে যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    fun updateCustomer(
        customer: CustomerEntity,
        name: String,
        phone: String,
        address: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank()) {
            Toast.makeText(getApplication(), "দয়া করে কাস্টমারের নাম লিখুন", Toast.LENGTH_SHORT).show()
            return
        }
        val engPhone = BanglaFormatter.toEnglishDigits(phone.trim())

        viewModelScope.launch {
            repository.updateCustomer(
                customer.copy(
                    name = name,
                    phone = engPhone,
                    address = address,
                    notes = notes
                )
            )
            Toast.makeText(getApplication(), "কাস্টমারের তথ্য আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    fun deleteCustomer(customerId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            selectedCustomerId.value = null
            Toast.makeText(getApplication(), "কাস্টমার মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    fun addTransaction(
        customerId: Long,
        type: String, // "DUE" or "PAYMENT"
        amountStr: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        val engAmount = BanglaFormatter.toEnglishDigits(amountStr).toDoubleOrNull() ?: 0.0
        if (engAmount <= 0) {
            Toast.makeText(getApplication(), "দয়া করে সঠিক টাকার পরিমাণ দিন", Toast.LENGTH_SHORT).show()
            return
        }

        val desc = description.ifBlank { if (type == "DUE") "বাকি" else "জমা" }

        viewModelScope.launch {
            repository.addTransaction(
                customerId = customerId,
                type = type,
                amount = engAmount,
                description = desc
            )
            val msg = if (type == "DUE") "বাকি যোগ করা হয়েছে" else "টাকা জমা সংরক্ষণ করা হয়েছে"
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    fun updateTransaction(
        transaction: TransactionEntity,
        amountStr: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        val engAmount = BanglaFormatter.toEnglishDigits(amountStr).toDoubleOrNull() ?: 0.0
        if (engAmount <= 0) {
            Toast.makeText(getApplication(), "দয়া করে সঠিক টাকার পরিমাণ দিন", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            repository.updateTransaction(
                transaction.copy(
                    amount = engAmount,
                    description = description.ifBlank { transaction.description }
                )
            )
            Toast.makeText(getApplication(), "লেনদেন আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    fun deleteTransaction(transactionId: Long, customerId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId, customerId)
            Toast.makeText(getApplication(), "লেনদেনটি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupJson()
                onResult(json)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "ব্যাকআপ তৈরিতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
        }
    }

    fun importBackup(jsonString: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            if (success) {
                storeName.value = repository.getStoreName()
                Toast.makeText(getApplication(), "ডাটা সফলভাবে রিস্টোর করা হয়েছে", Toast.LENGTH_SHORT).show()
                onSuccess()
            } else {
                Toast.makeText(getApplication(), "ডাটা রিস্টোর ব্যর্থ হয়েছে! সঠিক JSON ফাইল নির্বাচন করুন।", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            selectedCustomerId.value = null
            Toast.makeText(getApplication(), "সকল ডাটা মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    fun unlockApp() {
        isAppUnlocked.value = true
    }

    fun lockApp() {
        if (pinManager.isPinSet()) {
            isAppUnlocked.value = false
        }
    }

    fun resetDataAndPin() {
        viewModelScope.launch {
            repository.clearAllData()
            pinManager.clearPin()
            selectedCustomerId.value = null
            isAppUnlocked.value = true
            Toast.makeText(getApplication(), "সকল ডাটা ও PIN সফলভাবে রিসেট করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }
}
