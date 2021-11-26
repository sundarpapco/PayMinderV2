package com.example.payminder.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.payminder.database.entities.LoadDetails

@Dao
interface DetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLoadDetail(detail:LoadDetails)

    @Query("select * from details where id=1")
    fun getLoadDetailLiveData():LiveData<LoadDetails>

    @Query("select * from details where id=1")
    suspend fun getLoadDetail():LoadDetails

    @Query("delete from details")
    suspend fun clearLoadDetail()

}