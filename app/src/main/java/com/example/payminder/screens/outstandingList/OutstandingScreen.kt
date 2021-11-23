package com.example.payminder.screens.outstandingList

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.example.payminder.R
import com.example.payminder.createGoogleClient
import com.example.payminder.database.entities.Customer
import com.example.payminder.screens.Screens
import com.example.payminder.ui.LoadingDialog
import com.example.payminder.ui.LoadingScreen
import com.example.payminder.ui.TitleText
import com.example.payminder.ui.theme.PayMinderTheme
import com.example.payminder.util.LoadingStatus

@ExperimentalMaterialApi
@Composable
fun OutstandingScreen(
    navController: NavController
) {

    val context = LocalContext.current
    var fabState by remember { mutableStateOf(MultiFabState.COLLAPSED) }
    val graphEntry = remember { navController.getBackStackEntry(Screens.Outstanding.route) }
    val viewModel = remember { ViewModelProvider(graphEntry).get(OutStandingListVM::class.java) }
    val customers by viewModel.customers.observeAsState()
    val period by viewModel.loadDetails.observeAsState()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        //Do something with the selected content Uri here
        it?.let { uri ->
            viewModel.loadFileFromUri(uri)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        //Top AppBar and Contents
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            TopAppBar(
                title = {
                    TitleText(
                        title = stringResource(id = R.string.bills_receivable),
                        subtitle = period?.period ?: ""
                    )
                },
                actions = {
                    IconButton(onClick = {
                        filePicker.launch("application/vnd.ms-excel")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_file_upload),
                            contentDescription = "Load file"
                        )
                    }

                    IconButton(onClick = {
                        createGoogleClient(context).signOut().addOnSuccessListener {
                            val options = NavOptions.Builder()
                                .setPopUpTo(Screens.Outstanding.route, true)
                                .build()
                            navController.navigate(Screens.GooGleSignIn.route, options)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout_24),
                            contentDescription = "Log out"

                        )
                    }
                },
                elevation = 0.dp
            )
            //Box For Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                customers?.let {
                    if (it.isEmpty()) {
                        FileNotLoadedScreen()
                    } else {
                        CustomersList(it) { id, name ->
                            navController.navigate(
                                Screens.InvoiceList.navigationString(id, name)
                            )
                        }
                    }
                } ?: LoadingScreen()
            }
        }

        //Scrim when Fab is expended. Should be shown between other contents and Fab
        if (fabState == MultiFabState.EXPANDED)
            FabScrim {
                fabState = MultiFabState.COLLAPSED
            }


        //Floating Action Button
        MultiFloatingActionButton(
            iconId = R.drawable.ic_send,
            state = fabState,
            onFabClick = {
                fabState = if (it == MultiFabState.COLLAPSED)
                    MultiFabState.EXPANDED
                else
                    MultiFabState.COLLAPSED
            },
            expandedFabItems = sendIntimationFabItems(),
            onExpandedItemClick = {
                fabState=MultiFabState.COLLAPSED
                if(it.identifier=="sendMail")
                    viewModel.startSendingEmail()
            }
        )

        DisplayLoadingStatus(
            loadingStatus = viewModel.loadingStatus,
            onLoadSuccess = {
                Toast.makeText(context, "Load Success and Valid", Toast.LENGTH_SHORT).show()
                viewModel.loadingStatus = null
            },
            onLoadFailed = { reason ->
                Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                viewModel.loadingStatus = null
            }
        )
    }
}

@Composable
private fun FabScrim(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background.copy(alpha = 0.7f))
            .pointerInput(true) {
                detectTapGestures {
                    onClick()
                }
            }
    )
}

@Composable
private fun FileNotLoadedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.please_load_a_file),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@ExperimentalMaterialApi
@Composable
private fun CustomersList(
    customers: List<Customer>,
    onClick: (id: Int, name: String) -> Unit
) {


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(start = 12.dp, end = 12.dp, top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            Spacer(Modifier.height(24.dp))
        }

        items(
            items = customers,
            key = { it.id }
        ) { customer ->
            CustomerListItem(
                customer = customer,
                onClick = onClick
            )
        }

        item {
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun DisplayLoadingStatus(
    loadingStatus: LoadingStatus?,
    onLoadSuccess: () -> Unit,
    onLoadFailed: (reason: String) -> Unit
) {
    when (loadingStatus) {

        is LoadingStatus.Loading -> {
            LoadingDialog(msg = stringResource(id = R.string.loading_file))
        }

        is LoadingStatus.Success<*> -> {
            onLoadSuccess()
        }

        is LoadingStatus.Error -> {
            onLoadFailed(loadingStatus.error)
        }

    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun OutstandingScreenPreview() {
    PayMinderTheme {
        OutstandingScreen(
            rememberNavController()
        )
    }
}