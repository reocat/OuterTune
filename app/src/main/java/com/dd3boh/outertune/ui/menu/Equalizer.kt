package com.dd3boh.outertune.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.R
import com.dd3boh.outertune.utils.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt


// Keys for DataStore
private val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
private val EQ_PRESET = intPreferencesKey("eq_preset")
private val EQ_CUSTOM_BANDS = stringPreferencesKey("eq_custom_bands")
private val EQ_CUSTOM_PRESETS = stringPreferencesKey("eq_custom_presets")
private val EQ_BASS_BOOST = floatPreferencesKey("eq_bass_boost")

// Data class for custom presets
data class CustomPreset(
    val name: String,
    val bandLevels: List<Short>
)

// Function to get preset data from DataStore
fun getEqualizerPrefs(dataStore: androidx.datastore.core.DataStore<Preferences>): Flow<EqualizerSettings> {
    return dataStore.data.map { preferences ->
        val enabled = preferences[EQ_ENABLED] ?: false
        val presetIndex = preferences[EQ_PRESET] ?: 0
        val bandLevelsJson = preferences[EQ_CUSTOM_BANDS] ?: "[]"
        val customPresetsJson = preferences[EQ_CUSTOM_PRESETS] ?: "[]"
        val bassBoost = preferences[EQ_BASS_BOOST] ?: 0f

        val bandLevels = try {
            val jsonArray = JSONArray(bandLevelsJson)
            List(jsonArray.length()) { jsonArray.getInt(it).toShort() }
        } catch (e: Exception) {
            emptyList()
        }

        val customPresets = try {
            val jsonArray = JSONArray(customPresetsJson)
            List(jsonArray.length()) { index ->
                val preset = jsonArray.getJSONObject(index)
                val name = preset.getString("name")
                val levels = preset.getJSONArray("levels")
                val bandValues = List(levels.length()) { levels.getInt(it).toShort() }
                CustomPreset(name, bandValues)
            }
        } catch (e: Exception) {
            emptyList()
        }

        EqualizerSettings(enabled, presetIndex, bandLevels, customPresets, bassBoost)
    }
}

// Data class to hold equalizer settings
data class EqualizerSettings(
    val enabled: Boolean = false,
    val presetIndex: Int = 0,
    val customBandLevels: List<Short> = emptyList(),
    val customPresets: List<CustomPreset> = emptyList(),
    val bassBoost: Float = 0f
)

