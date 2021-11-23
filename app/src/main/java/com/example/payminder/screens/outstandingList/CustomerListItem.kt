package com.example.payminder.screens.outstandingList

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.payminder.R
import com.example.payminder.database.entities.Customer
import com.example.payminder.ui.theme.PayMinderTheme
import com.example.payminder.ui.theme.SuccessGreen

@ExperimentalMaterialApi
@Composable
fun CustomerListItem(
    customer: Customer,
    onClick: (id:Int,name:String) -> Unit,
) {
    Card(
        onClick = { onClick(customer.id,customer.name) },
        modifier = Modifier
            .fillMaxWidth(),
        elevation = 0.dp
    ) {

        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier=Modifier.fillMaxWidth()
        ){
            IconButton(
                modifier = Modifier.width(24.dp),
                onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    contentDescription = "Overflow menu"
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            NameAndCity(name = customer.name, city = customer.city)
            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colors.background)
            Spacer(Modifier.height(12.dp))
            Row {
                PendingAmount(
                    totalAmount = customer.totalOutStandingRupees,
                    overdueAmount = customer.overdueRupees
                )
                Spacer(Modifier.weight(1f))
                MailAndMessageDetails(
                    hasMail = customer.hasEmailAddress(),
                    hasMobile = customer.hasMobileNumber(),
                    mailSent = customer.emailSent,
                    messageSent = customer.smsSent
                )

            }
        }

    }
}

@Composable
private fun NameAndCity(
    name: String,
    city: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            text = city,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PendingAmount(
    totalAmount: String,
    overdueAmount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier=modifier
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.requiredWidth(15.dp),
                painter = painterResource(id = R.drawable.ic_sigma),
                contentDescription = "Total outstanding Icon",
                tint = MaterialTheme.colors.secondary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = totalAmount,
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.requiredWidth(15.dp),
                painter = painterResource(id = R.drawable.ic_timer),
                contentDescription = "Overdue Icon",
                tint = MaterialTheme.colors.secondary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = overdueAmount,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
private fun MailAndMessageDetails(
    hasMail: Boolean,
    hasMobile: Boolean,
    mailSent: Boolean,
    messageSent: Boolean,
    modifier: Modifier = Modifier
) {

    if (!hasMail && !hasMobile)
        return

    Column(
        modifier = modifier.width(IntrinsicSize.Max)
    ) {
        if (hasMail) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.requiredWidth(15.dp),
                    painter = if (mailSent)
                        painterResource(id = R.drawable.ic_done)
                    else
                        painterResource(id = R.drawable.ic_close),
                    contentDescription = "Total outstanding Icon",
                    tint = if (mailSent)
                        SuccessGreen
                    else
                        MaterialTheme.colors.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.email),
                    style = MaterialTheme.typography.caption,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(Modifier.height(4.dp))
        }

        if (hasMobile) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.requiredWidth(15.dp),
                    painter = if (messageSent)
                        painterResource(id = R.drawable.ic_done)
                    else
                        painterResource(id = R.drawable.ic_close),
                    contentDescription = "Overdue Icon",
                    tint = if (messageSent)
                        SuccessGreen
                    else
                        MaterialTheme.colors.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.sms),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}


@ExperimentalMaterialApi
@Preview
@Composable
private fun PreviewCustomerListItem() {

    val customer = remember {
        Customer(
            totalOutStanding = 56458.0,
            overdueAmount = 3456.0
        ).apply {
            name = "Suri Graphix"
            city = "Sivakasi"
            mobile1 = "9894073488"
            email1 = "office@surigraphix.com"
            smsSent = true
        }
    }

    PayMinderTheme {
        CustomerListItem(
            customer = customer,
        ) {_,_->

        }
    }

}