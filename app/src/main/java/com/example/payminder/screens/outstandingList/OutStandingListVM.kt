package com.example.payminder.screens.outstandingList

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.payminder.R
import com.example.payminder.database.MasterDatabase
import com.example.payminder.database.Repository
import com.example.payminder.database.entities.Customer
import com.example.payminder.ui.ConfirmationDialogState
import com.example.payminder.util.*
import com.example.payminder.worker.IntimationWorker
import com.example.payminder.worker.SendEmailWorker
import com.example.payminder.worker.SendMessageWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
    val loadDetails = repository.getLoadDetailLiveData()
    var confirmationDialogState: ConfirmationDialogState<*>? by mutableStateOf(null)
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery:StateFlow<String?> = _searchQuery
    val filteredCustomers = MutableStateFlow<List<Customer>?>(null)
    val isIntimationRunning = MediatorLiveData<Boolean>()

    init {

        isIntimationRunning.addSource(IntimationWorker.getWorkStatusLiveData(context)){workInfo->
            isIntimationRunning.value = workInfo?.let {
                it.isNotEmpty() && it.first().state == WorkInfo.State.RUNNING
            } ?: false
        }

        loadCustomers()
    }

    fun setSearchQuery(newQuery: String?) {
        _searchQuery.value = newQuery
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            repository.getAllCustomersFlow()
                .combine(searchQuery.debounce { if (it != null) 300 else 0 }) { customers, query ->
                    //ToDO: Save the searchQuery in the saved state handle here
                    query?.let {
                        customers.filter { customer ->
                            customer.name.lowercase(Locale.getDefault())
                                .contains(it.lowercase(Locale.getDefault()))
                        }
                    } ?: customers
                }.flowOn(Dispatchers.Default)
                .collect {
                    filteredCustomers.value = it
                }
        }

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