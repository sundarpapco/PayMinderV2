package com.example.payminder.worker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager

object IntimationWorker{

    const val WORK_NAME = "com.sivakasi.papco.payMinder.intimationWork"

    fun getWorkStatusLiveData(context: Context): LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(WORK_NAME)
    }
}