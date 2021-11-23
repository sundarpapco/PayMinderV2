package com.example.payminder.worker

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.payminder.PayMinderApp
import com.example.payminder.R
import kotlinx.coroutines.delay

class SendEmailWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {

        private const val WORK_NAME="com.sivakasi.papco.payMinder.sendEmailWork"
        private const val NOTIFICATION_ID = 1

        fun startWith(context: Context){
            val request= OneTimeWorkRequestBuilder<SendEmailWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    private val notificationBuilder =
        NotificationCompat.Builder(context, PayMinderApp.CHANNEL_ID_INTIMATION).apply {
            setContentTitle(applicationContext.getString(R.string.email_intimation))
            setContentText(applicationContext.getString(R.string.sending_email))
            setProgress(0, 100, true)
            setSmallIcon(R.drawable.ic_logo)
            foregroundServiceBehavior = FOREGROUND_SERVICE_IMMEDIATE
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

    override suspend fun doWork(): Result {

        notify(notificationBuilder.build())

        val maxProgress=100
        var currentProgress=0
        repeat(10){
            delay(1000)
            currentProgress+=10
            updateNotification(currentProgress, maxProgress)
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,notificationBuilder.build()
        )
    }

    private fun updateNotification(currentProgress: Int, maxProgress: Int) {
        val notification= if (currentProgress >= maxProgress) {
            notificationBuilder.setContentText(applicationContext.getString(R.string.email_sent_successfully))
                .setProgress(0, 0, false)
                .build()
        } else {
            notificationBuilder.setProgress(maxProgress, currentProgress, false).build()
        }

        notify(notification)
    }

    private fun notify(notification:Notification){
        NotificationManagerCompat.from(applicationContext).apply {
            notify(NOTIFICATION_ID,notification)
        }
    }

    private suspend fun sendMail(){
        delay(1000)
    }

}