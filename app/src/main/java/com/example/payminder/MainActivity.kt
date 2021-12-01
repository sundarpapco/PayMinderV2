package com.example.payminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import com.example.payminder.ui.PayMinderUI
import com.example.payminder.ui.theme.PayMinderTheme

class MainActivity : ComponentActivity() {
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PayMinderTheme {
                PayMinderUI()
            }
        }
    }
}
