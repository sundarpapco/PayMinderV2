package com.example.payminder.screens

import androidx.navigation.NavBackStackEntry
import java.net.URLDecoder
import java.net.URLEncoder


sealed class Screens(val route: String) {

    object GooGleSignIn : Screens("GoogleSignIn")

    object Outstanding : Screens("Outstanding")

    object InvoiceList : Screens("InvoiceList/{customerId}/{customerName}") {

        private const val ARG_CUSTOMER_ID = "customerId"
        private const val ARG_CUSTOMER_NAME = "customerName"

        fun navigationString(customerId: Int, customerName: String): String {

            val encodedName = if (customerName.contains("/")) {
                URLEncoder.encode(customerName, "UTF-8")
            } else
                customerName

            return "InvoiceList/${customerId}/${encodedName}"
        }

        fun getArgs(navEntry:NavBackStackEntry):InvoiceListScreenArgs{

            val customerId=navEntry.arguments?.getString(ARG_CUSTOMER_ID)?.toInt()
                ?: error("CustomerId argument not found in InvoiceListScreen")

            val customerName=navEntry.arguments?.getString(ARG_CUSTOMER_NAME)
                ?: error("Customer name argument not found in InvoiceListScreen")

            return InvoiceListScreenArgs(
                customerId,
                URLDecoder.decode(customerName,"UTF-8")
            )
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

data class InvoiceListScreenArgs(val customerId: Int, val customerName: String)
