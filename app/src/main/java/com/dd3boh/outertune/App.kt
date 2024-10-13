package com.dd3boh.outertune

import android.app.Application
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.datastore.preferences.core.edit
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.request.CachePolicy
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeLocale
import com.zionhuang.innertube.utils.parseCookieString
import com.zionhuang.kugou.KuGou
import com.dd3boh.outertune.constants.*
import com.dd3boh.outertune.extensions.*
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.outertune.utils.reportException
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.Proxy
import java.util.*
import kotlin.collections.contains

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        val locale = Locale.getDefault()
        val languageTag = locale.toLanguageTag().replace("-Hant", "") // replace zh-Hant-* to zh-*
        YouTube.locale = YouTubeLocale(
            gl = dataStore[ContentCountryKey]?.takeIf { it != SYSTEM_DEFAULT }
                ?: locale.country.takeIf { it in CountryCodeToName }
                ?: "US",
            hl = dataStore[ContentLanguageKey]?.takeIf { it != SYSTEM_DEFAULT }
                ?: locale.language.takeIf { it in LanguageCodeToName }
                ?: languageTag.takeIf { it in LanguageCodeToName }
                ?: "en"
        )
        if (languageTag == "zh-TW") {
            KuGou.useTraditionalChinese = true
        }

        if (dataStore[ProxyEnabledKey] == true) {
            try {
                YouTube.proxy = Proxy(
                    dataStore[ProxyTypeKey].toEnum(defaultValue = Proxy.Type.HTTP),
                    dataStore[ProxyUrlKey]!!.toInetSocketAddress()
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to parse proxy url.", LENGTH_SHORT).show()
                reportException(e)
            }
        }

        if (dataStore[UseLoginForBrowse] == true) {
            YouTube.useLoginForBrowse = true
        }

        GlobalScope.launch {
            dataStore.data
                .map { it[VisitorDataKey] }
                .distinctUntilChanged()
                .collect { visitorData ->
                    YouTube.visitorData = visitorData
                        ?.takeIf { it != "null" } // Previously visitorData was sometimes saved as "null" due to a bug
                        ?: YouTube.visitorData().getOrNull()?.also { newVisitorData ->
                            dataStore.edit { settings ->
                                settings[VisitorDataKey] = newVisitorData
                            }
                        } ?: YouTube.DEFAULT_VISITOR_DATA
                }
        }
        GlobalScope.launch {
            dataStore.data
                .map { it[InnerTubeCookieKey] }
                .distinctUntilChanged()
                .collect { cookie ->
                    YouTube.cookie = if ("SAPISID" in parseCookieString(cookie?: "")) cookie else null
                }
        }
    }

    override fun newImageLoader(): ImageLoader {
        val cacheSize = dataStore[MaxImageCacheSizeKey]

        // will crash app if you set to 0 after cache starts being used
        if (cacheSize == 0) {
            return ImageLoader.Builder(this)
                .crossfade(true)
                .respectCacheHeaders(false)
                .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()
        }

        return ImageLoader.Builder(this)
        .crossfade(true)
        .respectCacheHeaders(false)
        .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        .diskCache(
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizeBytes((cacheSize ?: 512) * 1024 * 1024L)
                .build()
        )
        .build()
    }
}