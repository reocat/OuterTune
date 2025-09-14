package com.dd3boh.outertune.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.R

@Composable
fun EditorDialog(
    title: String,
    label: String,
    initialValue: String,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    validation: (String) -> Boolean = { true },
    errorMessage: String = "",
    content: @Composable () -> Unit = {}
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(initialValue)) }
    var isError by remember { mutableStateOf(!validation(initialValue)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.8f),
        title = { Text(text = title) },
        text = {
            Column(modifier = modifier) {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        isError = !validation(it.text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(label) },
                    isError = isError,
                    singleLine = false,
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!isError) {
                                onDone(textFieldValue.text)
                            }
                        }
                    )
                )
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                content()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isError) {
                        onDone(textFieldValue.text)
                    }
                },
                enabled = !isError
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
