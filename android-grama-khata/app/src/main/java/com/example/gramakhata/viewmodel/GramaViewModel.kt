package com.example.gramakhata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramakhata.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GramaViewModel(private val repository: GramaRepository) : ViewModel() {

    val allStores = repository.allStores

    private val _currentStoreId = MutableStateFlow<Long?>(null)
    val currentStoreId: StateFlow<Long?> = _currentStoreId

    val currentStore = _currentStoreId.flatMapLatest { id ->
        if (id != null) flow { emit(repository.getStoreById(id)) }
        else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val customers = _currentStoreId.flatMapLatest { id ->
        if (id != null) repository.getCustomersForStore(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val transactions = _currentStoreId.flatMapLatest { id ->
        if (id != null) repository.getTransactionsForStore(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectStore(storeId: Long) {
        _currentStoreId.value = storeId
    }

    fun createStore(shopName: String, ownerName: String, pin: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertStore(Store(shopName = shopName, ownerName = ownerName, pin = pin))
            onComplete(id)
        }
    }

    fun getCustomerBalance(customerId: Long, transactions: List<Transaction>): Double {
        return transactions.filter { it.customerId == customerId }.sumOf { 
            if (it.type == "CREDIT") it.amount else -it.amount 
        }
    }

    fun insertCustomer(name: String, phone: String, photoUri: String? = null) {
        val storeId = _currentStoreId.value ?: return
        viewModelScope.launch {
            repository.insertCustomer(Customer(storeId = storeId, name = name, phone = phone, photoUri = photoUri))
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun addTransaction(customerId: Long, amount: Double, type: String) {
        viewModelScope.launch {
            repository.insertTransaction(Transaction(customerId = customerId, amount = amount, type = type))
        }
    }
}
