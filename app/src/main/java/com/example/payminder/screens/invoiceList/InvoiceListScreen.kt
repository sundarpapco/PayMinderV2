package com.example.payminder.screens.invoiceList

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.payminder.R
import com.example.payminder.database.entities.Invoice
import com.example.payminder.screens.CustomerInfoScreen
import com.example.payminder.screens.InvoiceListScreen
import com.example.payminder.ui.LoadingScreen
import com.example.payminder.ui.TitleText


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun InvoiceListScreen(
    args:InvoiceListScreen,
    viewModel: InvoiceListVM,
    navController: NavController
) {
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
                        title = args.customerName,
                        subtitle = stringResource(id = R.string.outstanding_invoice)
                    )
                },
                actions = {
                          IconButton(onClick = {
                              navController.navigate(CustomerInfoScreen(args.customerId))
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
    ) {padding->
        invoiceList?.let {
            InvoiceList(invoices = it, modifier = Modifier.padding(padding))
        } ?: LoadingScreen()
    }

}

@Composable
private fun InvoiceList(
    invoices: List<Invoice>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
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

@Composable
private fun CustomLayout(
    content: @Composable ()->Unit
){
    Layout(content = content){measurables,constraints->

        val firstMeasurable = measurables.first()
        firstMeasurable.parentData

        layout(
           constraints.maxWidth,
           constraints.maxHeight
       ){

       }
    }
}