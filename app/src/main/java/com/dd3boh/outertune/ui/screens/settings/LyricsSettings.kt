package com.dd3boh.outertune.ui.screens.settings


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.EnableKugouKey
import com.dd3boh.outertune.constants.EnableLrcLibKey
import com.dd3boh.outertune.constants.LyricFontSizeKey
import com.dd3boh.outertune.constants.LyricSourcePrefKey
import com.dd3boh.outertune.constants.LyricTrimKey
import com.dd3boh.outertune.constants.LyricsTextPositionKey
import com.dd3boh.outertune.constants.MultilineLrcKey
import com.dd3boh.outertune.constants.TranslationLanguageKey
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import me.bush.translator.Language
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    // state variables and such
    val (enableKugou, onEnableKugouChange) = rememberPreference(key = EnableKugouKey, defaultValue = true)
    val (enableLrcLib, onEnableLrcLibChange) = rememberPreference(key = EnableLrcLibKey, defaultValue = true)
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(LyricsTextPositionKey, defaultValue = LyricsPosition.CENTER)
    val (multilineLrc, onMultilineLrcChange) = rememberPreference(MultilineLrcKey, defaultValue = true)
    val (lyricTrim, onLyricTrimChange) = rememberPreference(LyricTrimKey, defaultValue = false)
    val (lyricFontSize, onLyricFontSizeChange) = rememberPreference(LyricFontSizeKey, defaultValue = 20)
    val (translationLanguage, onTranslationLanguageChange) = rememberEnumPreference(TranslationLanguageKey, defaultValue = Language.ENGLISH)


    val (preferLocalLyric, onPreferLocalLyric) = rememberPreference(LyricSourcePrefKey, defaultValue = true)
    var showFontSizeDialog by remember {
        mutableStateOf(false)
    }

    // lyrics font size
    if (showFontSizeDialog) {
        FontSizeDialog(
            initialValue = lyricFontSize,
            onDismiss = { showFontSizeDialog = false },
            onConfirm = {
                onLyricFontSizeChange(it)
                showFontSizeDialog = false
            },
            onReset = { onLyricFontSizeChange(20) }
        )
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceGroupTitle(
            title = stringResource(R.string.grp_lyrics_source)
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.enable_lrclib)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            checked = enableLrcLib,
            onCheckedChange = onEnableLrcLibChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_kugou)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            checked = enableKugou,
            onCheckedChange = onEnableKugouChange
        )

        // prioritize local lyric files over all cloud providers
        SwitchPreference(
            title = { Text(stringResource(R.string.lyrics_prefer_local)) },
            description = stringResource(R.string.lyrics_prefer_local_description),
            icon = { Icon(Icons.Rounded.ContentCut, null) },
            checked = preferLocalLyric,
            onCheckedChange = onPreferLocalLyric
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_lyrics_parser)
        )
        // multiline lyrics
        SwitchPreference(
            title = { Text(stringResource(R.string.lyrics_multiline_title)) },
            description = stringResource(R.string.lyrics_multiline_description),
            icon = { Icon(Icons.AutoMirrored.Rounded.Sort, null) },
            checked = multilineLrc,
            onCheckedChange = onMultilineLrcChange
        )

        // trim (remove spaces around) lyrics
        SwitchPreference(
            title = { Text(stringResource(R.string.lyrics_trim_title)) },
            icon = { Icon(Icons.Rounded.ContentCut, null) },
            checked = lyricTrim,
            onCheckedChange = onLyricTrimChange
        )

        // translation language
        EnumListPreference(
            title = { Text(stringResource(R.string.translate_lyrics)) },
            icon = { Icon(Icons.Rounded.Translate, null) },
            selectedValue = translationLanguage,
            onValueSelected = onTranslationLanguageChange,
            valueText = { it.name }
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_lyrics_format)
        )

        // lyrics position
        EnumListPreference(
            title = { Text(stringResource(R.string.lyrics_text_position)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            selectedValue = lyricsPosition,
            onValueSelected = onLyricsPositionChange,
            valueText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
            }
        )

        PreferenceEntry(
            title = { Text( stringResource(R.string.lyrics_font_Size)) },
            description = "$lyricFontSize sp",
            icon = { Icon(Icons.Rounded.TextFields, null) },
            onClick = { showFontSizeDialog = true }
        )
    }


    TopAppBar(
        title = { Text(stringResource(R.string.lyrics_settings_title)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

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
        modifier = Modifier.fillMaxWidth(0.8f),
        title = {
            Text(
                text = stringResource(R.string.lyrics_font_Size),
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    modifier = Modifier.padding(horizontal = 4.dp)
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
                        modifier = Modifier.padding(horizontal = 4.dp)
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
                modifier = Modifier.padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
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