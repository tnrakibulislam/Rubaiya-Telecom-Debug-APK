package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.BanglaFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class AppRepository(
    private val dao: AppDao,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("rubaiya_telecom_prefs", Context.MODE_PRIVATE)

    fun getStoreName(): String = prefs.getString("store_name", "রুবাইয়া টেলিকম") ?: "রুবাইয়া টেলিকম"
    fun setStoreName(name: String) {
        prefs.edit().putString("store_name", name).apply()
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("is_dark_mode", false)
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    fun getAllCustomersFlow(): Flow<List<CustomerEntity>> = dao.getAllCustomersFlow()

    fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?> = dao.getCustomerByIdFlow(id)

    fun getTransactionsForCustomerFlow(customerId: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsForCustomerFlow(customerId)

    suspend fun addCustomer(
        name: String,
        phone: String,
        address: String = "",
        initialDue: Double = 0.0,
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val engPhone = BanglaFormatter.toEnglishDigits(phone)
        val customer = CustomerEntity(
            name = name.trim(),
            phone = engPhone,
            address = address.trim(),
            initialBalance = initialDue,
            currentBalance = initialDue,
            notes = notes.trim(),
            createdAt = System.currentTimeMillis()
        )
        val customerId = dao.insertCustomer(customer)
        if (initialDue > 0) {
            val initTx = TransactionEntity(
                customerId = customerId,
                type = "DUE",
                amount = initialDue,
                description = "প্রাথমিক বাকি",
                timestamp = System.currentTimeMillis()
            )
            dao.insertTransaction(initTx)
        }
        recalculateCustomerBalance(customerId)
        customerId
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        val engPhone = BanglaFormatter.toEnglishDigits(customer.phone)
        val updated = customer.copy(
            name = customer.name.trim(),
            phone = engPhone,
            address = customer.address.trim(),
            notes = customer.notes.trim()
        )
        dao.updateCustomer(updated)
    }

    suspend fun deleteCustomer(customerId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionsForCustomer(customerId)
        dao.deleteCustomerById(customerId)
    }

    suspend fun addTransaction(
        customerId: Long,
        type: String, // "DUE" or "PAYMENT"
        amount: Double,
        description: String
    ): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            customerId = customerId,
            type = type,
            amount = amount,
            description = description.trim(),
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertTransaction(tx)
        recalculateCustomerBalance(customerId)
        id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.updateTransaction(transaction)
        recalculateCustomerBalance(transaction.customerId)
    }

    suspend fun deleteTransaction(transactionId: Long, customerId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionById(transactionId)
        recalculateCustomerBalance(customerId)
    }

    suspend fun recalculateCustomerBalance(customerId: Long) = withContext(Dispatchers.IO) {
        val customer = dao.getCustomerByIdDirect(customerId) ?: return@withContext
        val txs = dao.getTransactionsForCustomerDirect(customerId)
        
        var balance = 0.0
        for (tx in txs) {
            if (tx.type == "DUE") {
                balance += tx.amount
            } else if (tx.type == "PAYMENT") {
                balance -= tx.amount
            }
        }
        dao.updateCustomer(customer.copy(currentBalance = balance))
    }

    fun getSummaryStatsFlow(): Flow<SummaryStats> {
        val customersFlow = dao.getAllCustomersFlow()
        val transactionsFlow = dao.getAllTransactionsFlow()

        return combine(customersFlow, transactionsFlow) { customers, transactions ->
            val startOfDay = getStartOfDayTimestamp()
            val totalCustomers = customers.size
            val totalDue = customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
            
            val todayTxs = transactions.filter { it.timestamp >= startOfDay }
            val todayPayment = todayTxs.filter { it.type == "PAYMENT" }.sumOf { it.amount }
            val todayDue = todayTxs.filter { it.type == "DUE" }.sumOf { it.amount }

            SummaryStats(
                totalCustomers = totalCustomers,
                totalDue = totalDue,
                todayPayment = todayPayment,
                todayDue = todayDue
            )
        }.flowOn(Dispatchers.IO)
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dao.getAllCustomersDirect()
        if (existing.isEmpty()) {
            val c1 = addCustomer(
                name = "Saim",
                phone = "01570234785",
                address = "উপজেলা বাজার",
                initialDue = 9220.0,
                notes = "নিয়মিত কাস্টমার"
            )
            addTransaction(c1, "DUE", 500.0, "মোবাইল রিচার্জ")
            addTransaction(c1, "PAYMENT", 1000.0, "নগদ জমা")

            val c2 = addCustomer(
                name = "Adnan",
                phone = "01712345678",
                address = "রেলগেট মোড়",
                initialDue = 8720.0,
                notes = ""
            )

            val c3 = addCustomer(
                name = "Mazidul",
                phone = "01811223344",
                address = "নতুন বাজার",
                initialDue = 0.0,
                notes = "সব পরিশোধিত"
            )

            val c4 = addCustomer(
                name = "Babul",
                phone = "01987654321",
                address = "কলেজ রোড",
                initialDue = 1555.0,
                notes = ""
            )
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearAllTransactions()
        dao.clearAllCustomers()
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())
        root.put("storeName", getStoreName())

        val customersList = dao.getAllCustomersDirect()
        val custArray = JSONArray()
        for (c in customersList) {
            val item = JSONObject()
            item.put("id", c.id)
            item.put("name", c.name)
            item.put("phone", c.phone)
            item.put("address", c.address)
            item.put("initialBalance", c.initialBalance)
            item.put("currentBalance", c.currentBalance)
            item.put("notes", c.notes)
            item.put("createdAt", c.createdAt)
            custArray.put(item)
        }
        root.put("customers", custArray)

        val txList = dao.getAllTransactionsDirect()
        val txArray = JSONArray()
        for (t in txList) {
            val item = JSONObject()
            item.put("id", t.id)
            item.put("customerId", t.customerId)
            item.put("type", t.type)
            item.put("amount", t.amount)
            item.put("description", t.description)
            item.put("timestamp", t.timestamp)
            txArray.put(item)
        }
        root.put("transactions", txArray)

        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("customers") && !root.has("transactions") && !root.has("version")) {
                return@withContext false
            }

            if (root.has("storeName")) {
                setStoreName(root.getString("storeName"))
            }

            // Clear current data to prevent duplicate customers and duplicate transactions on restore
            clearAllData()

            val custArray = root.optJSONArray("customers") ?: JSONArray()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()

            // Map old customer id -> newly inserted customer id
            val idMap = mutableMapOf<Long, Long>()

            for (i in 0 until custArray.length()) {
                val item = custArray.getJSONObject(i)
                val oldId = item.optLong("id", -1L)
                val c = CustomerEntity(
                    id = 0, // Auto-generate new primary key
                    name = item.optString("name", "অজ্ঞাত"),
                    phone = item.optString("phone", ""),
                    address = item.optString("address", ""),
                    initialBalance = item.optDouble("initialBalance", 0.0),
                    currentBalance = item.optDouble("currentBalance", 0.0),
                    notes = item.optString("notes", ""),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
                val newId = dao.insertCustomer(c)
                if (oldId != -1L) {
                    idMap[oldId] = newId
                }
            }

            for (i in 0 until txArray.length()) {
                val item = txArray.getJSONObject(i)
                val oldCustId = item.optLong("customerId", -1L)
                val newCustId = idMap[oldCustId] ?: oldCustId
                if (newCustId != -1L) {
                    val t = TransactionEntity(
                        id = 0,
                        customerId = newCustId,
                        type = item.optString("type", "DUE"),
                        amount = item.optDouble("amount", 0.0),
                        description = item.optString("description", ""),
                        timestamp = item.optLong("timestamp", System.currentTimeMillis())
                    )
                    dao.insertTransaction(t)
                }
            }

            // Recalculate balances for imported customers
            for (newCustId in idMap.values) {
                recalculateCustomerBalance(newCustId)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
