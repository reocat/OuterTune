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
            // Debug logging to understand what's happening
            println("=== Discord RPC Image Debug ===")
            println("Original URL: $url")
            println("Is Valid: ${imageProcessor.isValidImageUrl(url)}")
            
            // Only process valid image URLs
            if (!imageProcessor.isValidImageUrl(url)) {
                println("URL validation failed, returning null")
                return null
            }
            
            // Download the image
            println("Downloading image from: $url")
            val response = httpClient.get(url)
            val imageBytes = response.bodyAsChannel().toInputStream().use { input ->
                val output = ByteArrayOutputStream()
                input.copyTo(output)
                output.toByteArray()
            }
            println("Downloaded ${imageBytes.size} bytes")
            
            // Process the image locally
            val result = imageProcessor.processImage(imageBytes, url, discordToken)
            println("Processed result: $result")
            println("===============================")
            
            result
        } catch (e: Exception) {
            println("Error processing image URL: ${e.message}")
            null
        }
    }
    
    fun close() {
        httpClient.close()
        imageProcessor.close()
    }
}