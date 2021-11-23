package com.example.payminder

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

fun createGoogleClient(
    context: Context
): GoogleSignInClient {
    val scopeSendMail = "https://www.googleapis.com/auth/gmail.send"
    val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    builder.requestScopes(Scope(scopeSendMail))
    builder.requestIdToken(context.getString(R.string.client_id_debug))
    builder.requestEmail()
    builder.requestProfile()
    val gso = builder.build()
    return GoogleSignIn.getClient(context, gso)
}