package com.example.payminder.worker

data class MessageSentResult(
    val customerId: Int,
    val mobileNumber: String,
    val failureReason: String? = null
) {
    val isSuccess: Boolean
        get() = failureReason == null

}