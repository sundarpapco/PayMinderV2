package com.example.payminder.worker

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.example.payminder.R

object MessagingIntent {

    const val SMS_SENT_ACTION = "com.sivakasi.papco.payminder.message_sent"
    private const val KEY_CUSTOMER_ID = "customer:id"
    private const val KEY_MOBILE_NUMBER = "mobile:number"

    fun createFrom(context: Context, details: MessagingDetail): PendingIntent {

        val intent = Intent(SMS_SENT_ACTION)
        intent.putExtra(KEY_CUSTOMER_ID, details.customer.id)
        intent.putExtra(KEY_MOBILE_NUMBER, details.mobileNumber)

        return PendingIntent.getBroadcast(
            context,
            details.customer.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun parseToResult(context: Context, intent: Intent, resultCode: Int): MessageSentResult {

        check(intent.action == SMS_SENT_ACTION) { "Invalid intent given for parsing" }

        val customerId = intent.getIntExtra(KEY_CUSTOMER_ID, -1)
        require(customerId != -1) { "Customer Id not found in the intent while parsing" }
        val mobileNumber = intent.getStringExtra(KEY_MOBILE_NUMBER)
            ?: error("mobile number not found in the intent while parsing")
        val failureReason = failureReason(context, resultCode)
        return MessageSentResult(customerId, mobileNumber, failureReason)

    }

    private fun failureReason(context: Context, resultCode: Int): String? {

        return when (resultCode) {

            Activity.RESULT_OK -> null

            SmsManager.RESULT_NETWORK_ERROR ->
                context.getString(R.string.sms_failure_network_error)

            SmsManager.RESULT_ERROR_GENERIC_FAILURE ->
                context.getString(R.string.sms_failure_generic_failure)

            SmsManager.RESULT_ERROR_NO_SERVICE ->
                context.getString(R.string.sms_failure_no_service)

            SmsManager.RESULT_ERROR_RADIO_OFF ->
                context.getString(R.string.sms_failure_radio_off)

            SmsManager.RESULT_RADIO_NOT_AVAILABLE ->
                context.getString(R.string.sms_failure_radio_off)

            SmsManager.RESULT_NETWORK_REJECT ->
                context.getString(R.string.sms_failure_network_rejection)

            else ->
                context.getString(R.string.unknown_error)
        }

    }

}