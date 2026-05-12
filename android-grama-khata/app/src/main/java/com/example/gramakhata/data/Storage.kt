package com.example.gramakhata.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Store::class, Customer::class, Transaction::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gramaDao(): GramaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grama_khata_database"
                )
                .fallbackToDestructiveMigration() // Simple for this example
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class GramaRepository(private val gramaDao: GramaDao) {
    val allStores = gramaDao.getAllStores()

    suspend fun insertStore(store: Store) = gramaDao.insertStore(store)
    suspend fun getStoreById(id: Long) = gramaDao.getStoreById(id)

    fun getCustomersForStore(storeId: Long) = gramaDao.getCustomersForStore(storeId)
    fun getTransactionsForStore(storeId: Long) = gramaDao.getTransactionsForStore(storeId)

    fun getTransactionsForCustomer(customerId: Long) = gramaDao.getTransactionsForCustomer(customerId)

    suspend fun insertCustomer(customer: Customer) = gramaDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = gramaDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) {
        gramaDao.deleteTransactionsForCustomer(customer.id)
        gramaDao.deleteCustomer(customer)
    }

    suspend fun insertTransaction(transaction: Transaction) = gramaDao.insertTransaction(transaction)
}
