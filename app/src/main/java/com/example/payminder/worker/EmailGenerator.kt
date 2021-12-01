package com.example.payminder.worker

import android.content.Context
import com.example.payminder.R
import com.example.payminder.database.entities.Customer
import com.example.payminder.database.entities.Invoice
import com.example.payminder.database.entities.LoadDetails
import com.example.payminder.util.rupeeFormatString
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.util.Base64
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailGenerator(
    private val context: Context,
    private val customer: Customer,
    private val invoices: List<Invoice>,
    private val loadDetails: LoadDetails,
    private val sendingCompanyName: String
) {

    fun generateGmailMessage(): Message {
        return enCodeMimeMessageToGmailMessage(createMimeMessage())
    }

    private fun createMimeMessage(): MimeMessage {

        require(customer.hasEmailAddress()) {
            "Cannot generate email for customer who doesn't have any email address"
        }

        var companyName = ""
        val fromEmail = GoogleSignIn.getLastSignedInAccount(context)?.let {
            companyName = it.displayName ?: ""
            "${it.displayName} <${it.email}>"
        } ?: error("Cannot send Email without signing in to google")

        val subject = context.getString(R.string.email_subject, companyName)

        val session = Session.getDefaultInstance(Properties())
        val mimeMessage = MimeMessage(session)
        mimeMessage.setFrom(InternetAddress(fromEmail))
        mimeMessage.subject = subject

        customer.emailAddresses()
            .filter {
                it.isNotBlank()
            }.forEach {
                mimeMessage.addRecipient(
                    javax.mail.Message.RecipientType.TO,
                    InternetAddress("${customer.name} <$it>")
                )
            }

        val multipart = MimeMultipart()
        val htmlPart = MimeBodyPart()
        htmlPart.setContent(
            generateHtmlMessageBody(),
            "text/html"
        )
        multipart.addBodyPart(htmlPart)
        mimeMessage.setContent(multipart)
        return mimeMessage

    }

    private fun enCodeMimeMessageToGmailMessage(mimeMessage: MimeMessage): Message {

        val buffer = ByteArrayOutputStream()
        mimeMessage.writeTo(buffer)
        val bytes = buffer.toByteArray()
        val encodedEmail = Base64.encodeBase64URLSafeString(bytes)
        val message = Message()
        message.raw = encodedEmail
        return message

    }

    private fun generateHtmlMessageBody(): String {

        val tableDetails = generateHtmlTableDetails()
        return context.getString(
            R.string.email_template,
            "${customer.name}, ${customer.city}",
            loadDetails.period,
            tableDetails,
            sendingCompanyName
        )
    }

    private fun generateHtmlTableDetails(): String {

        var totalAmount = 0.0
        val tableDetails = StringBuilder()
        val htmlRupeeSymbol = "&#8377;"
        var amountString:String=""
        invoices.forEach {
            totalAmount += it.amount
            amountString=it.amount.rupeeFormatString(false)
            with(tableDetails) {
                append("<tr>")
                append("<td>${it.number}</td>")
                append("<td>${it.date}</td>")
                append("<td nowrap=\"nowrap\" style=\"text-align: right;\">$htmlRupeeSymbol ${amountString}</td>")
                append("<td>${it.overdueByDays}</td>")
                append("</tr>")
            }
        }

        //Add the Total row as last header row
        with(tableDetails) {
            append("<tr>")
            append("<th></th>")
            append("<th>${context.getString(R.string.total)}</th>")
            append("<th nowrap=\"nowrap\" style=\"text-align: right;\">$htmlRupeeSymbol ${amountString}</th>")
            append("<th></th>")

        }

        return tableDetails.toString()

    }


}