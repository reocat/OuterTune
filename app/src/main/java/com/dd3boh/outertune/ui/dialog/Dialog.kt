/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O⁠ute⁠rTu⁠ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.dialog

import android.content.ClipData
import android.text.format.Formatter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dd3boh.outertune.LocalSnackbarHostState
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DialogCornerRadius
import com.dd3boh.outertune.constants.SNACKBAR_VERY_SHORT
import com.dd3boh.outertune.db.entities.FormatEntity
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.utils.CurrentClientHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

@Composable
fun DefaultDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    buttons: (@Composable RowScope.() -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.padding(24.dp),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                horizontalAlignment = horizontalAlignment,
                modifier = modifier
                    .padding(24.dp)
            ) {
                if (icon != null) {
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.iconContentColor) {
                        Box(
                            Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
                if (title != null) {
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.titleContentColor) {
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier.align(if (icon == null) Alignment.Start else Alignment.CenterHorizontally)
                            ) {
                                title()
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                content()

                if (buttons != null) {
                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                            ProvideTextStyle(
                                value = MaterialTheme.typography.labelLarge
                            ) {
                                buttons()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.padding(24.dp),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(vertical = 24.dp)
            ) {
                LazyColumn(content = content)
            }
        }
    }
}

@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    initialTextFieldValue: TextFieldValue = TextFieldValue(),
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 10,
    isInputValid: (String) -> Boolean = { it.isNotEmpty() },
    keyboardType: KeyboardType = KeyboardType.Text,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null,
) {
    val (textFieldValue, onTextFieldValueChange) = remember {
        mutableStateOf(initialTextFieldValue)
    }

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier,
        icon = icon,
        title = title,
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }

            TextButton(
                enabled = isInputValid(textFieldValue.text),
                onClick = {
                    onDismiss()
                    onDone(textFieldValue.text)
                }
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = onTextFieldValueChange,
            placeholder = placeholder,
            singleLine = singleLine,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(),
            keyboardOptions = KeyboardOptions(
                imeAction = if (singleLine) ImeAction.Done else ImeAction.None,
                keyboardType = keyboardType
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone(textFieldValue.text)
                    onDismiss()
                }
            ),
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .focusRequester(focusRequester)
        )

        extraContent?.invoke()
    }
}

/**
 * Dialog for user interaction
 *
 * @param title Title of prompt
 * @param titleBar Title of prompt. Specifying this will override title
 * @param onDismiss
 * @param onConfirm
 * @param onReset
 * @param onCancel
 * @param content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPromptDialog(
    title: String? = null,
    titleBar: @Composable (RowScope.() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onReset: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    isInputValid: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) = BasicAlertDialog(
    onDismissRequest = { onDismiss() },
    content = {
        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(DialogCornerRadius)
                )
                .fillMaxWidth(0.8f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // title
                if (titleBar != null) {
                    Row {
                        titleBar()
                    }
                } else if (title != null) {
                    Text(
                        text = title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                content() // body
            }

            // bottom options
            // always have an ok, but explicit cancel/reset is optional
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onReset != null) {
                    Row(modifier = Modifier.weight(1f)) {
                        TextButton(
                            onClick = { onReset() },
                        ) {
                            Text(stringResource(R.string.reset))
                        }
                    }
                }

                if (onCancel != null) {
                    TextButton(
                        onClick = { onCancel() }
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }

                TextButton(
                    onClick = { onConfirm() },
                    enabled = isInputValid
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
)

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

@Composable
fun DetailsDialog(
    mediaMetadata: MediaMetadata,
    currentFormat: FormatEntity?,
    currentPlayCount: Int?,
    volume: Float,
    clipboard: Clipboard,
    setVisibility: (newState: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { setVisibility(false) },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null
            )
        },
        confirmButton = {
            TextButton(
                onClick = { setVisibility(false) }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val details = mutableListOf(
                    stringResource(R.string.song_title) to mediaMetadata.title,
                    stringResource(R.string.song_artists) to mediaMetadata.artists.joinToString { it.name },
                    stringResource(R.string.media_id) to mediaMetadata.id,
                    stringResource(R.string.play_count) to currentPlayCount.toString(),
                    stringResource(R.string.client) to CurrentClientHolder.currentClient?.clientName
                )

                if (!mediaMetadata.isLocal) {
                    details.add("Itag" to currentFormat?.itag?.toString())
                } else {
                    mediaMetadata.trackNumber?.let {
                        details.add(stringResource(R.string.track_number) to it.toString())
                    }
                    mediaMetadata.discNumber?.let {
                        details.add(stringResource(R.string.disc_number) to it.toString())
                    }
                    details.add(stringResource(R.string.sort_by_date_released) to mediaMetadata.getDateString())
                    details.add(stringResource(R.string.sort_by_date_modified) to mediaMetadata.getDateModifiedString())
                }

                details.addAll(
                    mutableListOf(
                        stringResource(R.string.mime_type) to currentFormat?.mimeType,
                        stringResource(R.string.codecs) to currentFormat?.codecs,
                        stringResource(R.string.bitrate) to currentFormat?.bitrate?.let { "${it / 1000} Kbps" },
                        stringResource(R.string.sample_rate) to currentFormat?.sampleRate?.let { "$it Hz" },
                        stringResource(R.string.bits_per_sample) to currentFormat?.bitsPerSample?.takeIf { it > 0 }?.toString(),
                    )
                )

                if (!mediaMetadata.isLocal) {
                    details.add(stringResource(R.string.loudness) to currentFormat?.loudnessDb?.let { "$it dB" })
                }

                details.addAll(mutableListOf(
                    stringResource(R.string.volume) to "${(volume * 100).toInt()}%",
                    stringResource(R.string.file_size) to currentFormat?.contentLength?.let {
                        // TODO: This should 1024 sized not 1000
                        if (mediaMetadata.isLocal && mediaMetadata.localPath != null && File(mediaMetadata.localPath).exists()) {
                            Formatter.formatShortFileSize(context, File(mediaMetadata.localPath).length())
                        } else {
                            Formatter.formatShortFileSize(context, it)
                        }
                    }
                ))

                if (mediaMetadata.isLocal) {
                    details.add(stringResource(R.string.file_path) to mediaMetadata.localPath)
                }

                currentFormat?.extraComment?.let {
                    details.add(stringResource(R.string.extra_details) to it)
                }

                details.forEach { (label, text) ->
                    val displayText = text ?: stringResource(R.string.unknown)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                val clipData = ClipData.newPlainText(label, AnnotatedString(displayText))
                                clipboard.nativeClipboard.setPrimaryClip(clipData)

                                coroutineScope.launch {
                                    val job = launch {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.copied),
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Indefinite
                                        )
                                    }
                                    delay(SNACKBAR_VERY_SHORT)
                                    job.cancel()
                                }
                            }
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(DialogCornerRadius)
    )
}

@Composable
fun InfoLabel(
    text: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.padding(horizontal = 8.dp)
) {
    Icon(
        if (isError) Icons.Outlined.Error else Icons.Outlined.Info,
        contentDescription = null,
        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(4.dp)
    )
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
