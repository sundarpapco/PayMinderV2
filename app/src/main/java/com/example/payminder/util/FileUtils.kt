package com.example.payminder.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

fun temporaryFile(context:Context): File {
    val tempDirectory: File
    val cacheDirectory = context.cacheDir

    tempDirectory = File(cacheDirectory.absolutePath + "/temp")
    if (!tempDirectory.exists())
        tempDirectory.mkdirs()

    val filePath = tempDirectory.absolutePath + "/outstandingDetails.xls"
    return File(filePath)
}


@Suppress("BlockingMethodInNonBlockingContext")
suspend fun copyFileToCache(context:Context,fileUri:Uri)= withContext(Dispatchers.IO){

    val inputStream=context.contentResolver.openInputStream(fileUri)
    val fileData=inputStream?.readBytes()
    val fileToWrite= temporaryFile(context)
    val fos = FileOutputStream(fileToWrite)
    fos.write(fileData)
    fos.close()
}