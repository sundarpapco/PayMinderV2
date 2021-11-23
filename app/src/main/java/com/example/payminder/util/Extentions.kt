package com.example.payminder.util

import android.content.Context
import com.example.payminder.R
import java.math.BigDecimal
import kotlin.math.pow

fun Exception.getMessage(context:Context):String=
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
            "\u20B9 0.00"
        else
            "0.00"

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

    return if(includeSymbol)
        "\u20B9 $wholePartResult.${split[1]}"
    else
        "$wholePartResult.${split[1]}"

}
