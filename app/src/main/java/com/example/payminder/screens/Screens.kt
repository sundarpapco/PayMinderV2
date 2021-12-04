package com.example.payminder.screens

import java.net.URLEncoder
import java.nio.charset.StandardCharsets


sealed class Screens(val route: String) {

    object GooGleSignIn : Screens("GoogleSignIn")

    object Outstanding : Screens("Outstanding")

    object InvoiceList : Screens("InvoiceList/{customerId}/{customerName}") {

        const val ARG_CUSTOMER_ID = "customerId"
        const val ARG_CUSTOMER_NAME = "customerName"

        fun navigationString(customerId: Int, customerName: String): String {

            val encodedName= if (customerName.contains("/")) {
                URLEncoder.encode(customerName,"UTF-8")
            } else
                customerName

            return "InvoiceList/${customerId}/${encodedName}"
        }

    }

}
