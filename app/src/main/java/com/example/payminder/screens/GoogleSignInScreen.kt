package com.example.payminder.screens

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.payminder.R
import com.example.payminder.createGoogleClient
import com.example.payminder.ui.theme.PayMinderTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun GoogleSignInScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val signedIn = remember {
        if (GoogleSignIn.getLastSignedInAccount(context) != null)
            mutableStateOf(true)
        else
            mutableStateOf(false)
    }
    val configuration = LocalConfiguration.current
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val completedTask = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {

            completedTask.getResult(ApiException::class.java)
            //Successfully logged In. Change the UI State
            signedIn.value = true

        } catch (e: ApiException) {
            //Parse the exception and provide the Snack bar if needed
            signedIn.value = false
        }
    }

    if (!signedIn.value) {
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            PortraitMode {
                signInLauncher.launch(createGoogleClient(context).signInIntent)
            }
        else
            LandscapeMode {
                signInLauncher.launch(createGoogleClient(context).signInIntent)
            }
    }else{
        LaunchedEffect(signedIn.value){
            if(signedIn.value) {
                val options=NavOptions.Builder()
                    .setPopUpTo(Screens.GooGleSignIn.route,true)
                    .build()
                navController.navigate(Screens.Outstanding.route,options)
            }
        }
    }


}

@Composable
private fun PortraitMode(
    onSignInButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.requiredWidth(150.dp),
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Company Logo"
            )

            Spacer(Modifier.height(80.dp))

            Button(onClick = onSignInButtonClick) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        tint = MaterialTheme.colors.secondary,
                        contentDescription = "Google Logo"
                    )
                    Text(
                        text = stringResource(id = R.string.sign_in_with_google),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

            }
        }
    }
}


@Composable
private fun LandscapeMode(
    onSignInButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Image(
                modifier = Modifier.requiredWidth(150.dp),
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Company Logo"
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Button(
                onClick = onSignInButtonClick
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        tint = MaterialTheme.colors.secondary,
                        contentDescription = "Google Logo"
                    )
                    Text(
                        text = stringResource(id = R.string.sign_in_with_google),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

            }
        }

    }
}


@Preview(name = "Portrait")
@Composable
private fun PreviewPortraitMode() {
    PayMinderTheme {
        PortraitMode {}
    }
}

@Preview(name = "Landscape", widthDp = 720, heightDp = 360)
@Composable
private fun PreviewLandscapeMode() {
    PayMinderTheme {
        LandscapeMode {}
    }
}