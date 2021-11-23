package com.example.payminder.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.payminder.database.entities.Invoice

@Dao
interface InvoicesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addInvoices(invoices:List<Invoice>)

    @Query("select * from invoices where customerId=:customerId order by overdueByDays desc")
    fun getInvoicesForCustomer(customerId:Int):LiveData<List<Invoice>>

    @Query("delete from invoices")
    suspend fun clearTable()

}