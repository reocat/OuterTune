package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.constants.LYRIC_FETCH_TIMEOUT
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.lyrics.LyricsHelper
import com.dd3boh.outertune.lyrics.LyricsResult
import com.dd3boh.outertune.models.MediaMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.akanework.gramophone.logic.utils.SemanticLyrics
import com.dd3boh.outertune.models.LyricsData
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LyricsMenuViewModel @Inject constructor(
    private val lyricsHelper: LyricsHelper,
    val database: MusicDatabase,
) : ViewModel() {
    private var job: Job? = null

    private val _results = MutableStateFlow<List<LyricsResult>>(emptyList())
    val results = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun search(mediaId: String, title: String, artist: String, duration: Int) {
        _isLoading.value = true
        _results.value = emptyList()
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                withTimeoutOrNull(LYRIC_FETCH_TIMEOUT) {
                    lyricsHelper.getAllLyrics(mediaId, title, artist, duration) { result ->
                        _results.update { currentResults ->
                            currentResults + result
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching lyrics")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelSearch() {
        job?.cancel()
        job = null
    }

    fun refetchLyrics(mediaMetadata: MediaMetadata, onDone: (SemanticLyrics?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            database.query { deleteLyricById(mediaMetadata.id) }
            withTimeoutOrNull(LYRIC_FETCH_TIMEOUT) {
                val lyricsData: LyricsData? = lyricsHelper.getLyrics(mediaMetadata)
                onDone(lyricsData?.lyrics)
            }
        }
    }
}