package com.example.payminder.screens.outstandingList

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.payminder.R
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.ui.ConfirmationDialogState
import com.example.payminder.util.*
import com.example.payminder.worker.IntimationWorker
import com.example.payminder.worker.SendEmailWorker
import com.example.payminder.worker.SendMessageWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.*

@FlowPreview
@ExperimentalCoroutinesApi
class OutStandingListVM(
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()
    private val repository = Repository(MasterDatabase.getInstance(context))

    var loadingStatus: LoadingStatus? by mutableStateOf(null)
    val customers = repository.getAllCustomersLiveData()
    val loadDetails = repository.getLoadDetailLiveData()
    val intimationSendingStatus = IntimationWorker.getWorkStatusLiveData(context)
    var confirmationDialogState: ConfirmationDialogState<*>? by mutableStateOf(null)
    val searchQuery = MutableStateFlow<String?>(null)

    val filteredCustomers = repository.getAllCustomersFlow()
        .combine(searchQuery.debounce(500)) { customers, query ->
            //ToDO: Save the searchQuery in the saved state handle here
            query?.let {
                customers.filter { customer ->
                    customer.name.lowercase(Locale.getDefault())
                        .contains(it.lowercase(Locale.getDefault()))
                }
            } ?: customers
        }.flowOn(Dispatchers.Default)


    fun setSearchQuery(newQuery:String?){
        searchQuery.value=newQuery
    }


    fun loadFileFromUri(uri: Uri) {

        loadingStatus = LoadingStatus.Loading(context.getString(R.string.loading_file))
        viewModelScope.launch {

            loadingStatus = try {

                //Copy the file user selected to the cache directory of our application to read data
                copyFileToCache(context, uri)

                //Parse the copied file and check whether the file is valid or not
                val parser = ExcelFileParser(temporaryFile(context).absolutePath)
                val data = parser.readData()

                repository.saveParsedData(data)
                LoadingStatus.Success(true)

            } catch (e: FileNotFoundException) {
                LoadingStatus.Error(context.getString(R.string.check_your_internet_connection))
            } catch (e: Exception) {
                LoadingStatus.Error(e.getMessage(context))
            }

        }
    }

    fun showSendMailToAllConfirmation() {
        confirmationDialogState = ConfirmationDialogState<Unit>(
            id = R.id.confirmation_send_mail_all,
            title = context.getString(R.string.send_mail),
            msg = context.getString(R.string.confirm_send_mail_all),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel),
            checkBoxText = context.getString(R.string.force_send_to_all)
        )
    }

    fun showSendMessageToAllConfirmation() {
        confirmationDialogState = ConfirmationDialogState<Unit>(
            id = R.id.confirmation_send_msg_all,
            title = context.getString(R.string.send_message),
            msg = context.getString(R.string.confirm_send_message_all),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel),
            checkBoxText = context.getString(R.string.force_send_to_all)
        )
    }

    fun showSendMailToCustomerConfirmation(customerId: Int) {
        confirmationDialogState = ConfirmationDialogState(
            id = R.id.confirmation_send_mail_customer,
            data = customerId,
            title = context.getString(R.string.send_mail),
            msg = context.getString(R.string.confirm_send_mail_customer),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun showSendMessageToCustomerConfirmation(customerId: Int) {
        confirmationDialogState = ConfirmationDialogState(
            id = R.id.confirmation_send_msg_customer,
            data = customerId,
            title = context.getString(R.string.send_message),
            msg = context.getString(R.string.confirm_send_message_customer),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun showSignOutConfirmation() {
        confirmationDialogState = ConfirmationDialogState<Unit>(
            id = R.id.confirmation_sign_out,
            msg = context.getString(R.string.confirm_sign_out),
            positiveButtonText = context.getString(R.string.sign_out),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun startSendingEmail(forceSend: Boolean) {
        SendEmailWorker.startWith(context = context, forceSend = forceSend)
    }

    fun sendEmailToCustomer(customerId: Int) {
        SendEmailWorker.startWith(context = context, customerId)
    }

    fun startSendingMessages(forceSend: Boolean) {
        SendMessageWorker.startWith(context = context, forceSend = forceSend)
    }

    fun sendMessageToCustomer(customerId: Int) {
        SendMessageWorker.startWith(context = context, customerId)
    }

}