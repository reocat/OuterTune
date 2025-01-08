package com.dd3boh.outertune.ui.component

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
import com.zionhuang.innertube.utils.parseCookieString

@Composable
fun TokenEditorDialog(
    initialValue: String,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(initialValue)) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.edit_token)) },
        text = {
            Column(modifier = modifier) {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        isError = it.text.isNotEmpty() && !isTokenValid(it.text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.token)) },
                    isError = isError,
                    singleLine = false,
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isTokenValid(textFieldValue.text)) {
                                onDone(textFieldValue.text)
                            }
                        }
                    )
                )
                if (isError) {
                    Text(
                        text = stringResource(R.string.invalid_token),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                InfoLabel(text = stringResource(R.string.token_adv_login_description))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isTokenValid(textFieldValue.text)) {
                        onDone(textFieldValue.text)
                    }
                },
                enabled = isTokenValid(textFieldValue.text)
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun isTokenValid(token: String): Boolean {
    return token.isNotEmpty() && try {
        "SAPISID" in parseCookieString(token)
    } catch (e: Exception) {
        false
    }
}