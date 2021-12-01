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
import com.example.payminder.util.ExcelFileParser
import com.example.payminder.util.LoadingStatus
import com.example.payminder.util.copyFileToCache
import com.example.payminder.util.temporaryFile
import com.example.payminder.worker.IntimationWorker
import com.example.payminder.worker.SendEmailWorker
import com.example.payminder.worker.SendMessageWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

@FlowPreview
@ExperimentalCoroutinesApi
class OutStandingListVM(
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()
    private val repository=Repository(MasterDatabase.getInstance(context))

    var loadingStatus: LoadingStatus? by mutableStateOf(null)
    val customers=repository.getAllCustomersLiveData()
    val loadDetails=repository.getLoadDetailLiveData()
    val intimationSendingStatus = IntimationWorker.getWorkStatusLiveData(context)
    var confirmationDialogState:ConfirmationDialogState<*>? by mutableStateOf(null)

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
                LoadingStatus.Error(context.getString(R.string.invalid_file_format))
            }

        }
    }

    fun showSendMailToAllConfirmation(){
        confirmationDialogState=ConfirmationDialogState<Unit>(
            id=R.id.confirmation_send_mail_all,
            title = context.getString(R.string.send_mail),
            msg = context.getString(R.string.confirm_send_mail_all),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun showSendMessageToAllConfirmation(){
        confirmationDialogState=ConfirmationDialogState<Unit>(
            id=R.id.confirmation_send_msg_all,
            title = context.getString(R.string.send_message),
            msg = context.getString(R.string.confirm_send_message_all),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun showSendMailToCustomerConfirmation(customerId: Int){
        confirmationDialogState=ConfirmationDialogState(
            id=R.id.confirmation_send_mail_customer,
            data=customerId,
            title = context.getString(R.string.send_mail),
            msg = context.getString(R.string.confirm_send_mail_customer),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun showSendMessageToCustomerConfirmation(customerId: Int){
        confirmationDialogState=ConfirmationDialogState(
            id=R.id.confirmation_send_msg_customer,
            data=customerId,
            title = context.getString(R.string.send_message),
            msg = context.getString(R.string.confirm_send_message_customer),
            positiveButtonText = context.getString(R.string.send),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun showSignOutConfirmation(){
        confirmationDialogState=ConfirmationDialogState<Unit>(
            id=R.id.confirmation_sign_out,
            msg = context.getString(R.string.confirm_sign_out),
            positiveButtonText = context.getString(R.string.sign_out),
            negativeButtonText = context.getString(R.string.cancel)
        )
    }

    fun startSendingEmail(){
        SendEmailWorker.startWith(context)
    }

    fun sendEmailToCustomer(customerId: Int){
        SendEmailWorker.startWith(context,customerId)
    }

    fun startSendingMessages(){
        SendMessageWorker.startWith(context)
    }

    fun sendMessageToCustomer(customerId:Int){
        SendMessageWorker.startWith(context,customerId)
    }

}