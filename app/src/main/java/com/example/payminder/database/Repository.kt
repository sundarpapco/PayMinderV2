package com.example.payminder.database

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.payminder.database.entities.Customer
import com.example.payminder.database.entities.LoadDetails
import com.example.payminder.util.ExcelFileParser.ParsedData
import kotlinx.coroutines.flow.Flow

class Repository(
    private val db: MasterDatabase
) {

    private suspend fun clearDataBase() = db.withTransaction {
        db.customersDao().clearTable()
        db.invoicesDao().clearTable()
        db.detailDao().clearLoadDetail()
    }

    suspend fun saveParsedData(parsedData: ParsedData) =
        db.withTransaction {
            clearDataBase()
            db.detailDao().saveLoadDetail(parsedData.detail)
            parsedData.data.forEach { (customer, invoices) ->
                db.customersDao().addCustomer(customer)
                db.invoicesDao().addInvoices(invoices)
            }
        }


    fun getAllCustomersLiveData(): LiveData<List<Customer>> =
        db.customersDao().getAllCustomersLiveData()

    fun getAllCustomersFlow(): Flow<List<Customer>> =
        db.customersDao().getAllCustomersFlow()

    suspend fun getAllCustomers():List<Customer> =
        db.customersDao().getAllCustomers()

    suspend fun getCustomer(customerId: Int): Customer =
        db.customersDao().getCustomer(customerId)

    fun getInvoicesForCustomerLiveData(customerId: Int) =
        db.invoicesDao().getInvoicesForCustomerLiveData(customerId)

    suspend fun getInvoicesForCustomer(customerId: Int) =
        db.invoicesDao().getInvoicesForCustomer(customerId)

    fun getLoadDetailLiveData():LiveData<LoadDetails> =
        db.detailDao().getLoadDetailLiveData()

    suspend fun getLoadDetail():LoadDetails =
        db.detailDao().getLoadDetail()

    suspend fun updateCustomer(customer:Customer)=
        db.customersDao().updateCustomer(customer)

    suspend fun resetEmailSendingDetail(){
        db.customersDao().resetEmailSendingStatus()
    }

    suspend fun resetMessageSendingDetail(){
        db.customersDao().resetMessageSendingStatus()
    }
}