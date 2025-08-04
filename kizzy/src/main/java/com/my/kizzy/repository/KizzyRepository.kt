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
            // Download the image
            val response = httpClient.get(url)
            val imageBytes = response.bodyAsChannel().toInputStream().use { input ->
                val output = ByteArrayOutputStream()
                input.copyTo(output)
                output.toByteArray()
            }
            
            // Process the image locally
            imageProcessor.processImage(imageBytes, url, discordToken)
        } catch (e: Exception) {
            null
        }
    }
    
    fun close() {
        httpClient.close()
        imageProcessor.close()
    }
}