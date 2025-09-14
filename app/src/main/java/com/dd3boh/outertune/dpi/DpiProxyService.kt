package com.dd3boh.outertune.dpi

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dd3boh.outertune.constants.DpiBypassCmdArgsKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class DpiProxyService : Service() {
    private var proxyJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val isRunning = AtomicBoolean(false)

    companion object {
        private val TAG: String = DpiProxyService::class.java.simpleName
        const val ACTION_START = "com.dd3boh.outertune.dpi.START"
        const val ACTION_STOP = "com.dd3boh.outertune.dpi.STOP"
        const val ACTION_RESTART = "com.dd3boh.outertune.dpi.RESTART"

        init {
            System.loadLibrary("byedpi")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("DPI Proxy Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return when (val action = intent?.action) {
            ACTION_START -> {
                startProxy()
                START_STICKY
            }
            ACTION_STOP -> {
                stopProxy()
                START_NOT_STICKY
            }
            ACTION_RESTART -> {
                restartProxy()
                START_STICKY
            }
            else -> {
                Timber.tag(TAG).w("Unknown action: $action")
                START_NOT_STICKY
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startProxy() {
        Timber.tag(TAG).i("Starting DPI proxy")

        if (!isRunning.compareAndSet(false, true)) {
            Timber.tag(TAG).w("Proxy already running")
            return
        }

        proxyJob = serviceScope.launch {
            try {
                val args = buildCmdArgs()

                Timber.tag(TAG).i("Starting proxy with args: ${args.joinToString(" ")}")
                val code = jniStartProxy(args)
                delay(500)

                if (code != 0) {
                    Timber.tag(TAG).e("Proxy stopped with code $code")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to start proxy")
            } finally {
                isRunning.set(false)
                stopSelf()
            }
        }
    }

    private fun buildCmdArgs(): Array<String> {
        val ip = "127.0.0.1"
        val port = "1081"
        val args = applicationContext.dataStore[DpiBypassCmdArgsKey] ?: "-d1 -d3+s -s6+s -d9+s -s12+s -d15+s -s20+s -d25+s -s30+s -d35+s -r1+s -S -a1 -As -d1 -d3+s -s6+s -d9+s -s12+s -d15+s -s20+s -d25+s -s30+s -d35+s -S -a1"

        val cleanArgs = args.substringAfter("-").trim()
        val prefix = "--ip $ip --port $port"
        return arrayOf("ciadpi") + shellSplit("$prefix $cleanArgs")
    }

    private fun shellSplit(cmd: String): Array<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var escapeNext = false

        for (char in cmd) {
            when {
                escapeNext -> {
                    current.append(char)
                    escapeNext = false
                }
                char == '\\' -> escapeNext = true
                char == '"' -> inQuotes = !inQuotes
                char == ' ' && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(char)
            }
        }

        if (current.isNotEmpty()) {
            result.add(current.toString())
        }

        return result.distinct().toTypedArray()
    }

    private fun stopProxy(shouldStopSelf: Boolean = true) {
        Timber.tag(TAG).i("Stopping proxy")

        if (!isRunning.compareAndSet(true, false)) {
            Timber.tag(TAG).w("Proxy not running or already stopping")
            return
        }

        serviceScope.launch {
            try {
                jniStopProxy()
                withTimeoutOrNull(1000L) {
                    proxyJob?.cancel()
                    proxyJob?.join()
                }

                if (proxyJob?.isActive == true) {
                    Timber.tag(TAG).w("Proxy job did not cancel in time, forcing close")
                    jniForceClose()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Exception while stopping proxy")
            } finally {
                proxyJob = null
                Timber.tag(TAG).i("Proxy stopped")
                if (shouldStopSelf) {
                    stopSelf()
                }
            }
        }
    }

    private fun restartProxy() {
        serviceScope.launch {
            stopProxy(shouldStopSelf = false)
            proxyJob?.join()
            startProxy()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(TAG).d("DPI Proxy Service destroyed")
        serviceScope.cancel()
    }

    private external fun jniStartProxy(args: Array<String>): Int
    private external fun jniStopProxy(): Int
    private external fun jniForceClose(): Int
}