@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val playerConnection = LocalPlayerConnection.current ?: return
    val coroutineScope = rememberCoroutineScope()

    val equalizerInstance = playerConnection.equalizer

    // Get settings from DataStore
    val settings by getEqualizerPrefs(dataStore).collectAsState(initial = EqualizerSettings())

    // Current UI state (initialize with settings but update on changes)
    var eqEnabled by remember { mutableStateOf(settings.enabled) }
    var currentPresetIndex by remember { mutableIntStateOf(settings.presetIndex) }
    var bassBoostValue by remember { mutableFloatStateOf(settings.bassBoost) }
    var showCustomPresetDialog by remember { mutableStateOf(false) }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    // Band levels state list: if settings.customBandLevels is not empty, use that
    val bandLevels = remember {
        mutableStateListOf<Short>().apply {
            if (settings.customBandLevels.isNotEmpty()) {
                addAll(settings.customBandLevels)
            } else {
                val count = equalizerInstance?.numberOfBands ?: 5
                repeat(count.toInt()) { add(0) }
            }
        }
    }

    // Sync local states when settings change from DataStore
    LaunchedEffect(settings) {
        eqEnabled = settings.enabled
        currentPresetIndex = settings.presetIndex
        bassBoostValue = settings.bassBoost
        if (settings.customBandLevels.isNotEmpty()) {
            bandLevels.clear()
            bandLevels.addAll(settings.customBandLevels)
        }
    }

    // Get band frequencies
    val bandFrequencies = remember {
        if (equalizerInstance != null) {
            List(equalizerInstance.numberOfBands.toInt()) { band ->
                equalizerInstance.getCenterFreq(band.toShort()) / 1000
            }
        } else {
            listOf(60, 230, 910, 3600, 14000) // Fallback values
        }
    }

    // Get preset names
    val presetNames = remember {
        if (equalizerInstance != null) {
            List(equalizerInstance.numberOfPresets.toInt()) { preset ->
                equalizerInstance.getPresetName(preset.toShort())
            }.toMutableList().apply {
                add("Custom")
                settings.customPresets.forEach { add(it.name) }
            }
        } else {
            listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock", "Custom")
        }
    }

    // Apply settings when they change
    LaunchedEffect(eqEnabled, currentPresetIndex, bandLevels.toList(), bassBoostValue) {
        if (equalizerInstance != null) {
            equalizerInstance.enabled = eqEnabled

            // Apply preset if not custom
            if (currentPresetIndex < equalizerInstance.numberOfPresets) {
                equalizerInstance.usePreset(currentPresetIndex.toShort())
            } else {
                // Apply custom bands
                bandLevels.forEachIndexed { index, level ->
                    if (index < equalizerInstance.numberOfBands) {
                        equalizerInstance.setBandLevel(index.toShort(), level)
                    }
                }
            }

            // Save settings to DataStore
            coroutineScope.launch {
                dataStore.edit { preferences ->
                    preferences[EQ_ENABLED] = eqEnabled
                    preferences[EQ_PRESET] = currentPresetIndex

                    // Save band levels as JSON
                    val bandsJson = JSONArray().apply {
                        bandLevels.forEach { put(it) }
                    }.toString()
                    preferences[EQ_CUSTOM_BANDS] = bandsJson

                    // Save bass boost
                    preferences[EQ_BASS_BOOST] = bassBoostValue
                }
            }
        }
    }

    // Update UI when preset changes
    LaunchedEffect(currentPresetIndex) {
        if (equalizerInstance != null && currentPresetIndex < equalizerInstance.numberOfPresets) {
            // If it's a built-in preset, get its band levels
            equalizerInstance.usePreset(currentPresetIndex.toShort())
            bandLevels.clear()
            repeat(equalizerInstance.numberOfBands.toInt()) { band ->
                bandLevels.add(equalizerInstance.getBandLevel(band.toShort()))
            }
        } else if (currentPresetIndex > (equalizerInstance?.numberOfPresets ?: 0)) {
            // If it's a custom preset, load its values
            val customPresetIndex = currentPresetIndex - (equalizerInstance?.numberOfPresets?.toInt() ?: 0) - 1
            if (customPresetIndex < settings.customPresets.size) {
                val preset = settings.customPresets[customPresetIndex]
                bandLevels.clear()
                bandLevels.addAll(preset.bandLevels)
            }
        }
    }

    // Save custom preset dialog
    if (showCustomPresetDialog) {
        SavePresetDialog(
            onSave = { name ->
                coroutineScope.launch {
                    // Get current custom presets
                    val currentPresets = settings.customPresets.toMutableList()

                    // Add new preset
                    currentPresets.add(CustomPreset(name, bandLevels.toList()))

                    // Save to DataStore
                    dataStore.edit { preferences ->
                        val presetsJson = JSONArray().apply {
                            currentPresets.forEach { preset ->
                                put(JSONObject().apply {
                                    put("name", preset.name)
                                    put("levels", JSONArray().apply {
                                        preset.bandLevels.forEach { put(it) }
                                    })
                                })
                            }
                        }.toString()
                        preferences[EQ_CUSTOM_PRESETS] = presetsJson
                    }

                    // Update current preset to the new one
                    currentPresetIndex = (equalizerInstance?.numberOfPresets?.toInt() ?: 0) + 1 + currentPresets.size - 1
                }
                showCustomPresetDialog = false
            },
            onDismiss = { showCustomPresetDialog = false }
        )
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.equalizer)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                // Enable/Disable Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (eqEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = eqEnabled,
                        onCheckedChange = { eqEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Preset Selector
                PresetSelector(
                    presets = presetNames,
                    currentPresetIndex = currentPresetIndex,
                    onPresetSelected = { currentPresetIndex = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency bands
                EqualizerBands(
                    bandFrequencies = bandFrequencies,
                    bandLevels = bandLevels,
                    onBandChanged = { index, value ->
                        if (index < bandLevels.size) {
                            bandLevels[index] = value
                            // If we change a band, switch to custom preset
                            if (currentPresetIndex < (equalizerInstance?.numberOfPresets ?: 0)) {
                                currentPresetIndex = (equalizerInstance?.numberOfPresets?.toInt() ?: 0)
                            }
                        }
                    },
                    enabled = eqEnabled
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save custom preset button
                Button(
                    onClick = { showCustomPresetDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.save_preset))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Advanced settings expander
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.advanced_settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { showAdvancedSettings = !showAdvancedSettings }) {
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (showAdvancedSettings) 180f else 0f)
                        )
                    }
                }

                // Advanced settings content
                AnimatedVisibility(
                    visible = showAdvancedSettings,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.bass_boost),
                                style = MaterialTheme.typography.titleSmall
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = bassBoostValue,
                                    onValueChange = { bassBoostValue = it },
                                    valueRange = 0f..1f,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(bassBoostValue * 100).toInt()}%",
                                    modifier = Modifier.width(50.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun PresetSelector(
    presets: List<String>,
    currentPresetIndex: Int,
    onPresetSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = stringResource(R.string.preset),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presets.getOrElse(currentPresetIndex) { "Custom" },
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null
                )
            }

            // Invisible clickable area
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(0.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Transparent)
                    .padding(0.dp)
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                presets.forEachIndexed { index, preset ->
                    DropdownMenuItem(
                        text = { Text(preset) },
                        onClick = {
                            onPresetSelected(index)
                            expanded = false
                        },
                        trailingIcon = {
                            if (index == currentPresetIndex) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EqualizerBands(
    bandFrequencies: List<Int>,
    bandLevels: List<Short>,
    onBandChanged: (Int, Short) -> Unit,
    enabled: Boolean,
    minLevel: Short = -1500,
    maxLevel: Short = 1500
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(bandFrequencies.indices.toList()) { index ->
            val freq = bandFrequencies[index]
            val level = bandLevels.getOrElse(index) { 0 }
            val normalizedLevel = (level - minLevel).toFloat() / (maxLevel - minLevel)
            val animatedLevel by animateFloatAsState(
                targetValue = normalizedLevel,
                label = "EqBandAnimation"
            )

            FrequencyBandSlider(
                frequency = freq,
                level = animatedLevel,
                onLevelChanged = { newLevel ->
                    val scaledLevel = (minLevel + (maxLevel - minLevel) * newLevel).toInt().toShort()
                    onBandChanged(index, scaledLevel)
                },
                enabled = enabled
            )
        }
    }
}

@Composable
fun FrequencyBandSlider(
    frequency: Int,
    level: Float,
    onLevelChanged: (Float) -> Unit,
    enabled: Boolean
) {
    val displayFreq = if (frequency >= 1000) {
        "${frequency / 1000}kHz"
    } else {
        "${frequency}Hz"
    }

    val density = LocalDensity.current
    val trackHeight = 200.dp
    val thumbSize = 24.dp

    // Convert track height and thumb size to pixels
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val availableSpacePx = trackHeightPx - thumbSizePx

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = if (level >= 0.5f) "+${((level - 0.5f) * 30).toInt()}"
            else "${((level - 0.5f) * 30).toInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )

        Box(
            modifier = Modifier
                .height(trackHeight)
                .width(48.dp)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press,
                                PointerEventType.Move -> {
                                    val y = event.changes.first().position.y
                                    val newLevel = 1 - (y / trackHeightPx)
                                        .coerceIn(0f, 1f)
                                    onLevelChanged(newLevel)
                                }
                                else -> Unit
                            }
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        ) {
            // Track
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.Center)
            )

            // Thumb bar
            Box(
                modifier = Modifier
                    .width(24.dp)    // Adjust width for a bar-like appearance
                    .height(8.dp)   // Adjust height to suit your design
                    .align(Alignment.TopCenter)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (availableSpacePx * (1 - level)).roundToInt()
                        )
                    }
                    .zIndex(1f)  // Ensure it appears on top
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        RoundedCornerShape(2.dp)  // Rounded corners for a nice bar effect
                    )
            )
        }

        Text(
            text = displayFreq,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
fun SavePresetDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_preset)) },
        text = {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text(stringResource(R.string.preset_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (presetName.isNotBlank()) {
                        onSave(presetName)
                    }
                },
                enabled = presetName.isNotBlank()
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}