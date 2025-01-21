package com.dd3boh.outertune.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AccountChannelHandleKey
import com.dd3boh.outertune.constants.AccountEmailKey
import com.dd3boh.outertune.constants.AccountNameKey
import com.dd3boh.outertune.constants.AppLanguageKey
import com.dd3boh.outertune.constants.ContentCountryKey
import com.dd3boh.outertune.constants.ContentLanguageKey
import com.dd3boh.outertune.constants.CountryCodeToName
import com.dd3boh.outertune.constants.InnerTubeCookieKey
import com.dd3boh.outertune.constants.LanguageCodeToName
import com.dd3boh.outertune.constants.LikedAutoDownloadKey
import com.dd3boh.outertune.constants.LikedAutodownloadMode
import com.dd3boh.outertune.constants.ProxyEnabledKey
import com.dd3boh.outertune.constants.ProxyTypeKey
import com.dd3boh.outertune.constants.ProxyUrlKey
import com.dd3boh.outertune.constants.SYSTEM_DEFAULT
import com.dd3boh.outertune.constants.VisitorDataKey
import com.dd3boh.outertune.constants.YtmSyncKey
import com.dd3boh.outertune.ui.component.EditTextPreference
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.ListPreference
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.component.TokenEditorDialog
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.zionhuang.innertube.utils.parseCookieString
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.net.Proxy
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current

    val accountName by rememberPreference(AccountNameKey, "")
    val accountEmail by rememberPreference(AccountEmailKey, "")
    val accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, defaultValue = true)
    val (likedAutoDownload, onLikedAutoDownload) = rememberEnumPreference(LikedAutoDownloadKey, LikedAutodownloadMode.OFF)
    val (contentLanguage, onContentLanguageChange) = rememberPreference(key = ContentLanguageKey, defaultValue = "system")
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")
    val availableLanguages = remember { getAvailableLanguages(context) }
    val localeManager = remember { LocaleManager(context) }
    val (selectedLanguage, setSelectedLanguage) = rememberPreference(key = AppLanguageKey, defaultValue = "system")

    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")

    // temp vars
    var showToken: Boolean by remember {
        mutableStateOf(false)
    }

    var showTokenEditor by remember {
        mutableStateOf(false)
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        PreferenceGroupTitle(
            title = stringResource(R.string.account)
        )
        PreferenceEntry(
            title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
            description = if (isLoggedIn) {
                accountEmail.takeIf { it.isNotEmpty() }
                    ?: accountChannelHandle.takeIf { it.isNotEmpty() }
            } else null,
            icon = { Icon(Icons.Rounded.Person, null) },
            onClick = { navController.navigate("login") }
        )
        if (isLoggedIn) {
            PreferenceEntry(
                title = { Text(stringResource(R.string.logout)) },
                icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) },
                onClick = {
                    onInnerTubeCookieChange("")
                    runBlocking {
                        context.dataStore.edit { settings ->
                            settings.remove(InnerTubeCookieKey)
                            settings.remove(VisitorDataKey)
                        }
                    }
                }
            )
        }

        PreferenceEntry(
            title = {
                if (showToken) {
                    Text(stringResource(R.string.token_shown))
                    Text(
                        text = if (isLoggedIn) innerTubeCookie else stringResource(R.string.not_logged_in),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1 // just give a preview so user knows it's at least there
                    )
                } else {
                    Text(stringResource(R.string.token_hidden))
                }
            },
            icon = { Icon(Icons.Rounded.VpnKey, null) },
            onClick = {
                if (!showToken) {
                    showToken = true
                } else {
                    showTokenEditor = true
                }
            },
        )

        if (showTokenEditor) {
            TokenEditorDialog(
                initialValue = innerTubeCookie,
                onDone = { newToken ->
                    onInnerTubeCookieChange(newToken)
                    showTokenEditor = false
                },
                onDismiss = { showTokenEditor = false },
                modifier = Modifier
            )
        }

        SwitchPreference(
            title = { Text(stringResource(R.string.ytm_sync)) },
            icon = { Icon(Icons.Rounded.Sync, null) },
            checked = ytmSync,
            onCheckedChange = onYtmSyncChange,
            isEnabled = isLoggedIn
        )
        ListPreference(
            title = { Text(stringResource(R.string.like_autodownload)) },
            icon = { Icon(Icons.Rounded.Favorite, null) },
            values = listOf(LikedAutodownloadMode.OFF, LikedAutodownloadMode.ON, LikedAutodownloadMode.WIFI_ONLY),
            selectedValue = likedAutoDownload,
            valueText = { when (it) {
                LikedAutodownloadMode.OFF -> stringResource(R.string.off)
                LikedAutodownloadMode.ON -> stringResource(R.string.on)
                LikedAutodownloadMode.WIFI_ONLY -> stringResource(R.string.wifi_only)
            } },
            onValueSelected = onLikedAutoDownload
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.localization)
        )
        // Language settings
        ListPreference(
            title = { Text(stringResource(R.string.app_language)) },
            icon = { Icon(Icons.Rounded.Public, null) },
            selectedValue = selectedLanguage,
            values = availableLanguages.map { it.code },
            valueText = { code ->
                availableLanguages.find { it.code == code }?.displayName
                    ?: stringResource(R.string.system_default)
            },
            onValueSelected = { newLanguage ->
                if (newLanguage == "system") {
                    // Use system default language
                    val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        LocaleList.getDefault()[0]
                    } else {
                        @Suppress("DEPRECATION")
                        Locale.getDefault()
                    }

                    if (localeManager.updateLocale(systemLocale.language)) {
                        setSelectedLanguage("system")
                        restartApp(context)
                    }
                } else if (localeManager.updateLocale(newLanguage)) {
                    setSelectedLanguage(newLanguage)
                    restartApp(context)
                } else {
                    Toast.makeText(
                        context,
                        "Failed to update language. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        ListPreference(
            title = { Text(stringResource(R.string.content_language)) },
            icon = { Icon(Icons.Rounded.Language, null) },
            selectedValue = contentLanguage,
            values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
            valueText = {
                LanguageCodeToName.getOrElse(it) {
                    stringResource(R.string.system_default)
                }
            },
            onValueSelected = onContentLanguageChange
        )
        ListPreference(
            title = { Text(stringResource(R.string.content_country)) },
            icon = { Icon(Icons.Rounded.LocationOn, null) },
            selectedValue = contentCountry,
            values = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
            valueText = {
                CountryCodeToName.getOrElse(it) {
                    stringResource(R.string.system_default)
                }
            },
            onValueSelected = onContentCountryChange
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.proxy)
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_proxy)) },
            checked = proxyEnabled,
            onCheckedChange = onProxyEnabledChange
        )

        if (proxyEnabled) {
            ListPreference(
                title = { Text(stringResource(R.string.proxy_type)) },
                selectedValue = proxyType,
                values = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
                valueText = { it.name },
                onValueSelected = onProxyTypeChange
            )
            EditTextPreference(
                title = { Text(stringResource(R.string.proxy_url)) },
                value = proxyUrl,
                onValueChange = onProxyUrlChange
            )
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.content)) },
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

