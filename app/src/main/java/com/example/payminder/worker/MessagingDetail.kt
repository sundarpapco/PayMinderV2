package com.example.payminder.worker

import android.content.Context
import com.example.payminder.R
import com.example.payminder.database.entities.Customer

/*

Just a holder class which contains information about a message which has to be sent.

@Params
customer - Customer Object to whom this message should be sent. Also, this field has all the data necessary to generate
the text message to send to this customer.

mobileNumber - A particular mobile number of this customer to send the text message. Since the customer
can have many mobile numbers as a list, this field indicates the exact mobile number to which we should
send the message

 */
data class MessagingDetail(
    val customer: Customer,
    val mobileNumber: String,
    val senderName:String
) {

    //Will return true if this message is sent to the last mobile number of this customer mobile number list
    fun isThisLastMobileNumber(): Boolean {
        return with(customer.mobileNumbers()) {
            if (isEmpty())
                true
            else
                last() == mobileNumber
        }
    }

    fun isThisFirstMobileNumber(): Boolean {
        return with(customer.mobileNumbers()) {
            if (isEmpty())
                true
            else
                first() == mobileNumber
        }
    }

    //returns the message which should be sent as the message
    fun message(context:Context): String {
        return context.getString(
            R.string.message_template,
            senderName,
            customer.name,
            customer.overdueRupees
        )
    }
}