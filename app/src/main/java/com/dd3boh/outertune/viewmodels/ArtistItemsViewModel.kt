package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.models.ItemsPage
import com.dd3boh.outertune.utils.reportException
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.YTItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val browseId = savedStateHandle.get<String>("browseId")!!
    private val params = savedStateHandle.get<String>("params")

    val title = MutableStateFlow("")
    val itemsPage = MutableStateFlow<ItemsPage?>(null)

    private fun sortByYearIfAlbumItems(items: List<YTItem>): List<YTItem> {
        return if (items.isNotEmpty() && items.all { it is AlbumItem }) {
            items.sortedWith(compareByDescending<YTItem> {
                (it as AlbumItem).year ?: Int.MIN_VALUE
            })
        } else {
            items
        }
    }

    init {
        viewModelScope.launch {
            YouTube.artistItems(
                BrowseEndpoint(
                    browseId = browseId,
                    params = params
                )
            ).onSuccess { artistItemsPage ->
                title.value = artistItemsPage.title
                val sorted = sortByYearIfAlbumItems(artistItemsPage.items)
                itemsPage.value = ItemsPage(
                    items = sorted.distinctBy { it.id },
                    continuation = artistItemsPage.continuation
                )
            }.onFailure {
                reportException(it)
            }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val oldItemsPage = itemsPage.value ?: return@launch
            val continuation = oldItemsPage.continuation ?: return@launch
            YouTube.artistItemsContinuation(continuation)
                .onSuccess { artistItemsContinuationPage ->
                    val combined = (oldItemsPage.items + artistItemsContinuationPage.items).distinctBy { it.id }
                    itemsPage.update {
                        ItemsPage(
                            items = sortByYearIfAlbumItems(combined),
                            continuation = artistItemsContinuationPage.continuation
                        )
                    }
                }.onFailure {
                    reportException(it)
                }
        }
    }
}
