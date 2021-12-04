package com.example.payminder.worker

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.payminder.MainActivity
import com.example.payminder.PayMinderApp
import com.example.payminder.R
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.database.entities.Customer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
@Suppress("DEPRECATION")
class SendMessageWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {

        private const val INPUT_DATA_CUSTOMER_ID = "customer:id"
        private const val INPUT_DATA_FORCE_SEND = "force:send:message"
        private const val NOTIFICATION_ID_PROGRESS = 1
        private const val NOTIFICATION_ID_FAILURE = 2
        private const val PENDING_INTENT_REQUEST_CODE = 1

        fun startWith(context: Context, customerId: Int = -1, forceSend: Boolean = false) {
            val request = OneTimeWorkRequestBuilder<SendMessageWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(
                    workDataOf(
                        INPUT_DATA_CUSTOMER_ID to customerId,
                        INPUT_DATA_FORCE_SEND to forceSend
                    )
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IntimationWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    private val repository = Repository(MasterDatabase.getInstance(applicationContext))

    private val notificationBuilder =
        NotificationCompat.Builder(context, PayMinderApp.CHANNEL_ID_INTIMATION).apply {
            setContentTitle(applicationContext.getString(R.string.sending_sms_intimation))
            setProgress(0, 100, true)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                setSmallIcon(R.drawable.ic_logo_png)
            else
                setSmallIcon(R.drawable.ic_logo)
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(createNotificationContentIntent())

            val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(id)
            addAction(R.drawable.ic_close, context.getString(R.string.cancel), cancelIntent)
        }

    private val senderName: String =
        GoogleSignIn.getLastSignedInAccount(applicationContext)?.let{
            it.displayName ?: ""
        } ?: error("Cannot send messages without signing in")

    private val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        applicationContext.getSystemService(SmsManager::class.java)
    else
        SmsManager.getDefault()

    private var currentProgress = 0
    private var maxProgress = 0


    override suspend fun doWork(): Result {

        clearFailureNotificationIfAny()

        setForeground(getForegroundInfo())
        notify(notificationBuilder.build())

        val customerList = getCustomerList()

        if (customerList.isEmpty())
            return Result.success()

        return sendTextMessages(customerList)
    }


    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID_PROGRESS, notificationBuilder.build()
        )
    }

    private suspend fun sendTextMessages(customers: List<Customer>): Result {

        maxProgress = customers.size
        try {
            messageSentResultFlow(applicationContext)
                .zip(messageSendingFlow(customers)) { result, detail ->
                    check(result.isSuccess) { result.failureReason!! }
                    detail
                }.collect {
                    if (it.isThisLastMobileNumber())
                        repository.updateCustomer(it.customer.apply { smsSent = true })
                }
        } catch (e: Exception) {
            e.printStackTrace()
            postFailureNotification(e.message!!)
            return Result.failure()
        }

        return Result.success()
    }

    private fun sendTextMessage(details: MessagingDetail) {
        val intent = MessagingIntent.createFrom(applicationContext, details)
        val message = details.message(applicationContext)

        if (message.length < 160) {
            smsManager.sendTextMessage(details.mobileNumber, null, message, intent, null)
        } else {
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(
                details.mobileNumber,
                null,
                parts,
                arrayListOf(intent),
                null
            )
        }
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
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                        setSmallIcon(R.drawable.ic_logo_png)
                    else
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


    /*This function will return the list of customers to send Emails

    Case 1:If the user has not provided any customerId as Input (in which case it will be -1),
    then the whole list of customers will be retrieved. Then, the customers who has mobile numbers and
    overdue amount but still the email was not sent is filtered and returned as a list.

    Case 2: If the user has provided a specific customer Id, then it means the user needs to send
    Message to a customer even though the message has already been sent. So, in that case that user will
    be fetched from the database. Then a check will be made to confirm he is having a valid mobile number
    and also he is having overdue amount.If so, the smsSent status is reset to false in the
    database (Necessary. Else the Message sending loop will skip this customer) and then that
    customer will be returned as a list.
     */
    private suspend fun getCustomerList(): List<Customer> {

        val customerId = inputData.getInt(INPUT_DATA_CUSTOMER_ID, -1)

        return if (customerId == -1) {
            if (forceSend())
                repository.resetMessageSendingDetail()
            repository.getAllCustomers()
                .filter { it.hasMobileNumber() && !it.smsSent && it.overdueAmount > 0.0 }
        } else {
            val customer = repository.getCustomer(customerId)
            return if (!customer.hasMobileNumber() || customer.overdueAmount == 0.0) {
                emptyList()
            } else {
                if (customer.smsSent)
                    repository.updateCustomer(customer.apply { smsSent = false })
                listOf(customer)
            }
        }

    }

    private fun messageSendingFlow(customers: List<Customer>): Flow<MessagingDetail> =
        customers.asFlow()
            .flatMapConcat { messagingDetailsOfCustomer(it) }
            .onEach {
                if (it.isThisFirstMobileNumber()) {
                    currentProgress++
                    updateNotification(currentProgress, maxProgress)
                }
                sendTextMessage(it)
            }


    private fun messagingDetailsOfCustomer(customer: Customer): Flow<MessagingDetail> =
        customer.mobileNumbers().asFlow()
            .map { MessagingDetail(customer, it,senderName) }


    private fun messageSentResultFlow(context: Context) = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == MessagingIntent.SMS_SENT_ACTION)
                    trySend(MessagingIntent.parseToResult(context, intent, resultCode))
            }
        }

        context.registerReceiver(receiver, IntentFilter(MessagingIntent.SMS_SENT_ACTION))
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private fun forceSend(): Boolean =
        inputData.getBoolean(INPUT_DATA_FORCE_SEND, false)

}




