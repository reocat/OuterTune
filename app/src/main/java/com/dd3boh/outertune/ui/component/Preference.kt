package com.dd3boh.outertune.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.dd3boh.outertune.ui.screens.settings.IconType
import com.dd3boh.outertune.ui.screens.settings.SettingsItem

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isEnabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isMiddle: Boolean = false
) {
    val cardShape = when {
        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        isLast -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        isMiddle -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(16.dp)
    }

    Column {
        Card(
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = isEnabled && onClick != null,
                        onClick = onClick ?: {}
                    )
                    .alpha(if (isEnabled) 1f else 0.5f)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (icon != null) {
                    Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                        icon()
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        title()
                    }
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (trailingContent != null) {
                    Spacer(Modifier.width(12.dp))
                    trailingContent()
                }
            }
        }

        if (!isLast && (isFirst || isMiddle)) {
            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}

@Composable
fun SettingsScreenSection(
    section: com.dd3boh.outertune.ui.screens.settings.SettingsSection,
    navController: androidx.navigation.NavController,
    showBadge: Boolean = false,
    onUpdateClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        section.items.forEachIndexed { index, item ->
            val isSingle = section.items.size == 1
            val isFirst = index == 0 && !isSingle
            val isLast = index == section.items.lastIndex && !isSingle
            

            SettingsScreenItem(
                item = item,
                onClick = {
                    if (onUpdateClick != null && item.route.isEmpty()) {
                        onUpdateClick()
                    } else {
                        navController.navigate(item.route)
                    }
                },
                showBadge = showBadge && item.icon is IconType.Vector && item.icon.imageVector == Icons.Outlined.Update,
                isFirst = isFirst,
                isLast = isLast,
                isSingle = isSingle
            )
        }
    }
}

@Composable
fun PreferenceEntry(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isEnabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isMiddle: Boolean = false
) {
    PreferenceItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        trailingContent = trailingContent,
        onClick = onClick,
        isEnabled = isEnabled,
        isFirst = isFirst,
        isLast = isLast,
        isMiddle = isMiddle
    )
}

@Composable
fun <T> ListPreference(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    selectedValue: T,
    values: List<T>,
    valueText: @Composable (T) -> String,
    onValueSelected: (T) -> Unit,
    onDisabled: (T) -> Boolean = { false },
    isEnabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isMiddle: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ListDialog(
            onDismiss = { showDialog = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            items(values) { value ->
                val isDisabled = onDisabled(value)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isDisabled) {
                            showDialog = false
                            onValueSelected(value)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    RadioButton(
                        selected = value == selectedValue,
                        onClick = null,
                        enabled = !isDisabled
                    )
                    Text(
                        text = valueText(value),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .alpha(if (isDisabled) 0.5f else 1f)
                    )
                }
            }
        }
    }

    PreferenceEntry(
        modifier = modifier,
        title = title,
        description = valueText(selectedValue),
        icon = icon,
        onClick = { showDialog = true },
        isEnabled = isEnabled,
        isFirst = isFirst,
        isLast = isLast,
        isMiddle = isMiddle
    )
}

@Composable
inline fun <reified T : Enum<T>> EnumListPreference(
    modifier: Modifier = Modifier,
    noinline title: @Composable () -> Unit,
    noinline icon: (@Composable () -> Unit)? = null,
    selectedValue: T,
    noinline valueText: @Composable (T) -> String,
    noinline onValueSelected: (T) -> Unit,
    isEnabled: Boolean = true,
    values: List<T> = enumValues<T>().toList(),
    noinline onDisabled: (T) -> Boolean = { false },
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isMiddle: Boolean = false
) {
    ListPreference(
        modifier = modifier,
        title = title,
        icon = icon,
        selectedValue = selectedValue,
        values = values,
        valueText = valueText,
        onValueSelected = onValueSelected,
        onDisabled = onDisabled,
        isEnabled = isEnabled,
        isFirst = isFirst,
        isLast = isLast,
        isMiddle = isMiddle
    )
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isMiddle: Boolean = false
) {
    PreferenceEntry(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = isEnabled,
                thumbContent = {
                    Icon(
                        imageVector = if (checked) Icons.Outlined.Check else Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            )
        },
        onClick = { onCheckedChange(!checked) },
        isEnabled = isEnabled,
        isFirst = isFirst,
        isLast = isLast,
        isMiddle = isMiddle
    )
}

@Composable
fun EditTextPreference(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    isInputValid: (String) -> Boolean = { it.isNotEmpty() },
    isEnabled: Boolean = true,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isMiddle: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        TextFieldDialog(
            initialTextFieldValue = TextFieldValue(text = value, selection = TextRange(value.length)),
            singleLine = singleLine,
            isInputValid = isInputValid,
            onDone = onValueChange,
            onDismiss = { showDialog = false }
        )
    }

    PreferenceEntry(
        modifier = modifier,
        title = title,
        description = value,
        icon = icon,
        onClick = { showDialog = true },
        isEnabled = isEnabled,
        isFirst = isFirst,
        isLast = isLast,
        isMiddle = isMiddle
    )
}

@Composable
fun PreferenceGroupTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(16.dp)
    )
}

@Composable
fun SettingsScreenItem(
    item: SettingsItem,
    onClick: () -> Unit,
    showBadge: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isSingle: Boolean = false
) {
    val cardShape = when {
        isSingle -> RoundedCornerShape(16.dp)
        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        isLast -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(8.dp)
    }

    PreferenceItem(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        description = item.description.takeIf { it.isNotEmpty() },
        icon = {
            BadgedBox(
                badge = {
                    if (showBadge) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = item.iconColor.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val icon = item.icon) {
                            is IconType.Vector -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = null,
                                tint = item.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            is IconType.Resource -> Icon(
                                painter = painterResource(icon.resId),
                                contentDescription = null,
                                tint = item.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        onClick = onClick,
        isFirst = isFirst,
        isLast = isLast,
        isMiddle = !isFirst && !isLast && !isSingle
    )
}