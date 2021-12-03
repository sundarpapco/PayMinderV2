package com.example.payminder.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.payminder.database.entities.Customer

@Dao
interface CustomersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCustomer(customer:Customer)

    @Query("select * from customers order by name asc")
    fun getAllCustomersLiveData():LiveData<List<Customer>>

    @Query("delete from customers")
    suspend fun clearTable()

    @Query("select * from customers where id=:customerId")
    suspend fun getCustomer(customerId:Int):Customer

    @Query("select * from customers order by name asc")
    suspend fun getAllCustomers():List<Customer>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCustomer(customer: Customer)

    @Query("update customers set smsSent='false'")
    suspend fun resetMessageSendingStatus()

    @Query("update customers set emailSent='false'")
    suspend fun resetEmailSendingStatus()

}