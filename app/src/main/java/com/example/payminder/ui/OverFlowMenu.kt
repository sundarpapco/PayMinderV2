package com.example.payminder.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.payminder.R
import com.example.payminder.ui.theme.PayMinderTheme

data class OverflowMenuItem(
    val id: Int,
    val label: String,
    val iconId: Int? = null,
    val onClick:()->Unit
)

@Composable
fun OverFlowMenu(
    items: List<OverflowMenuItem>,
    onDismiss: (() -> Unit)? = null,
) {

    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = {
            expanded = !expanded
        }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Overflow Menu",
                tint=MaterialTheme.colors.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onDismiss?.let{
                    it()
                }
                expanded = !expanded
            }
        ) {
            items.forEach {
                DropdownMenuItem(onClick = {
                    expanded = !expanded
                    it.onClick()
                }) {

                    it.iconId?.let { id ->
                        Icon(
                            painter = painterResource(id = id),
                            contentDescription = "Overflow Menu Icon"
                        )
                    }
                    Text(text = it.label)
                }
            }
        }
    }
}

@Composable
fun ActionMenu(
    items: List<OverflowMenuItem>,
    onDismiss: (() -> Unit)? = null,
){

    val actionItems = remember(items){items.filter { it.iconId != null }}
    val overflowItems = remember(items){items.filter { it.iconId==null }}

    for(item in actionItems){

        IconButton(onClick = {
            item.onClick()
        }) {
            Icon(
                painterResource(id = item.iconId!!),
                contentDescription = null
            )
        }
    }

    OverFlowMenu(items = overflowItems, onDismiss = onDismiss)
}


@Preview
@Composable
private fun OverFlowMenuPreview() {

    val menuItems = remember {
        listOf(
            OverflowMenuItem(1, "Send Email"){},
            OverflowMenuItem(2, "Send Message"){}
        )
    }

    PayMinderTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.CenterEnd
        ) {
            OverFlowMenu(items = menuItems, onDismiss = { })
        }
    }

}