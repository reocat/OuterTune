package com.dd3boh.outertune.extensions

import android.content.Context
import com.dd3boh.outertune.constants.InnerTubeCookieKey
import com.dd3boh.outertune.constants.YtmSyncKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.zionhuang.innertube.utils.parseCookieString
import kotlinx.coroutines.runBlocking

fun Context.isSyncEnabled(): Boolean =
    runBlocking {
        val ytmSync = dataStore[YtmSyncKey] ?: true
        val cookie = dataStore[InnerTubeCookieKey] ?: ""
        ytmSync && "SAPISID" in parseCookieString(cookie)
    }
