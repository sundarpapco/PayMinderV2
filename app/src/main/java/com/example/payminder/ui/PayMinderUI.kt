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
            InvoiceListScreen(
                args=Screens.InvoiceList.getArgs(it),
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

