package com.example.payminder.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.payminder.R
import java.math.BigDecimal
import kotlin.math.pow

fun Exception.getMessage(context: Context): String =
    this.message ?: context.getString(R.string.unknown_error)

fun Double.round(decimalPlaces: Int): Double {

    val factor = 10.0.pow(decimalPlaces.toDouble())
    val value = this * factor
    return if (value - value.toInt() >= 0.5)
        (value.toInt() + 1) / factor
    else
        (value.toInt()) / factor

}

fun Double.rupeeFormatString(includeSymbol: Boolean = true): String {

    if (this == 0.0)
        return if (includeSymbol)
            "\u20B9 0"
        else
            "0"

    val bigDecimal = BigDecimal(this).setScale(2, BigDecimal.ROUND_HALF_EVEN)
    val split = bigDecimal.toPlainString().split(".")
    var wholePartResult = ""
    val reversed = split[0].reversed()

    for (i in 1..reversed.length) {
        wholePartResult = if (i <= 3 || i % 2 != 0)
            reversed[i - 1] + wholePartResult
        else
            reversed[i - 1] + "," + wholePartResult
    }

    return if (includeSymbol)
        "\u20B9 $wholePartResult"
    else
        wholePartResult

}

fun Context.isPermissionsGranted(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
}

fun toast(context: Context, stringResource: Int, toastLength: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(
        context,
        context.getString(stringResource),
        toastLength
    ).show()
}

fun toast(context: Context, msg: String, toastLength: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(
        context,
        msg,
        toastLength
    ).show()
}
