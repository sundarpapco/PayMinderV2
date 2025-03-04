package com.example.payminder.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.payminder.screens.CustomerInfoScreen
import com.example.payminder.screens.GoogleSignInScreen
import com.example.payminder.screens.InvoiceListScreen
import com.example.payminder.screens.OutstandingScreen
import com.example.payminder.screens.customerInfoScreen.CustomerInfoScreen
import com.example.payminder.screens.invoiceList.InvoiceListScreen
import com.example.payminder.screens.invoiceList.InvoiceListVM
import com.example.payminder.screens.outstandingList.OutStandingListVM
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

    NavHost(navController = navController, startDestination = GoogleSignInScreen) {

        composable<GoogleSignInScreen> {
            GoogleSignInScreen(navController)
        }

        composable<OutstandingScreen> {
            OutstandingScreen(navController)
        }

        composable<InvoiceListScreen> {
            val args = it.toRoute<InvoiceListScreen>()
            val viewModel = remember(it){
                ViewModelProvider(it)[InvoiceListVM::class.java]
            }
            InvoiceListScreen(
                args=args,
                viewModel = viewModel,
                navController = navController
            )
        }

        composable<CustomerInfoScreen>{
            CustomerInfoScreen(
                backStackEntry = it,
                navController = navController
           )
        }

    }

}

