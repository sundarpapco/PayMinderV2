package com.example.payminder.worker

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.payminder.MainActivity
import com.example.payminder.PayMinderApp
import com.example.payminder.R
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.database.entities.Customer
import com.example.payminder.util.getMessage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes

@Suppress("BlockingMethodInNonBlockingContext")
class SendEmailWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {

        private const val INPUT_DATA_CUSTOMER_ID = "customer:id"
        private const val INPUT_DATA_FORCE_SEND="force:send:email"
        private const val NOTIFICATION_ID_PROGRESS = 1
        private const val NOTIFICATION_ID_FAILURE = 2
        private const val PENDING_INTENT_REQUEST_CODE = 1

        fun startWith(context: Context, customerId: Int = -1,forceSend:Boolean=false) {
            val request = OneTimeWorkRequestBuilder<SendEmailWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf(
                    INPUT_DATA_CUSTOMER_ID to customerId,
                    INPUT_DATA_FORCE_SEND to forceSend
                ))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IntimationWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
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
            setContentIntent(createNotificationContentIntent())

            val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(id)
            addAction(R.drawable.ic_close, context.getString(R.string.cancel), cancelIntent)
        }

    override suspend fun doWork(): Result {

        clearFailureNotificationIfAny()

        if (!isInternetConnected()) {
            postFailureNotification(applicationContext.getString(R.string.check_internet_connection))
            return Result.success()
        }

        setForeground(getForegroundInfo())
        notify(notificationBuilder.build())

        val customerList = getCustomerList()
        val gmailService = createGmailService()

        if (customerList.isEmpty())
            return Result.success()

        val maxProgress = customerList.size

        for ((index, customer) in customerList.withIndex()) {
            if (isStopped)
                break
            updateNotification(index + 1, maxProgress)
            try {
                sendMail(gmailService, customer)
            } catch (e: Exception) {
                e.printStackTrace()
                postFailureNotification(e.getMessage(applicationContext))
                return Result.failure()
            }
            //sendFakeEmail(gmailService,customer)
        }

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
            R.string.sending_status,
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

    //Creates PendingIntent which will open the app when clicked on the set notification
    private fun createNotificationContentIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            applicationContext,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun clearFailureNotificationIfAny() {
        val service =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.cancel(NOTIFICATION_ID_FAILURE)
    }

    private fun postFailureNotification(reason: String) {
        val notification =
            NotificationCompat.Builder(applicationContext, PayMinderApp.CHANNEL_ID_INTIMATION)
                .apply {
                    setContentTitle(applicationContext.getString(R.string.email_intimation_failed))
                    setContentText(reason)
                    setSmallIcon(R.drawable.ic_logo)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setContentIntent(createNotificationContentIntent())
                    setAutoCancel(true)
                }.build()

        notify(notification, NOTIFICATION_ID_FAILURE)
    }


    private fun notify(notification: Notification, id: Int = NOTIFICATION_ID_PROGRESS) {
        NotificationManagerCompat.from(applicationContext).apply {
            notify(id, notification)
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


    /*This function will return the list of customers to send Emails

    Case 1:If the user has not provided any customerId as Input (in which case it will be -1),
    then the whole list of customers will be retrieved. Then, the customers who has email addresses
    but still the email was not sent is filtered and returned as a list.

    Case 2: If the user has provided a specific customer Id, then it means the user needs to send
    Email to a customer even though the email has already been sent. So, in that case that user will
    be fetched from the database. Then a check will be made to confirm he is having a valid email Id.
    If so, the emailSent status is reset to false in the database (Necessary. Else the mail sending
    loop will skip this customer) and then that customer will bereturned as a list.
     */
    private suspend fun getCustomerList(): List<Customer> {

        val customerId = inputData.getInt(INPUT_DATA_CUSTOMER_ID, -1)

        return if (customerId == -1) {
            if(forceSend())
                repository.resetEmailSendingDetail()
            repository.getAllCustomers().filter { it.hasEmailAddress() && !it.emailSent }
        } else {
            val customer = repository.getCustomer(customerId)
            return if (!customer.hasEmailAddress()) {
                emptyList()
            } else {
                if (customer.emailSent)
                    repository.updateCustomer(customer.apply { emailSent = false })
                listOf(customer)
            }
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

    private fun isInternetConnected(): Boolean =
        (applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        }

    private fun forceSend():Boolean =
        inputData.getBoolean(INPUT_DATA_FORCE_SEND,false)
}