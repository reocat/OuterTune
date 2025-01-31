package com.dd3boh.outertune.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dd3boh.outertune.R
import kotlin.math.roundToInt

@Composable
fun FontSizeDialog(
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    onReset: () -> Unit
) {
    var currentValue by remember { mutableFloatStateOf(initialValue.toFloat()) }
    val defaultValue = 20f

    val previewOpacity by animateFloatAsState(
        targetValue = if (currentValue == initialValue.toFloat()) 0.6f else 1f,
        label = "preview opacity"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.lyrics_font_Size),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Preview Text",
                    fontSize = currentValue.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(previewOpacity)
                )

                Slider(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    valueRange = 8f..32f,
                    steps = 23,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { if (currentValue > 8f) currentValue-- },
                        enabled = currentValue > 8f
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }

                    Text(
                        text = "${currentValue.roundToInt()} sp",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    FilledTonalIconButton(
                        onClick = { if (currentValue < 32f) currentValue++ },
                        enabled = currentValue < 32f
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(
                    onClick = {
                        currentValue = defaultValue
                        onReset()
                    },
                    enabled = currentValue.roundToInt() != defaultValue.toInt()
                ) {
                    Text(stringResource(R.string.reset))
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }

                Button(
                    onClick = { onConfirm(currentValue.roundToInt()) },
                    enabled = currentValue.roundToInt() != initialValue
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    )
}