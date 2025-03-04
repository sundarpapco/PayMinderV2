package com.example.payminder.screens

import kotlinx.serialization.Serializable

@Serializable
object GoogleSignInScreen

@Serializable
object OutstandingScreen

@Serializable
data class InvoiceListScreen(val customerId: Int,val customerName: String)

@Serializable
data class CustomerInfoScreen(val customerId: Int)