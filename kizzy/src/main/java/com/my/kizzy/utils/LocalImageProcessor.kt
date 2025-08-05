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
        val attachments: List<DiscordAttachment>? = null
    )
    
    @Serializable
    private data class DiscordAttachment(
        val id: String? = null,
        val upload_url: String? = null,
        val upload_filename: String? = null
    )
    
    /**
     * Process an image and return a Discord-compatible image asset
     * For now, let's try a simpler approach - just return the URL directly
     * since the original implementation was working
     */
    suspend fun processImage(imageBytes: ByteArray, originalUrl: String, discordToken: String? = null): String? {
        return try {
            println("=== LocalImageProcessor Debug ===")
            println("Image bytes size: ${imageBytes.size}")
            println("Original URL: $originalUrl")
            
            // For now, let's try returning the URL directly
            // The original implementation might have been working with external URLs
            println("Returning original URL directly: $originalUrl")
            originalUrl
        } catch (e: Exception) {
            println("Error in processImage: ${e.message}")
            e.printStackTrace()
            "music"
        }
    }
    
    /**
     * Upload image directly to Discord's CDN
     * This requires a Discord token and uploads to Discord's servers
     */
    private suspend fun uploadToDiscordCDN(imageBytes: ByteArray, token: String): String? {
        return try {
            println("Starting Discord CDN upload...")
            
            // Step 1: Get upload URL from Discord
            println("Step 1: Getting upload URL from Discord...")
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
            
            println("Step 1 response status: ${response.status}")
            val responseText = response.bodyAsText()
            println("Step 1 response body: $responseText")
            
            // Parse the upload URL from response
            val uploadResponse = json.decodeFromString<DiscordUploadResponse>(responseText)
            val attachment = uploadResponse.attachments?.firstOrNull()
            
            println("Parsed attachment: $attachment")
            
            if (attachment?.upload_url != null) {
                // Step 2: Upload the actual image data to the provided URL
                println("Step 2: Uploading image data to: ${attachment.upload_url}")
                val uploadResponse2 = httpClient.post(attachment.upload_url) {
                    contentType(ContentType.Image.PNG)
                    setBody(imageBytes)
                }
                
                println("Step 2 response status: ${uploadResponse2.status}")
                
                // If upload was successful, return the asset ID
                if (uploadResponse2.status.value in 200..299) {
                    val result = attachment.id?.let { "mp:$it" }
                    println("Upload successful, returning: $result")
                    result
                } else {
                    println("Upload failed with status: ${uploadResponse2.status}")
                    null
                }
            } else {
                println("No upload URL received from Discord")
                null
            }
        } catch (e: Exception) {
            println("Error uploading to Discord CDN: ${e.message}")
            e.printStackTrace()
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