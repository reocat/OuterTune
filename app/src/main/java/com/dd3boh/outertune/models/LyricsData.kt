package com.dd3boh.outertune.models

import org.akanework.gramophone.logic.utils.SemanticLyrics

/**
 * Wrapper that couples the retrieved [SemanticLyrics] with the name of the provider
 * that delivered them.
 */
data class LyricsData(
    val lyrics: SemanticLyrics,
    val providerName: String,
) 