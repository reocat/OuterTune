package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.MusixmatchCookieKey
import com.dd3boh.outertune.constants.MusixmatchUserTokenKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.maxrave.lyricsproviders.LyricsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LyricsClientModule {

    @Provides
    @Singleton
    fun provideLyricsClient(@ApplicationContext context: Context): LyricsClient {
        val client = LyricsClient(context.cacheDir)
        runBlocking {
            client.musixmatchUserToken = context.dataStore[MusixmatchUserTokenKey]
            client.musixmatchCookie = context.dataStore[MusixmatchCookieKey]
        }
        return client
    }

    @Provides
    @Singleton
    fun provideKuGouLyricsProvider(): KuGouLyricsProvider {
        return KuGouLyricsProvider
    }

    @Provides
    @Singleton
    fun provideYouTubeLyricsProvider(): YouTubeLyricsProvider {
        return YouTubeLyricsProvider
    }

    @Provides
    @Singleton
    fun provideYouTubeSubtitleLyricsProvider(): YouTubeSubtitleLyricsProvider {
        return YouTubeSubtitleLyricsProvider
    }

    @Provides
    @Singleton
    fun provideLocalLyricsProvider(): LocalLyricsProvider {
        return LocalLyricsProvider
    }
}