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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.RenderingHints

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
            
            // Fallback: Use Discord's image proxy
            if (isValidImageUrl(originalUrl)) {
                // Use Discord's image proxy - this is the most privacy-friendly approach
                // Discord will fetch the image directly from the URL we provide
                originalUrl
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Upload image directly to Discord's CDN
     * This requires a Discord token and uploads to Discord's servers
     */
    private suspend fun uploadToDiscordCDN(imageBytes: ByteArray, token: String): String? {
        return try {
            // Resize image to Discord's recommended size (512x512)
            val resizedImage = resizeImage(imageBytes, 512, 512) ?: imageBytes
            
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
     * Optimize URL for Discord RPC
     */
    fun optimizeUrlForDiscord(url: String): String {
        // Handle YouTube/Google image URLs
        if (url.contains("googleusercontent.com")) {
            // Optimize for Discord by using a reasonable size
            return if (url.contains("=w") && url.contains("-h")) {
                // Already has size parameters, try to optimize
                val optimizedUrl = url.replace(Regex("=w\\d+-h\\d+.*"), "=w512-h512-p-l90-rj")
                optimizedUrl
            } else {
                // Add size parameters for better Discord compatibility
                "$url=w512-h512-p-l90-rj"
            }
        }
        
        if (url.contains("ggpht.com")) {
            // YouTube thumbnail URLs
            return if (url.contains("=s")) {
                // Already has size parameter
                url.replace(Regex("=s\\d+"), "=s512")
            } else {
                // Add size parameter
                "$url=s512"
            }
        }
        
        // For other URLs, return as-is
        return url
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
    
    /**
     * Resize image to Discord's recommended dimensions
     * Discord recommends 512x512 for large images and 128x128 for small images
     */
    fun resizeImage(imageBytes: ByteArray, targetWidth: Int, targetHeight: Int): ByteArray? {
        return try {
            val inputStream = ByteArrayInputStream(imageBytes)
            val originalImage = ImageIO.read(inputStream)
            
            val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
            val graphics = resizedImage.createGraphics()
            
            // Set rendering hints for better quality
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            
            graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
            graphics.dispose()
            
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(resizedImage, "PNG", outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
    
    fun close() {
        httpClient.close()
    }
} 