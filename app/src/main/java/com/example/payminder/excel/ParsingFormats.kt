package com.example.payminder.excel

import com.example.payminder.database.entities.Customer
import com.example.payminder.database.entities.Invoice
import com.example.payminder.database.entities.LoadDetails

data class ParsedRawData(
    val detail: LoadDetails,
    val parsedRows: List<ParsedRow>
)

data class ParsedRow(
    var invoiceDate: String = "",
    var invoiceNumber: String = "",
    var mobile1: String = "",
    var mobile2: String = "",
    var email1: String = "",
    var email2: String = "",
    var email3: String = "",
    var partyName: String = "",
    var invoiceAmount: Double = 0.0,
    var daysSinceInvoiced: Int = 0
)

data class ParsedData(
    val detail: LoadDetails,
    val data: Map<Customer, List<Invoice>>
)