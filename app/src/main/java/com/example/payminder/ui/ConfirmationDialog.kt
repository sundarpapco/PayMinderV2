package com.example.payminder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.payminder.ui.theme.PayMinderTheme

data class ConfirmationDialogState<T>(
    val id:Int,
    val title:String?=null,
    val msg:String,
    val positiveButtonText:String,
    val negativeButtonText:String?,
    val data:T?=null,
)

@Composable
fun ConfirmationDialog(
    state:ConfirmationDialogState<*>,
    onDismissRequest:()->Unit,
    onPositiveButtonClicked:(ConfirmationDialogState<*>)->Unit,
    onNegativeButtonClicked:(ConfirmationDialogState<*>)->Unit = {}
){

    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            ConfirmationDialogButtons(
                dialogState = state,
                onPositiveButtonClicked = onPositiveButtonClicked,
                onNegativeButtonClicked = onNegativeButtonClicked
            )
        },
        title = state.title?.let{
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.h6
                )
            }
        },
        text = {
            Text(
                text = state.msg,
                style = MaterialTheme.typography.body1
            )
        }

    )
}



@Composable
private fun ConfirmationDialogButtons(
    dialogState:ConfirmationDialogState<*>,
    onPositiveButtonClicked:(ConfirmationDialogState<*>)->Unit,
    onNegativeButtonClicked:(ConfirmationDialogState<*>)->Unit={}
){
    Row(
        modifier=Modifier.fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End
    ){
        dialogState.negativeButtonText?.let{
            TextButton(onClick = { onNegativeButtonClicked(dialogState) }) {
                Text(
                    text=it,
                    color= MaterialTheme.colors.secondary
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        TextButton(onClick = { onPositiveButtonClicked(dialogState) }) {
            Text(
                text=dialogState.positiveButtonText,
                color= MaterialTheme.colors.secondary
            )
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Preview
@Composable
private fun DialogPreview(){

    val state= remember{ConfirmationDialogState(
        id=1,
        data = "Some data",
        title = "Send Mail",
        msg = "Send message to all customers for whom the message has not yet sent?",
        positiveButtonText = "Send",
        negativeButtonText = "Cancel"
    )}

    PayMinderTheme {
        ConfirmationDialog(
            state = state,
            onDismissRequest = {  },
            onPositiveButtonClicked ={}
        )
    }

}