package com.example.payminder.screens.outstandingList

import android.Manifest
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import com.example.payminder.screens.FilterModalBottomSheet
import com.example.payminder.screens.Screens
import com.example.payminder.ui.*
import com.example.payminder.ui.theme.PayMinderTheme
import com.example.payminder.util.LoadingStatus
import com.example.payminder.util.isPermissionsGranted
import com.example.payminder.util.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch


val LocalSentIcon = staticCompositionLocalOf<Painter> {
    error("No Local value provided for Sent Icon")
}

val LocalNotSentIcon = staticCompositionLocalOf<Painter> {
    error("No Local value provided for Not sent Icon")
}

val LocalTotalAmountIcon = staticCompositionLocalOf<Painter> {
    error("No local value provided for total amount Icon")
}

val LocalOverdueIcon = staticCompositionLocalOf<Painter> {
    error("No Local value provided for Overdue Icon")
}


@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun OutstandingScreen(
    navController: NavController
) {
    val graphEntry = remember { navController.getBackStackEntry(Screens.Outstanding.route) }
    val viewModel = remember { ViewModelProvider(graphEntry).get(OutStandingListVM::class.java) }
    val filterSheetState =
        remember { ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden) }
    val filter by viewModel.filter.collectAsState()
    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = filterSheetState,
        sheetShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        sheetElevation = 0.dp,
        scrimColor = MaterialTheme.colors.background.copy(0.7f),
        sheetContent = {
            FilterModalBottomSheet(
                filter,
                onApply = {
                    scope.launch { filterSheetState.hide() }
                    viewModel.applyFilter(it)
                },
                onClear = {
                    scope.launch { filterSheetState.hide() }
                    viewModel.clearFilter()
                }
            )
        }
    ) {
        OutstandingScreenContent(
            viewModel = viewModel,
            navController = navController,
            filterSheetState = filterSheetState,
            isFilterEnabled = filter.isFilterEnabled()
        )
    }
}


