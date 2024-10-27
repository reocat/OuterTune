package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.utils.reportException
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.pages.MoodAndGenres
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodAndGenresViewModel
    @Inject
    constructor() : ViewModel() {
        val moodAndGenres = MutableStateFlow<List<MoodAndGenres>?>(null)

        init {
            viewModelScope.launch {
                YouTube
                    .moodAndGenres()
                    .onSuccess {
                        moodAndGenres.value = it
                    }.onFailure {
                        reportException(it)
                    }
            }
        }
    }
