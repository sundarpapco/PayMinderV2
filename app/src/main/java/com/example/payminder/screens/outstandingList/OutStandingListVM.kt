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
import com.example.payminder.util.ExcelFileParser
import com.example.payminder.util.LoadingStatus
import com.example.payminder.util.copyFileToCache
import com.example.payminder.util.temporaryFile
import com.example.payminder.worker.SendEmailWorker
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class OutStandingListVM(
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()
    private val repository=Repository(MasterDatabase.getInstance(context))
    var loadingStatus: LoadingStatus? by mutableStateOf(null)
    val customers=repository.getAllCustomersLiveData()
    val loadDetails=repository.getLoadDetailLiveData()

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

    fun startSendingEmail(){
        SendEmailWorker.startWith(context)
    }

}