data class LanguageInfo(
    val code: String,
    val displayName: String,
    val isAvailable: Boolean = true
)
fun getAvailableLanguages(context: Context): List<LanguageInfo> {
    val availableLocales = context.resources.assets.locales

    val systemDefault = LanguageInfo(
        code = "system",
        displayName = context.getString(R.string.system_default),
        isAvailable = true
    )

    fun getLocaleComponents(localeString: String): Pair<String, String> {
        val locale = if (localeString.contains("-")) {
            val (language, country) = localeString.split("-")
            Locale(language, country)
        } else {
            Locale(localeString)
        }

        val language = locale.language

        val country = locale.country


        return Pair(language, country)
    }

    val languageList = LanguageCodeToName.map { (code, name) ->
        LanguageInfo(
            code = code,
            displayName = name,
            isAvailable = availableLocales.any { localeString ->
                val (localeLanguage, localeCountry) = getLocaleComponents(localeString)

                when {
                    // Handle Chinese variants
                    code == "zh-CN" -> localeLanguage == "zh" && localeCountry == "CN"
                    code == "zh-TW" -> localeLanguage == "zh" && localeCountry == "TW"
                    code == "zh-HK" -> localeLanguage == "zh" && localeCountry == "HK"

                    // Handle other languages with country codes
                    code.contains("-") -> {
                        val (lang, country) = code.split("-")
                        localeLanguage == lang && localeCountry == country
                    }

                    // Handle simple language codes
                    else -> localeLanguage == code
                }
            }
        )
    }

    // Return system default plus available languages
    return listOf(systemDefault) + languageList.filter { it.isAvailable }
}

