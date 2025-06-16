package com.dd3boh.outertune.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.R
import kotlin.math.roundToInt

@Composable
fun SliderDialog(
    title: String,
    description: String? = null,
    initialValue: Int,
    defaultValue: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueSuffix: String = "",
    previewContent: @Composable ((Int) -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    onReset: (() -> Unit)? = null
) {
    var currentValue by remember { mutableFloatStateOf(initialValue.toFloat()) }

    val previewOpacity by animateFloatAsState(
        targetValue = if (currentValue == initialValue.toFloat()) 0.6f else 1f,
        label = "preview opacity"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.8f),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (description != null) 16.dp else 4.dp)
            ) {
                // Optional description
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Optional preview content
                previewContent?.let { preview ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(previewOpacity)
                    ) {
                        preview(currentValue.roundToInt())
                    }
                }

                // Slider
                Slider(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    valueRange = valueRange,
                    steps = steps,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Value controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            currentValue = (currentValue - 1).coerceAtLeast(valueRange.start)
                        },
                        enabled = currentValue > valueRange.start
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "${currentValue.roundToInt()}$valueSuffix",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    FilledTonalIconButton(
                        onClick = {
                            currentValue = (currentValue + 1).coerceAtMost(valueRange.endInclusive)
                        },
                        enabled = currentValue < valueRange.endInclusive
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Reset button (only show if onReset is provided)
                onReset?.let {
                    TextButton(
                        onClick = {
                            currentValue = defaultValue.toFloat()
                            it()
                        },
                        enabled = currentValue.roundToInt() != defaultValue
                    ) {
                        Text(stringResource(R.string.reset))
                    }
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