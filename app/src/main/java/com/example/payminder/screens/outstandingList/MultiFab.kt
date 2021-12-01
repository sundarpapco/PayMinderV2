package com.example.payminder.screens.outstandingList

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.payminder.R
import java.util.*

enum class MultiFabState {
    EXPANDED, COLLAPSED
}

data class ExpandedFabItem(
    val identifier: Int,
    val iconId: Int,
    val label: String
)

@Composable
fun MultiFloatingActionButton(
    iconId: Int,
    state: MultiFabState,
    onFabClick: (MultiFabState) -> Unit,
    expandedFabItems: List<ExpandedFabItem>,
    onExpandedItemClick: (ExpandedFabItem) -> Unit,
    isVisible: Boolean = true,
    showLabels: Boolean = true
) {

    val miniFabScale by animateFloatAsState(if (state == MultiFabState.EXPANDED) 1f else 0f)
    val scale by animateFloatAsState(if (isVisible) 1f else 0f)

    if (scale > 0f)

        Column(
            modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.End
        ) {

            if (state == MultiFabState.EXPANDED) {
                expandedFabItems.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {

                        if (showLabels) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.alpha(miniFabScale)
                            ) {
                                Text(
                                    text = it.label,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        FloatingActionButton(
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer {
                                    scaleX = miniFabScale
                                    scaleY = miniFabScale
                                },
                            onClick = { onExpandedItemClick(it) },
                            backgroundColor = MaterialTheme.colors.secondary
                        ) {
                            Icon(
                                painter = painterResource(id = it.iconId),
                                contentDescription = "Fab Icon"
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            FloatingActionButton(
                modifier = Modifier.graphicsLayer {
                    scaleX=scale
                    scaleY=scale
                },
                onClick = { onFabClick(state) },
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Icon(
                    painter = if (state == MultiFabState.COLLAPSED)
                        painterResource(id = iconId)
                    else
                        painterResource(id = R.drawable.ic_close),
                    contentDescription = "Fab Icon"
                )
            }
        }
}

fun sendIntimationFabItems(): List<ExpandedFabItem> {

    return LinkedList<ExpandedFabItem>().apply {
        add(
            ExpandedFabItem(
                R.id.fab_send_mail,
                R.drawable.ic_email,
                "Send Email"
            )
        )
        add(
            ExpandedFabItem(
                R.id.fab_send_message,
                R.drawable.ic_sms,
                "Send Message"
            )
        )
    }
}