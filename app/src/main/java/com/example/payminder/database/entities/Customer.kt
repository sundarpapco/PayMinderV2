package com.example.payminder.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.payminder.util.rupeeFormatString

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = false)
    var id:Int=0,
    var name:String="",
    var city:String="",
    var mobile1:String="",
    var mobile2:String="",
    var email1:String="",
    var email2:String="",
    var email3:String="",
    var totalOutStanding:Double=0.00,
    var overdueAmount:Double=0.00,
    var emailSent:Boolean=false,
    var smsSent:Boolean=false
){

    //Binding fields
    //These fields are here for making it easy while binding this class to recyclerView list.
    //These were provided as fields and not methods for the reason that, Room will create instances
    // of this class in background thread. So, all these fields will be initialized in the background
    // thread avoiding unnecessary calculations in main thread while binding and also to cache the value
    // to avoid recalculating on every rebinding while scrolling list

    @Ignore
    val totalOutStandingRupees=totalOutStanding.rupeeFormatString(true)

    @Ignore
    val overdueRupees=overdueAmount.rupeeFormatString(true)

    fun hasMobileNumber():Boolean=
        mobile1.isNotBlank() || mobile2.isNotBlank()

    fun hasEmailAddress():Boolean=
        email1.isNotBlank() || email2.isNotBlank() || email3.isNotBlank()
}