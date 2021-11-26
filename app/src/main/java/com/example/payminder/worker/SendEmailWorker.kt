package com.example.payminder.worker

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.work.*
import com.example.payminder.MainActivity
import com.example.payminder.PayMinderApp
import com.example.payminder.R
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.database.entities.Customer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import java.lang.Exception

@Suppress("BlockingMethodInNonBlockingContext")
class SendEmailWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {

        private const val INPUT_DATA_CUSTOMER_ID = "customer:id"
        private const val WORK_NAME = "com.sivakasi.papco.payMinder.sendEmailWork"
        private const val NOTIFICATION_ID_PROGRESS = 1

        fun startWith(context: Context, customerId: Int = -1) {
            val request = OneTimeWorkRequestBuilder<SendEmailWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf(INPUT_DATA_CUSTOMER_ID to customerId))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }

        fun getWorkStatusLiveData(context: Context): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(WORK_NAME)
        }
    }

    private val repository = Repository(MasterDatabase.getInstance(applicationContext))
    private val loggedInAccount: GoogleSignInAccount =
        GoogleSignIn.getLastSignedInAccount(applicationContext)
            ?: error("Cannot send email without signing in")

    private val notificationBuilder =
        NotificationCompat.Builder(context, PayMinderApp.CHANNEL_ID_INTIMATION).apply {
            setContentTitle(applicationContext.getString(R.string.sending_email_intimation))
            setProgress(0, 100, true)
            setSmallIcon(R.drawable.ic_logo)
            foregroundServiceBehavior = FOREGROUND_SERVICE_IMMEDIATE
            priority = NotificationCompat.PRIORITY_DEFAULT

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val contentIntent = PendingIntent.getActivity(
                applicationContext,
                2,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            setContentIntent(contentIntent)

            val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(id)
            addAction(R.drawable.ic_close, context.getString(R.string.cancel), cancelIntent)
        }

    override suspend fun doWork(): Result {

        setForeground(getForegroundInfo())
        notify(notificationBuilder.build())
        val customerList = getCustomerList()
        val gmailService = createGmailService()

        if (customerList.isEmpty())
            return Result.success()

        val maxProgress = customerList.size

        try {
            for ((index, customer) in customerList.withIndex()) {

                if (isStopped)
                    break
                updateNotification(index + 1, maxProgress)
                sendMail(gmailService, customer)
            }
        } catch (e: Exception) {
            Log.d("SUNDAR","Sending Email Failed")
            e.printStackTrace()
            return Result.failure()
        }

        Log.d("SUNDAR","Sent Emails Successfully")
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID_PROGRESS, notificationBuilder.build()
        )
    }

    private fun updateNotification(currentProgress: Int, maxProgress: Int) {

        require(currentProgress <= maxProgress) { "Invalid progress detected" }

        val contentText = applicationContext.getString(
            R.string.sending_email_status,
            currentProgress,
            maxProgress
        )

        val notification =
            notificationBuilder.apply {
                setProgress(maxProgress - 1, currentProgress - 1, false).build()
                setContentText(contentText)
            }.build()

        notify(notification)

    }


    private fun notify(notification: Notification) {
        NotificationManagerCompat.from(applicationContext).apply {
            notify(NOTIFICATION_ID_PROGRESS, notification)
        }
    }

    private suspend fun sendMail(gmail: Gmail, customer: Customer) {

        if (!customer.hasEmailAddress())
            return

        val invoices = repository.getInvoicesForCustomer(customer.id)
        val loadDetails = repository.getLoadDetail()
        val generator = EmailGenerator(
            applicationContext,
            customer,
            invoices,
            loadDetails,
            loggedInAccount.displayName ?: ""
        )
        gmail.users().Messages().send(loggedInAccount.id, generator.generateGmailMessage())
            .execute()

        customer.emailSent = true
        repository.updateCustomer(customer)
    }


    private suspend fun getCustomerList(): List<Customer> {

        val customerId = inputData.getInt(INPUT_DATA_CUSTOMER_ID, -1)

        return if (customerId == -1) {
            repository.getAllCustomers().filter { it.hasEmailAddress() && !it.emailSent }
        } else {
            val customer = repository.getCustomer(customerId)
            listOf(customer).filter { it.hasEmailAddress() && !it.emailSent }
        }

    }

    private fun createGmailService(): Gmail {

        //Create the credentials and Gmail service
        val scopes = listOf(GmailScopes.GMAIL_SEND)
        val jacksonFactory = JacksonFactory()
        val httpTransport = NetHttpTransport()

        val credentials =
            GoogleAccountCredential.usingOAuth2(applicationContext, scopes)
        credentials.backOff = ExponentialBackOff()
        credentials.selectedAccountName = loggedInAccount.email

        val builder = Gmail.Builder(httpTransport, jacksonFactory, credentials)
        builder.applicationName = applicationContext.getString(R.string.app_name)
        return builder.build()
    }

}