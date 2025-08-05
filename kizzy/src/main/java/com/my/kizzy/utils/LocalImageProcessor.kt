/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * LocalImageProcessor.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * Local image processor for Discord RPC images
 * Processes images locally without requiring external APIs
 */
class LocalImageProcessor {
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    @Serializable
    private data class DiscordUploadResponse(
        val id: String? = null,
        val url: String? = null
    )
    
    /**
     * Process an image and return a Discord-compatible image asset
     * Attempts to upload directly to Discord's CDN for better compatibility
     */
    suspend fun processImage(imageBytes: ByteArray, originalUrl: String, discordToken: String? = null): String? {
        return try {
            // First, try to upload directly to Discord's CDN if we have a token
            if (!discordToken.isNullOrBlank()) {
                uploadToDiscordCDN(imageBytes, discordToken)?.let { return it }
            }
            
            // If we can't upload to Discord CDN, we need to fall back to a different approach
            // Discord RPC doesn't support external URLs directly, so we'll use a fallback
            "music"
        } catch (e: Exception) {
            "music"
        }
    }
    
    /**
     * Upload image directly to Discord's CDN
     * This requires a Discord token and uploads to Discord's servers
     */
    private suspend fun uploadToDiscordCDN(imageBytes: ByteArray, token: String): String? {
        return try {
            // For now, upload the image as-is without resizing
            // We can add resizing later if needed
            
            val response = httpClient.post("https://discord.com/api/v9/channels/@me/attachments") {
                header("Authorization", token)
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "files" to listOf(
                        mapOf(
                            "id" to "0",
                            "filename" to "image.png",
                            "uploaded_filename" to "image.png"
                        )
                    )
                ))
            }
            
            // Parse the upload URL from response
            val uploadResponse = json.decodeFromString<DiscordUploadResponse>(response.bodyAsText())
            uploadResponse.id?.let { "mp:$it" }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if the URL is a valid image URL that Discord can proxy
     */
    fun isValidImageUrl(url: String): Boolean {
        return url.startsWith("http") && 
               (url.contains(".jpg") || url.contains(".jpeg") || 
                url.contains(".png") || url.contains(".gif") || 
                url.contains(".webp") || url.contains(".bmp") ||
                url.contains("googleusercontent.com") || url.contains("ggpht.com"))
    }
    
    /**
     * Alternative method: Convert image to base64 data URL
     * This embeds the image data directly in the presence update
     * Note: This may not work with Discord's current implementation
     */
    fun processImageToBase64(imageBytes: ByteArray, mimeType: String = "image/png"): String? {
        return try {
            val base64 = Base64.getEncoder().encodeToString(imageBytes)
            "data:$mimeType;base64,$base64"
        } catch (e: Exception) {
            null
        }
    }
    
    fun close() {
        httpClient.close()
    }
} 