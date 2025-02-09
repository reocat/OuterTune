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
    val initialText = buildString {
        append("***INNERTUBE COOKIE*** =$initialValue\n\n")
        append("***VISITOR DATA*** =$visitorData\n\n")
        append("***DATASYNC ID*** =$dataSyncId\n\n")
        append("***ACCOUNT NAME*** =$accountName\n\n")
        append("***ACCOUNT EMAIL*** =$accountEmail\n\n")
        append("***ACCOUNT CHANNEL HANDLE*** =$accountChannelHandle")
    }
    
    var textFieldValue by remember { mutableStateOf(TextFieldValue(initialText)) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.8f),
        title = { Text(text = stringResource(R.string.edit_token)) },
        text = {
            Column(modifier = modifier) {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        // Update error state based on cookie validity
                        val cookieLine = it.text.split("\n").find { line -> 
                            line.startsWith("***INNERTUBE COOKIE*** =") 
                        }?.substringAfter("***INNERTUBE COOKIE*** =") ?: ""
                        isError = cookieLine.isNotEmpty() && !isTokenValid(cookieLine)
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
                            processAndSaveToken(textFieldValue.text)
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
                onClick = { processAndSaveToken(textFieldValue.text) },
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

private fun processAndSaveToken(data: String) {
    data.split("\n").forEach { line ->
        when {
            line.startsWith("***INNERTUBE COOKIE*** =") -> 
                onInnerTubeCookieChange(line.substringAfter("***INNERTUBE COOKIE*** ="))
            line.startsWith("***VISITOR DATA*** =") -> 
                onVisitorDataChange(line.substringAfter("***VISITOR DATA*** ="))
            line.startsWith("***DATASYNC ID*** =") -> 
                onDataSyncIdChange(line.substringAfter("***DATASYNC ID*** ="))
            line.startsWith("***ACCOUNT NAME*** =") -> 
                onAccountNameChange(line.substringAfter("***ACCOUNT NAME*** ="))
            line.startsWith("***ACCOUNT EMAIL*** =") -> 
                onAccountEmailChange(line.substringAfter("***ACCOUNT EMAIL*** ="))
            line.startsWith("***ACCOUNT CHANNEL HANDLE*** =") -> 
                onAccountChannelHandleChange(line.substringAfter("***ACCOUNT CHANNEL HANDLE*** ="))
        }
    }
}

private fun isTokenValid(token: String): Boolean {
    return token.isNotEmpty() && try {
        "SAPISID" in parseCookieString(token)
    } catch (e: Exception) {
        false
    }
}
Last edited just now