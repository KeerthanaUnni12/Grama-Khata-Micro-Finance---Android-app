package com.example.gramakhata.data

import androidx.room.*

@Entity(tableName = "stores")
data class Store(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopName: String,
    val ownerName: String,
    val pin: String
)

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = Store::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeId: Long,
    val name: String,
    val phone: String,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val type: String, // "CREDIT" or "PAYMENT"
    val timestamp: Long = System.currentTimeMillis()
)
