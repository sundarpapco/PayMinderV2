package com.example.payminder.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.payminder.database.entities.Customer

@Dao
interface CustomersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCustomer(customer:Customer)

    @Query("select * from customers order by name asc")
    fun getCustomers():LiveData<List<Customer>>

    @Query("delete from customers")
    suspend fun clearTable()


}