package com.example.payminder.screens.invoiceList

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payminder.R
import com.example.payminder.database.entities.Invoice
import com.example.payminder.ui.theme.PayMinderTheme

@Composable
fun InvoiceListItem(
    invoice: Invoice
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            InvoiceDetails(invoiceNumber = invoice.number, invoiceDate = invoice.date)
            Spacer(modifier = Modifier.weight(1f))
            AmountDetails(invoice = invoice)
        }
    }
}

@Composable
private fun InvoiceDetails(
    invoiceNumber: String,
    invoiceDate: String
) {
    Column {
        Text(
            text = invoiceNumber,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = invoiceDate,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AmountDetails(
    invoice: Invoice
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.width(18.dp),
                painter = painterResource(id = R.drawable.ic_sigma),
                tint = MaterialTheme.colors.secondary,
                contentDescription = "Icon"
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = invoice.amountRupees,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.width(18.dp),
                painter = painterResource(id = R.drawable.ic_timer),
                tint = MaterialTheme.colors.secondary,
                contentDescription = "Icon"
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.xx_days, invoice.daysSinceInvoiced),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }

    }
}

@Preview
@Composable
private fun InvoiceListItemPreview() {
    val invoice = remember {
        Invoice(amount = 123456.0).apply {
            number = "B/777 (1920)"
            date = "04-Oct-2021"
            overdueByDays = 32
        }
    }

    PayMinderTheme {
        InvoiceListItem(invoice = invoice)
    }
}
