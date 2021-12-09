package com.example.payminder.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.payminder.screens.GoogleSignInScreen
import com.example.payminder.screens.Screens
import com.example.payminder.screens.customerInfoScreen.CustomerInfoScreen
import com.example.payminder.screens.invoiceList.InvoiceListScreen
import com.example.payminder.screens.outstandingList.OutstandingScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.net.URLDecoder

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun PayMinderUI() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.GooGleSignIn.route) {

        composable(Screens.GooGleSignIn.route) {
            GoogleSignInScreen(navController)
        }

        composable(Screens.Outstanding.route) {
            OutstandingScreen(navController)
        }

        composable(Screens.InvoiceList.route) {
            val customerId: Int =
                it.arguments?.getString(Screens.InvoiceList.ARG_CUSTOMER_ID)?.toInt()
                    ?: error("Customer ID Argument not found")

            val customerName = it.arguments?.getString(Screens.InvoiceList.ARG_CUSTOMER_NAME)
                ?: error("Customer Name Argument not found")

            InvoiceListScreen(
                customerId = customerId,
                customerName = URLDecoder.decode(customerName,"UTF-8"),
                navController = navController
            )
        }

        composable(Screens.CustomerInfo.route){
            CustomerInfoScreen(
               customerId = Screens.CustomerInfo.extractCustomerId(it),
               navController = navController
           )
        }

    }

}

