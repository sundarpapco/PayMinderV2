package com.example.payminder.screens.customerInfoScreen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.database.entities.Customer
import com.example.payminder.screens.CustomerInfoScreen
import kotlinx.coroutines.launch

class CustomerInfoVM(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    val customerId = savedStateHandle.toRoute<CustomerInfoScreen>().customerId
    var customer: Customer? by mutableStateOf(null)
    private val repository = Repository(MasterDatabase.getInstance(application.applicationContext))
    private var loaded = false

    init {
        loadCustomer(customerId)
    }

    private fun loadCustomer(customerId: Int) {

        if (loaded)
            return
        else
            loaded = true

        viewModelScope.launch {
            customer = repository.getCustomer(customerId)
        }
    }

}