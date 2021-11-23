package com.example.payminder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.payminder.database.dao.CustomersDao
import com.example.payminder.database.dao.DetailsDao
import com.example.payminder.database.dao.InvoicesDao
import com.example.payminder.database.entities.Customer
import com.example.payminder.database.entities.Invoice
import com.example.payminder.database.entities.LoadDetails

@Database(
    entities = [Customer::class, Invoice::class,LoadDetails::class],
    version = 1
)
abstract class MasterDatabase:RoomDatabase() {

    companion object{
        const val DB_NAME = "master.db"
        private var INSTANCE: MasterDatabase? = null

        fun getInstance(context: Context): MasterDatabase {

            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    MasterDatabase::class.java,
                    DB_NAME
                ).build()
            }

            return INSTANCE!!
        }
    }

    abstract fun customersDao():CustomersDao

    abstract fun invoicesDao():InvoicesDao

    abstract fun detailDao():DetailsDao

}