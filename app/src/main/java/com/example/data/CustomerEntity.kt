package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
