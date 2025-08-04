/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KizzyRepositoryImpl.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.repository

import com.my.kizzy.utils.LocalImageProcessor
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.ByteArrayOutputStream

/**
 * Modified by Zion Huang
 * Updated for local image processing by Assistant
 */
class KizzyRepository(private val discordToken: String? = null) {
    private val httpClient = HttpClient()
    private val imageProcessor = LocalImageProcessor()
    
    suspend fun getImage(url: String): String? {
        return try {
            // Optimize the URL for Discord first
            val optimizedUrl = imageProcessor.optimizeUrlForDiscord(url)
            
            // For privacy, we'll use Discord's image proxy instead of downloading
            // Discord will fetch the image directly from the optimized URL
            if (imageProcessor.isValidImageUrl(optimizedUrl)) {
                optimizedUrl
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun close() {
        httpClient.close()
        imageProcessor.close()
    }
}