package com.example.payminder.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "details")
class LoadDetails{
    @PrimaryKey(autoGenerate = false)
    var id:Int=1
    var period:String=""
}