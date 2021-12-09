package com.example.payminder.screens.invoiceList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.payminder.R
import com.example.payminder.database.entities.Invoice
import com.example.payminder.screens.Screens
import com.example.payminder.ui.LoadingScreen
import com.example.payminder.ui.TitleText


@Composable
fun InvoiceListScreen(
    customerId: Int,
    customerName: String,
    navController: NavController
) {

    val graphEntry = remember { navController.getBackStackEntry(Screens.InvoiceList.route) }
    val viewModel = remember { ViewModelProvider(graphEntry).get(InvoiceListVM::class.java) }
    val invoiceList by viewModel.invoiceList.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back Button"
                        )
                    }
                },
                title = {
                    TitleText(
                        title = customerName,
                        subtitle = stringResource(id = R.string.outstanding_invoice)
                    )
                },
                actions = {
                          IconButton(onClick = {
                              navController.navigate(
                                  Screens.CustomerInfo.navigationString(customerId)
                              )
                          }) {
                              Icon(
                                  imageVector = Icons.Outlined.Info,
                                  contentDescription = "Customer Information Icon"
                              )
                          }
                },
                elevation = 0.dp
            )
        }
    ) {
        invoiceList?.let {
            InvoiceList(invoices = it)
        } ?: LoadingScreen()
    }

    DisposableEffect(true) {
        viewModel.loadInvoiceOfCustomer(customerId)
        onDispose { }
    }

}

@Composable
private fun InvoiceList(
    invoices: List<Invoice>
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(invoices, key = {
                it.id
            }) {
                InvoiceListItem(invoice = it)
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }

        }
    }
}