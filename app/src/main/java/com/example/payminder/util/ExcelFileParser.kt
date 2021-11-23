package com.example.payminder.util

import com.example.payminder.database.entities.Customer
import com.example.payminder.database.entities.Invoice
import com.example.payminder.database.entities.LoadDetails
import jxl.Sheet
import jxl.Workbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.HashMap

@Suppress("BlockingMethodInNonBlockingContext")
class ExcelFileParser(private val filePath: String) {

    companion object {

        private const val DATA_STARTING_ROW = 7

        private const val COLUMN_INVOICE_DATE = 0
        private const val COLUMN_INVOICE_NUMBER = 1
        private const val COLUMN_MOBILE1 = 2
        private const val COLUMN_MOBILE2 = 3
        private const val COLUMN_EMAIL1 = 4
        private const val COLUMN_EMAIL2 = 5
        private const val COLUMN_EMAIL3 = 6
        private const val COLUMN_PARTY_NAME = 7
        private const val COLUMN_INVOICE_AMOUNT = 8
        private const val COLUMN_DAYS_SINCE_INVOICE = 10

    }

    private fun isValidFile(sheet: Sheet): Boolean {

        //getCell(Column,Row)
        return try {
            val billsReceivableField = sheet.getCell(0, 3).contents
            val partyNameFieldData = sheet.getCell(7, 5).contents
            billsReceivableField == "Bills Receivable" && partyNameFieldData == "Party's Name"
        } catch (e: Exception) {
            //If there is any exception while checking the file itself, then return false for file invalid
            false
        }

    }

    private fun parseDataFromFile(): ParsedRawData {

        val file = File(filePath)
        check(file.exists()) { "File not found while trying to parse" }

        val workbook = Workbook.getWorkbook(file)
        val workSheet = workbook.getSheet(0)

        require(isValidFile(workSheet)){"Invalid File format"}

        //getCell(Column,Row)

        val loadDetails = readLoadDetail(workSheet)
        val parsedList = LinkedList<ParsedRow>()
        var currentRow = DATA_STARTING_ROW

        //worksheet.rows will give us how many rows are there in the excel sheet. Since we need
        //to ignore the last row which is total, we are using worksheet.rows-1
        while (currentRow < workSheet.rows - 1) {

            val data = ParsedRow().apply {
                invoiceDate = workSheet.getCell(COLUMN_INVOICE_DATE, currentRow).contents
                invoiceNumber = workSheet.getCell(COLUMN_INVOICE_NUMBER, currentRow).contents
                mobile1 = workSheet.getCell(COLUMN_MOBILE1, currentRow).contents
                mobile2 = workSheet.getCell(COLUMN_MOBILE2, currentRow).contents
                email1 = workSheet.getCell(COLUMN_EMAIL1, currentRow).contents
                email2 = workSheet.getCell(COLUMN_EMAIL2, currentRow).contents
                email3 = workSheet.getCell(COLUMN_EMAIL3, currentRow).contents
                partyName = workSheet.getCell(COLUMN_PARTY_NAME, currentRow).contents
                invoiceAmount = workSheet.getCell(COLUMN_INVOICE_AMOUNT, currentRow).contents
                daysSinceInvoiced =
                    workSheet.getCell(COLUMN_DAYS_SINCE_INVOICE, currentRow).contents.toInt()

            }
            parsedList.add(data)
            currentRow++

        }

        workbook.close()
        return ParsedRawData(loadDetails,parsedList)
    }

    private fun readLoadDetail(sheet:Sheet)=
        LoadDetails().apply {
            period=sheet.getCell(0,4).contents
        }

    suspend fun readData(): ParsedData =
        withContext(Dispatchers.IO) {

            val result: MutableMap<Customer, List<Invoice>> = HashMap()
            val parsedRawData = parseDataFromFile()
            var customerId = 1

            parsedRawData.parsedRows.groupBy {
                it.partyName
            }.forEach { (_, parsedRows) ->

                var totalOutstanding = 0.0
                var overDueAmount = 0.0

                val customer = Customer().apply {
                    val parsedRow = parsedRows.first()
                    val cityAndName = parsedRow.partyName.split(",")
                    if (cityAndName.size == 2) {
                        city = cityAndName.first().trim()
                        name = cityAndName.last().trim()
                    } else {
                        name = cityAndName.first()
                    }

                    id = customerId
                    mobile1 = parsedRow.mobile1
                    mobile2 = parsedRow.mobile2
                    email1 = parsedRow.email1
                    email2 = parsedRow.email2
                    email3 = parsedRow.email3
                }

                val invoices = parsedRows.map {

                    /*
                    Don't know why but the amount column read from the excel sheet as string contains
                    Two double quote characters in the front of the amount. So, we need to remove those
                    double quotes before trying to convert it in to Double. Else it will throw exception
                     */
                    val invoiceAmount = it.invoiceAmount.replace("\"", "").toDouble()
                    totalOutstanding += invoiceAmount
                    if (it.daysSinceInvoiced > 30)
                        overDueAmount += invoiceAmount

                    Invoice().apply {
                        this.customerId = customerId
                        number = it.invoiceNumber
                        date = it.invoiceDate
                        amount = invoiceAmount
                        overdueByDays = if (it.daysSinceInvoiced > 30)
                            it.daysSinceInvoiced - 30
                        else
                            0
                    }
                }

                customer.totalOutStanding = totalOutstanding.round(2)
                customer.overdueAmount = overDueAmount.round(2)
                result[customer] = invoices
                customerId++
            }
            ParsedData(parsedRawData.detail,result)
        }

    data class ParsedData(
        val detail:LoadDetails,
        val data:Map<Customer,List<Invoice>>
    )

}

private data class ParsedRawData(
    val detail: LoadDetails,
    val parsedRows:List<ParsedRow>
)

private data class ParsedRow(
    var invoiceDate: String = "",
    var invoiceNumber: String = "",
    var mobile1: String = "",
    var mobile2: String = "",
    var email1: String = "",
    var email2: String = "",
    var email3: String = "",
    var partyName: String = "",
    var invoiceAmount: String = "0.00",
    var daysSinceInvoiced: Int = 0
)