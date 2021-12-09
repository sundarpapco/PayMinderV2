package com.example.payminder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.payminder.ui.theme.PayMinderTheme

@Composable
fun TitleText(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LoadingDialog(msg:String) {

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        LoadingDialogContent(msg = msg)
    }
}


@Composable
private fun LoadingDialogContent(msg:String){
    Surface(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ){
            CircularProgressIndicator(
                color = MaterialTheme.colors.secondary,
                modifier=Modifier.requiredSize(45.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(msg)
        }
    }
}

@Composable
fun LoadingScreen(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator(
            modifier=Modifier.size(50.dp),
            color = MaterialTheme.colors.secondary
        )
    }
}


@Preview
@Composable
private fun PreviewLoadingScreen(){
    PayMinderTheme{
        LoadingDialogContent(msg = "Loading File...")
    }
}