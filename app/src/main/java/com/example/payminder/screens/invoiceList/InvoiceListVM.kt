package com.example.payminder.screens.invoiceList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.database.entities.Invoice

class InvoiceListVM(application: Application) : AndroidViewModel(application) {

    private var alreadyLoaded: Boolean = false
    private val context = getApplication<Application>()
    private val repository = Repository(MasterDatabase.getInstance(context))

    private val _customerId: MutableLiveData<Int> = MutableLiveData()
    val invoiceList: LiveData<List<Invoice>> = Transformations.switchMap(_customerId) {
        repository.getInvoicesForCustomers(it)
    }

    fun loadInvoiceOfCustomer(customerId: Int) {

        if (alreadyLoaded)
            return
        else
            alreadyLoaded = true

        _customerId.value = customerId
    }

}