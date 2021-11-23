package com.example.payminder.database

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.payminder.database.entities.Customer
import com.example.payminder.database.entities.LoadDetails
import com.example.payminder.util.ExcelFileParser.ParsedData

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

    fun getAllCustomers(): LiveData<List<Customer>> =
        db.customersDao().getCustomers()

    fun getInvoicesForCustomers(customerId: Int) =
        db.invoicesDao().getInvoicesForCustomer(customerId)

    fun getLoadDetail():LiveData<LoadDetails> =
        db.detailDao().getLoadDetail()
}