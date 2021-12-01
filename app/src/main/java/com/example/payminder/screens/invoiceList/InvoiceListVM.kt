package com.example.payminder.screens.invoiceList

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.database.entities.Invoice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InvoiceListVM(application: Application) : AndroidViewModel(application) {

    private var alreadyLoaded: Boolean = false
    private val context = getApplication<Application>()
    private val repository = Repository(MasterDatabase.getInstance(context))

    private val _customerId: MutableLiveData<Int> = MutableLiveData()
    val invoiceList: LiveData<List<Invoice>> = Transformations.switchMap(_customerId) {
        repository.getInvoicesForCustomerLiveData(it)
    }

    fun loadInvoiceOfCustomer(customerId: Int) {

        if (alreadyLoaded)
            return
        else
            alreadyLoaded = true

        _customerId.value = customerId
    }



}
