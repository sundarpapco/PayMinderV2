package com.example.payminder.util

import java.lang.Exception

sealed class LoadingStatus(){
    class Loading(msg:String):LoadingStatus()
    class Success<Y>(val data:Y):LoadingStatus()
    class Error(val error:String):LoadingStatus()
}
