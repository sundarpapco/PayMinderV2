package com.example.payminder

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

fun createGoogleClient(
    context: Context
): GoogleSignInClient {
    //val scopeSendMail = "https://www.googleapis.com/auth/gmail.send"
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply{
        //requestScopes(Scope(scopeSendMail))
        requestIdToken(context.getString(R.string.client_id_debug))
        requestEmail()
        requestProfile()
    }.build()

    return GoogleSignIn.getClient(context, gso)
}