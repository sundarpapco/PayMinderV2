package com.example.payminder.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.payminder.util.rupeeFormatString

@Entity(
    tableName = "invoices"
)
class Invoice(
    @PrimaryKey(autoGenerate = true)
    var id:Int=0,
    var customerId:Int=0,
    var number:String="",
    var date:String="",
    var amount:Double=0.00,
    var overdueByDays:Int=0,
    var daysSinceInvoiced:Int=0
){
    @Ignore
    val amountRupees:String = amount.rupeeFormatString(true)
}