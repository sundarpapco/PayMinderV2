package com.example.payminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class PayMinderApp : Application() {

    companion object{
        const val CHANNEL_ID_INTIMATION="payMinder:intimation::channel"
    }

    override fun onCreate() {
        super.onCreate()

        //Create the Notification channel so that we can display notifications in the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            CHANNEL_ID_INTIMATION,
            applicationContext.getString(R.string.channel_name_intimation),
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}