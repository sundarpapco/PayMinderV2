package com.example.payminder.screens

import androidx.navigation.NavBackStackEntry
import java.net.URLEncoder


sealed class Screens(val route: String) {

    object GooGleSignIn : Screens("GoogleSignIn")

    object Outstanding : Screens("Outstanding")

    object InvoiceList : Screens("InvoiceList/{customerId}/{customerName}") {

        const val ARG_CUSTOMER_ID = "customerId"
        const val ARG_CUSTOMER_NAME = "customerName"

        fun navigationString(customerId: Int, customerName: String): String {

            val encodedName = if (customerName.contains("/")) {
                URLEncoder.encode(customerName, "UTF-8")
            } else
                customerName

            return "InvoiceList/${customerId}/${encodedName}"
        }

    }

    object CustomerInfo : Screens("CustomerInfo/{customerId}") {

        private const val ARG_CUSTOMER_ID = "customerId"

        fun navigationString(customerId: Int): String {
            return "CustomerInfo/${customerId}"
        }

        fun extractCustomerId(navEntry: NavBackStackEntry): Int {
            return navEntry.arguments?.getString(ARG_CUSTOMER_ID)?.toInt()
                ?: error("Customer Id argument not found")
        }

    }


}
