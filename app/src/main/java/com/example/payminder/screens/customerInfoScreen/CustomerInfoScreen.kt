package com.example.payminder.screens.customerInfoScreen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.payminder.R
import com.example.payminder.database.entities.Customer
import com.example.payminder.ui.LoadingScreen
import com.example.payminder.ui.theme.PayMinderTheme
import java.util.Locale

@Composable
fun CustomerInfoScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController
) {

    val config = LocalConfiguration.current
    val viewModel = remember { ViewModelProvider(backStackEntry)[CustomerInfoVM::class.java] }
    val customer = viewModel.customer

    if (customer == null) {
        LoadingScreen()
    } else {
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            CustomerInfoPortraitScreen(customer = customer, navController)
        else
            CustomerInfoLandscapeScreen(customer = customer, navController)
    }
}


@Composable
private fun CustomerInfoPortraitScreen(customer: Customer, navController: NavController) {

    val emails = remember { customer.emailAddresses() }
    val mobileNumbers = remember { customer.mobileNumbers() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colors.background)
    ) {

        Header(customer = customer, Modifier.height(250.dp)) {
            navController.popBackStack()
        }
        AmountBand(customer = customer)
        Spacer(modifier = Modifier.height(45.dp))
        IntimationDetails(
            icon = painterResource(id = R.drawable.ic_email),
            details = emails
        )
        if (emails.isNotEmpty())
            Spacer(Modifier.height(40.dp))
        IntimationDetails(
            icon = painterResource(id = R.drawable.ic_sms),
            details = mobileNumbers
        )
    }
}

@Composable
private fun CustomerInfoLandscapeScreen(customer: Customer, navController: NavController) {

    val emails = remember { customer.emailAddresses() }
    val mobileNumbers = remember { customer.mobileNumbers() }

    Row(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            Header(
                customer = customer,
                Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
            ) {
                navController.popBackStack()
            }
            AmountBand(customer = customer)
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(50.dp))

            IntimationDetails(
                icon = painterResource(id = R.drawable.ic_email),
                details = emails
            )

            if (emails.isNotEmpty())
                Spacer(Modifier.height(40.dp))

            IntimationDetails(
                icon = painterResource(id = R.drawable.ic_sms),
                details = mobileNumbers
            )
        }
    }
}

@Composable
private fun IntimationDetails(icon: Painter, details: List<String>) {

    details.forEachIndexed { index, address ->
        if (index == 0)
            DetailRow(icon = icon, detail = address)
        else
            DetailRow(icon = null, detail = address)

        Spacer(modifier = Modifier.height(12.dp))
    }
}


@Composable
private fun Header(customer: Customer, modifier: Modifier = Modifier, onBackPress: () -> Unit) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {

        CustomerInfo(customer = customer, Modifier.align(Alignment.Center))

        IconButton(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            onClick = onBackPress
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Navigate Back",
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}


@Composable
private fun CustomerInfo(customer: Customer, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier.size(60.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.first().uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.h4
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        Text(
            text = customer.name,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = customer.city,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

}


@Composable
private fun AmountBand(customer: Customer) {
    Surface(
        color = MaterialTheme.colors.secondary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Amount(
                icon = painterResource(id = R.drawable.ic_sigma),
                customer.totalOutStandingRupees,
                modifier = Modifier.weight(1f)
            )

            Amount(
                icon = painterResource(id = R.drawable.ic_timer),
                customer.overdueRupees,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Amount(
    icon: Painter,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.height(21.dp),
            painter = icon,
            contentDescription = "Total outstanding Icon",
            tint = MaterialTheme.colors.background
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.background
        )
    }
}

@Composable
private fun DetailRow(
    icon: Painter?,
    detail: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(45.dp))

        if (icon != null)
            Icon(
                modifier = Modifier.width(32.dp),
                painter = icon,
                tint = MaterialTheme.colors.secondary,
                contentDescription = "Detail Icon"
            )
        else
            Spacer(Modifier.size(32.dp))

        Spacer(Modifier.width(27.dp))

        Text(
            text = detail,
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.button,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Preview
@Composable
private fun PreviewCustomerInfoScreen() {

    val customer = remember {
        Customer(totalOutStanding = 123456.0, overdueAmount = 12345.0).apply {
            name = "A.P.K. Packs Pvt Ltd"
            city = "Sivakasi"
            email1 = "m.sundaravel@gmail.com"
            email2 = "madhanasundar@gmail.com"
            email3 = "papcopvtltd@gmail.com"
            mobile1 = "9047013696"
            mobile2 = "9843838696"
        }
    }

    PayMinderTheme {
        CustomerInfoPortraitScreen(customer = customer, NavController(LocalContext.current))
    }

}

@Preview(name = "Landscape", widthDp = 720, heightDp = 360)
@Composable
private fun PreviewCustomerInfoLandscape() {

    val customer = remember {
        Customer(totalOutStanding = 123456.0, overdueAmount = 12345.0).apply {
            name = "A.P.K. Packs Pvt Ltd"
            city = "Sivakasi"
            email1 = "m.sundaravel@gmail.com"
            email2 = "madhanasundar@gmail.com"
            email3 = "papcopvtltd@gmail.com"
            mobile1 = "9047013696"
            mobile2 = "9843838696"
        }
    }

    PayMinderTheme {
        CustomerInfoLandscapeScreen(customer = customer, NavController(LocalContext.current))
    }

}