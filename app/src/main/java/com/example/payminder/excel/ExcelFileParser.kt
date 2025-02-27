package com.example.payminder.excel

import androidx.core.text.isDigitsOnly
import com.example.payminder.database.entities.Customer
import com.example.payminder.database.entities.Invoice
import com.example.payminder.database.entities.LoadDetails
import com.example.payminder.util.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.util.*
import kotlin.collections.HashMap

class ExcelFileParser(private val filePath: String) {

    companion object {

        private const val DATA_STARTING_ROW = 9
        private const val DATA_PERIOD_ROW = 5

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
        var billsReceivableField = ""
        var partyNameField = ""

        return try {

            sheet.getRow(4)?.let {
                billsReceivableField = it.getCell(0).stringCellValue
            } ?: return false

            sheet.getRow(7)?.let {
                partyNameField = it.getCell(7).stringCellValue
            } ?: return false

            return billsReceivableField == "Bills Receivable" && partyNameField == "Party's Name"

        } catch (e: Exception) {
            //If there is any exception while checking the file itself, then return false for file invalid
            false
        }

    }

    private fun parseDataFromFile(): ParsedRawData {

        val inputStream = FileInputStream(filePath)
        val workBook = WorkbookFactory.create(inputStream)
        val workSheet = workBook.getSheetAt(0)

        require(isValidFile(workSheet)) { "Invalid File format" }

        //getCell(Column,Row)

        val loadDetails = readLoadDetail(workSheet)
        val parsedList = LinkedList<ParsedRow>()
        var currentRow = DATA_STARTING_ROW
        var row: Row

        //to ignore the last row which is total, we are using < instead of <= worksheet.lastRowNum
        while (currentRow < workSheet.lastRowNum) {

            row = workSheet.getRow(currentRow)

            val data = ParsedRow().apply {
                invoiceDate = DataFormatter().formatCellValue(row.getCell(COLUMN_INVOICE_DATE))
                invoiceNumber = row.getCell(COLUMN_INVOICE_NUMBER).stringCellValue

                mobile1 = row.getCell(COLUMN_MOBILE1).stringCellValue.trim()
                //removing space between numbers
                mobile1 = mobile1.replace(" ", "")
                checkMobileNumber(mobile1, currentRow + 1)

                mobile2 = row.getCell(COLUMN_MOBILE2).stringCellValue.trim()
                //removing spaces between numbers
                mobile2 = mobile2.replace(" ", "")
                checkMobileNumber(mobile2, currentRow + 1)

                email1 = row.getCell(COLUMN_EMAIL1).stringCellValue.trim()
                checkEmail(email1, currentRow + 1)

                email2 = row.getCell(COLUMN_EMAIL2).stringCellValue.trim()
                checkEmail(email2, currentRow + 1)

                email3 = row.getCell(COLUMN_EMAIL3).stringCellValue.trim()
                checkEmail(email3, currentRow + 1)

                partyName = row.getCell(COLUMN_PARTY_NAME).stringCellValue
                invoiceAmount = row.getCell(COLUMN_INVOICE_AMOUNT).numericCellValue
                daysSinceInvoiced =
                    row.getCell(COLUMN_DAYS_SINCE_INVOICE).stringCellValue.toInt()

            }

            parsedList.add(data)
            currentRow++

        }

        inputStream.close()
        return ParsedRawData(loadDetails, parsedList)
    }

    private fun readLoadDetail(sheet: Sheet): LoadDetails {

        val fourthRow = sheet.getRow(DATA_PERIOD_ROW)
        require(fourthRow != null) { "Error while trying to read Load details (5th row)" }

        return LoadDetails().apply {
            period = fourthRow.getCell(0)?.stringCellValue?.split("to")?.last()?.trim()
                ?: error("Error while fetching the Load detail (First column of 5th row)")
        }
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

                    check(cityAndName.size >= 2) { "Invalid Customer name and city for ${parsedRow.partyName}" }
                    city = cityAndName[0].trim()
                    name = cityAndName[1].trim()

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
                    //val invoiceAmount = it.invoiceAmount.replace("\"", "").toDouble()
                    totalOutstanding += it.invoiceAmount
                    if (it.daysSinceInvoiced > 30)
                        overDueAmount += it.invoiceAmount

                    Invoice().apply {
                        this.customerId = customerId
                        number = it.invoiceNumber
                        date = it.invoiceDate
                        amount = it.invoiceAmount
                        daysSinceInvoiced = it.daysSinceInvoiced
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
            ParsedData(parsedRawData.detail, result)
        }

    private fun checkMobileNumber(mobileNumber: String, row: Int) {
        check(mobileNumber.isBlank() || (mobileNumber.isDigitsOnly() && mobileNumber.length == 10)) {
            "Invalid mobile number in row $row"
        }
    }

    private fun checkEmail(email: String, row: Int) {
        check(email.isBlank() || (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            "Invalid Email address in row $row"
        }
        android.util.Patterns.PHONE
    }
}