// Helper function to restart the app
private fun restartApp(context: Context) {
    val intent = context.packageManager
        .getLaunchIntentForPackage(context.packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

// LocaleManager
class LocaleManager(private val context: Context) {
    companion object {
        private val COMPLEX_SCRIPT_LANGUAGES = setOf(
            "ne", "mr", "hi", "bn", "pa", "gu", "ta", "te", "kn", "ml",
            "si", "th", "lo", "my", "ka", "am", "km",
            "zh-CN", "zh-TW", "zh-HK", "ja", "ko"
        )
    }

    fun updateLocale(languageCode: String): Boolean {
        try {
            val locale = when (languageCode) {
                "system" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    LocaleList.getDefault()[0]
                } else {
                    @Suppress("DEPRECATION")
                    Locale.getDefault()
                }
                else -> createLocaleFromCode(languageCode)
            }

            val config = context.resources.configuration
            Locale.setDefault(locale)
            setLocaleApi24(config, locale)

            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            val newContext = context.createConfigurationContext(config)
            updateAppContext(newContext)

            return true
        } catch (e: Exception) {
            Timber.tag("LocaleManager").e(e, "Failed to update locale")
            return false
        }
    }

    private fun createLocaleFromCode(languageCode: String): Locale {
        return when {
            languageCode == "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            languageCode == "zh-TW" -> Locale.TRADITIONAL_CHINESE
            languageCode == "zh-HK" -> Locale("zh", "HK")

            languageCode in COMPLEX_SCRIPT_LANGUAGES -> {
                if (languageCode.contains("-")) {
                    val (language, country) = languageCode.split("-")
                    Locale.Builder()
                        .setLanguage(language)
                        .setRegion(country)
                        .setScript(getScriptForLanguage(languageCode))
                        .build()
                } else {
                    Locale.Builder()
                        .setLanguage(languageCode)
                        .setScript(getScriptForLanguage(languageCode))
                        .build()
                }
            }

            languageCode.contains("-") -> {
                val (language, country) = languageCode.split("-")
                Locale(language, country)
            }

            else -> Locale(languageCode)
        }
    }

    private fun getScriptForLanguage(languageCode: String): String {
        return when (languageCode) {
            "hi", "mr" -> "Deva" // Devanagari
            "bn" -> "Beng" // Bengali
            "pa" -> "Guru" // Gurmukhi
            "gu" -> "Gujr" // Gujarati
            "ta" -> "Taml" // Tamil
            "te" -> "Telu" // Telugu
            "kn" -> "Knda" // Kannada
            "ml" -> "Mlym" // Malayalam
            "si" -> "Sinh" // Sinhala
            "th" -> "Thai" // Thai
            "ka" -> "Geor" // Georgian
            "am" -> "Ethi" // Ethiopic
            "km" -> "Khmr" // Khmer
            else -> ""
        }
    }

    private fun setLocaleApi24(config: Configuration, locale: Locale) {
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
    }

    @Suppress("DEPRECATION")
    private fun setLocaleLegacy(config: Configuration, locale: Locale) {
        config.locale = locale
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun updateAppContext(newContext: Context) {
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val thread = activityThread.getMethod("currentActivityThread").invoke(null)
            val application = activityThread.getMethod("getApplication").invoke(thread)
            val appContext = application.javaClass.getMethod("getBaseContext").invoke(application)

            val contextImpl = Class.forName("android.app.ContextImpl")
            val implResources = contextImpl.getDeclaredField("mResources")
            implResources.isAccessible = true
            implResources.set(appContext, newContext.resources)
        } catch (e: Exception) {
            Timber.tag("LocaleManager").e(e, "Failed to update app context")
        }
    }
}

// Language mappings
val LanguageCodeToName = mapOf(
    "ar" to "العربية",
    "en" to "English",
    "fr" to "Français",
    "es" to "Español (España)",
    "it" to "Italiano",
    "de" to "Deutsch",
    "nl" to "Nederlands",
    "pt-PT" to "Português",
    "pt" to "Português (Brasil)",
    "ru" to "Русский",
    "tr" to "Türkçe",
    "id" to "Bahasa Indonesia",
    "ur" to "اردو",
    "fa" to "فارسی",
    "ne" to "नेपाली",
    "mr" to "मराठी",
    "hi" to "हिन्दी",
    "bn" to "বাংলা",
    "pa" to "ਪੰਜਾਬੀ",
    "gu" to "ગુજરાતી",
    "ta" to "தமிழ்",
    "te" to "తెలుగు",
    "kn" to "ಕನ್ನಡ",
    "ml" to "മലയാളം",
    "si" to "සිංහල",
    "th" to "ภาษาไทย",
    "lo" to "ລາວ",
    "my" to "ဗမာ",
    "ka" to "ქართული",
    "am" to "አማርኛ",
    "km" to "ខ្មែរ",
    "zh-CN" to "中文 (简体)",
    "zh-TW" to "中文 (繁體)",
    "zh-HK" to "中文 (香港)",
    "ja" to "日本語",
    "ko" to "한국어",
)