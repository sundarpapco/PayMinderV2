package com.example.payminder.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.payminder.R
import com.example.payminder.ui.theme.PayMinderTheme
import com.example.payminder.util.CustomerListFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@Composable
fun FilterModalBottomSheet(
    initialFilter:CustomerListFilter,
    onApply:(CustomerListFilter)->Unit,
    onClear:()->Unit
) {

    var localFilter = remember(initialFilter) { initialFilter.copy() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SheetHeadingAndActions(
                onActionApply = {
                   onApply(localFilter)
                },
                onActionClearAll = onClear
            )
            Spacer(Modifier.height(24.dp))
            FiltersList(initialFilter = localFilter){
                localFilter=it
            }
            Spacer(Modifier.height(35.dp))

        }
    }

}

@Composable
private fun SheetHeadingAndActions(
    onActionApply: () -> Unit,
    onActionClearAll: () -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Text(
            text = stringResource(id = R.string.filter_by),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onActionClearAll) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                tint = MaterialTheme.colors.onSurface,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onActionApply) {
            Icon(
                painter = painterResource(id = R.drawable.ic_done),
                tint = MaterialTheme.colors.onSurface,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun FiltersList(
    initialFilter: CustomerListFilter,
    onFilterChange: (CustomerListFilter) -> Unit
) {

    var emailState by remember(initialFilter) { mutableStateOf(initialFilter.email) }
    var mobileState by remember(initialFilter) { mutableStateOf(initialFilter.mobile) }
    var overdueState by remember(initialFilter) { mutableStateOf(initialFilter.overdue) }
    var emailIntimationState by remember(initialFilter) { mutableStateOf(initialFilter.emailIntimation) }
    var smsIntimationState by remember(initialFilter) { mutableStateOf(initialFilter.smsIntimation) }

    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {

        FilterBy(heading = stringResource(
            id = R.string.email_address
        ),
            selectionIndex = emailState,
            items = listOf(
                stringResource(id = R.string.all), stringResource(id = R.string.have),
                stringResource(id = R.string.not_have)
            ),
            onSelectionChange = {
                emailState = it
                onFilterChange(
                    CustomerListFilter(
                        email = emailState,
                        mobile = mobileState,
                        overdue = overdueState,
                        emailIntimation = emailIntimationState,
                        smsIntimation = smsIntimationState
                    )
                )
            }
        )

        FilterBy(heading = stringResource(
            id = R.string.mobile_number
        ),
            selectionIndex = mobileState,
            items = listOf(
                stringResource(id = R.string.all), stringResource(id = R.string.have),
                stringResource(id = R.string.not_have)
            ),
            onSelectionChange = {
                mobileState = it
                onFilterChange(
                    CustomerListFilter(
                        email = emailState,
                        mobile = mobileState,
                        overdue = overdueState,
                        emailIntimation = emailIntimationState,
                        smsIntimation = smsIntimationState
                    )
                )
            }
        )

        FilterBy(heading = stringResource(
            id = R.string.overdue
        ),
            selectionIndex = overdueState,
            items = listOf(
                stringResource(id = R.string.all), stringResource(id = R.string.have),
                stringResource(id = R.string.not_have)
            ),
            onSelectionChange = {
                overdueState = it
                onFilterChange(
                    CustomerListFilter(
                        email = emailState,
                        mobile = mobileState,
                        overdue = overdueState,
                        emailIntimation = emailIntimationState,
                        smsIntimation = smsIntimationState
                    )
                )
            }
        )

        FilterBy(heading = stringResource(
            id = R.string.email_intimation
        ),
            selectionIndex = emailIntimationState,
            items = listOf(
                stringResource(id = R.string.all), stringResource(id = R.string.sent),
                stringResource(id = R.string.not_sent)
            ),
            onSelectionChange = {
                emailIntimationState = it
                onFilterChange(
                    CustomerListFilter(
                        email = emailState,
                        mobile = mobileState,
                        overdue = overdueState,
                        emailIntimation = emailIntimationState,
                        smsIntimation = smsIntimationState
                    )
                )
            }
        )

        FilterBy(heading = stringResource(
            id = R.string.sms_intimation
        ),
            selectionIndex = smsIntimationState,
            items = listOf(
                stringResource(id = R.string.all), stringResource(id = R.string.sent),
                stringResource(id = R.string.not_sent)
            ),
            onSelectionChange = {
                smsIntimationState = it
                onFilterChange(
                    CustomerListFilter(
                        email = emailState,
                        mobile = mobileState,
                        overdue = overdueState,
                        emailIntimation = emailIntimationState,
                        smsIntimation = smsIntimationState
                    )
                )
            }
        )
    }

}


@Composable
private fun FilterBy(
    heading: String,
    items: List<String>,
    selectionIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    Column {
        Text(
            text = heading,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.secondary
        )
        Spacer(Modifier.height(12.dp))
        ChipGroup(
            items = items,
            selectionIndex = selectionIndex,
            onSelectionChange = onSelectionChange
        )
    }
}

@Composable
private fun ChipGroup(
    items: List<String>,
    selectionIndex: Int,
    onSelectionChange: (Int) -> Unit
) {

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            Chip(
                text = item,
                isSelected = selectionIndex == index,
            ) {
                onSelectionChange(index)
            }
        }
    }
}

@Composable
private fun Chip(
    text: String,
    isSelected: Boolean,
    onSelectionChange: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.4f)),
        color = if (isSelected) MaterialTheme.colors.secondary else MaterialTheme.colors.surface,
        modifier = Modifier.clickable {
            if (!isSelected)
                onSelectionChange()
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
        )
    }
}

@Preview
@Composable
private fun PreviewHeading() {

    PayMinderTheme {
        Surface(
            elevation = 0.dp
        ) {
            SheetHeadingAndActions(onActionApply = { /*TODO*/ }) {

            }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@FlowPreview
@Preview
@Composable
private fun PreviewFiltersList() {


    PayMinderTheme {
        Surface {
            FilterModalBottomSheet(
                initialFilter = CustomerListFilter(),
                onApply = {
                          Log.d("SUNDAR",it.toString())
                },
                onClear = {}
            )
        }
    }
}

