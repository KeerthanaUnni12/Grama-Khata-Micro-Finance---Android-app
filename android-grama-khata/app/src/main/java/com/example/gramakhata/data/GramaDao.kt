package com.example.gramakhata.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GramaDao {
    // Store Queries
    @Query("SELECT * FROM stores ORDER BY shopName ASC")
    fun getAllStores(): Flow<List<Store>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store): Long

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getStoreById(id: Long): Store?

    // Customer Queries
    @Query("SELECT * FROM customers WHERE storeId = :storeId ORDER BY name ASC")
    fun getCustomersForStore(storeId: Long): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Transaction Queries
    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<Transaction>>

    @Query("SELECT t.* FROM transactions t INNER JOIN customers c ON t.customerId = c.id WHERE c.storeId = :storeId ORDER BY t.timestamp DESC")
    fun getTransactionsForStore(storeId: Long): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransactions(transactions: List<Transaction>)
    
    @Query("DELETE FROM transactions WHERE customerId = :customerId")
    suspend fun deleteTransactionsForCustomer(customerId: Long)
}