@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@FlowPreview
@Composable
private fun OutstandingScreenContent(
    viewModel: OutStandingListVM,
    navController: NavController,
    filterSheetState: ModalBottomSheetState,
    isFilterEnabled: Boolean
) {
    val context = LocalContext.current
    var fabState by remember { mutableStateOf(MultiFabState.COLLAPSED) }
    val customers by viewModel.filteredCustomers.collectAsState(initial = null)
    val period by viewModel.loadDetails.observeAsState()
    val isIntimationRunning by viewModel.isIntimationRunning.observeAsState(false)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        //Do something with the selected content Uri here
        it?.let { uri ->
            viewModel.loadFileFromUri(uri)
        }
    }
    val permissionSeeker =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    val topBarActions by remember(isFilterEnabled) {

        mutableStateOf(
            listOf(

                OverflowMenuItem(
                    1,
                    context.getString(R.string.search),
                    R.drawable.ic_search
                ) {
                    viewModel.setSearchQuery("")
                },

                OverflowMenuItem(
                    id = 2,
                    label = context.getString(R.string.filter),
                    iconId = if (isFilterEnabled)
                        R.drawable.ic_filter_on
                    else
                        R.drawable.ic_filter_off

                ) {
                    coroutineScope.launch { filterSheetState.show() }
                },

                OverflowMenuItem(
                    3,
                    context.getString(R.string.load_file),
                    null
                ) {
                    if (isIntimationRunning)
                        toast(context, R.string.cannot_load_while_intimating)
                    else
                        filePicker.launch("application/vnd.ms-excel")
                },

                OverflowMenuItem(
                    4,
                    context.getString(R.string.sign_out),
                    null
                ) {
                    viewModel.showSignOutConfirmation()
                }
            )
        )
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {

        //Show any confirmation dialog if necessary
        viewModel.confirmationDialogState?.let {
            ConfirmationDialog(
                state = it,
                onDismissRequest = { viewModel.confirmationDialogState = null },
                onPositiveButtonClicked = { state ->
                    viewModel.confirmationDialogState = null
                    onDialogConfirmation(
                        context,
                        state,
                        navController,
                        viewModel
                    )
                },
                onNegativeButtonClicked = { viewModel.confirmationDialogState = null }
            )
        }


        //Top AppBar and Contents
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (searchQuery == null) {
                TopAppBar(
                    title = {
                        TitleText(
                            title = stringResource(id = R.string.bills_receivable),
                            subtitle = period?.let {
                                stringResource(id = R.string.until_xx, it.period)
                            } ?: ""
                        )
                    },
                    actions = {
                        ActionMenu(items = topBarActions)
                    },
                    elevation = 0.dp
                )
            } else {
                SearchBar(query = searchQuery!!, onQueryChange = viewModel::setSearchQuery)
            }

            if (isIntimationRunning)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.secondary
                )

            //Box For Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                customers?.let {
                    if (it.isEmpty()) {
                        period?.let {
                            EmptyScreen(stringResource(id = R.string.no_customers_found))
                        } ?: EmptyScreen(stringResource(id = R.string.please_load_a_file))

                    } else {
                        CustomersList(
                            customers = it,
                            onSendMailToCustomer = viewModel::showSendMailToCustomerConfirmation,
                            onSendMessageToCustomer = viewModel::showSendMessageToCustomerConfirmation

                        ) { id, name ->
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
                fabState = MultiFabState.COLLAPSED
                if (context.isPermissionsGranted()) {
                    if (it.identifier == R.id.fab_send_mail)
                        viewModel.showSendMailToAllConfirmation()

                    if (it.identifier == R.id.fab_send_message)
                        viewModel.showSendMessageToAllConfirmation()
                } else
                    toast(context, R.string.sms_permission_not_available)

            },
            isVisible = !isIntimationRunning
        )

        DisplayLoadingStatus(
            loadingStatus = viewModel.loadingStatus,
            onLoadSuccess = {
                toast(context, R.string.file_load_success)
                viewModel.loadingStatus = null
            },
            onLoadFailed = { reason ->
                toast(context, reason)
                viewModel.loadingStatus = null
            }
        )
    }

    DisposableEffect(true) {

        if (!context.isPermissionsGranted())
            permissionSeeker.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.READ_PHONE_STATE
                )
            )

        onDispose {

        }
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
private fun EmptyScreen(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = msg,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@ExperimentalMaterialApi
@Composable
private fun CustomersList(
    customers: List<Customer>,
    onSendMailToCustomer: (Int) -> Unit,
    onSendMessageToCustomer: (Int) -> Unit,
    onClick: (id: Int, name: String) -> Unit,
) {

    //Providing all the drawable Icons necessary in order to boost the scrolling smoothness
    CompositionLocalProvider(
        LocalSentIcon provides painterResource(id = R.drawable.ic_done),
        LocalNotSentIcon provides painterResource(id = R.drawable.ic_close),
        LocalTotalAmountIcon provides painterResource(id = R.drawable.ic_sigma),
        LocalOverdueIcon provides painterResource(id = R.drawable.ic_timer)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(start = 12.dp, end = 12.dp, top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

           item(key = "customer list top spacing") {
                Spacer(Modifier.height(24.dp))
            }

            items(
                items = customers,
                key = { it.id }
            ) { customer ->
                CustomerListItem(
                    customer = customer,
                    onClick = onClick,
                    onSendMail = onSendMailToCustomer,
                    onSendMessage = onSendMessageToCustomer
                )
            }

            item(key="customer list bottom spacing") {
                Spacer(Modifier.height(100.dp))
            }
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
        else ->{

        }

    }
}


@ExperimentalCoroutinesApi
@FlowPreview
private fun onDialogConfirmation(
    context: Context,
    state: ConfirmationDialogState<*>,
    navController: NavController,
    viewModel: OutStandingListVM
) {

    when (state.id) {

        R.id.confirmation_sign_out -> {
            createGoogleClient(context).signOut().addOnSuccessListener {
                val options = NavOptions.Builder()
                    .setPopUpTo(Screens.Outstanding.route, true)
                    .build()
                navController.navigate(Screens.GooGleSignIn.route, options)
            }
        }

        R.id.confirmation_send_mail_all -> {
            viewModel.startSendingEmail(state.isChecked)
        }

        R.id.confirmation_send_mail_customer -> {
            viewModel.sendEmailToCustomer(state.data as Int)
        }

        R.id.confirmation_send_msg_all -> {
            viewModel.startSendingMessages(state.isChecked)
        }

        R.id.confirmation_send_msg_customer -> {
            viewModel.sendMessageToCustomer(state.data as Int)
        }

        else -> {
            error("Invalid confirmation dialog ID")
        }

    }

}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String?) -> Unit
) {

    val searchBarFocus = remember { FocusRequester() }

    TopAppBar(
        elevation = 0.dp
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchBarFocus),
            value = query,
            onValueChange = { onQueryChange(it) },
            placeholder = {
                Text(
                    stringResource(id = R.string.search),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                )
            },
            trailingIcon = {
                IconButton(onClick = { onQueryChange(null) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close search",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            },
            singleLine = true,
            maxLines = 1,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colors.secondary
            )
        )
    }

    BackHandler {
        onQueryChange(null)
    }

    DisposableEffect(key1 = true) {
        searchBarFocus.requestFocus()
        onDispose { }
    }
}


@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
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