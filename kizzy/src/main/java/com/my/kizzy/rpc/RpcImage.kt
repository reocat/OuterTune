/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcImage.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.rpc

import com.my.kizzy.repository.KizzyRepository

/**
 * Modified by Zion Huang
 * Updated for local image processing by Assistant
 */
sealed class RpcImage {
    abstract suspend fun resolveImage(repository: KizzyRepository): String?
    
    class DiscordImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: KizzyRepository): String {
            return "mp:${image}"
        }
    }
    
    class ExternalImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: KizzyRepository): String? {
            // For privacy, we'll use Discord's image proxy instead of external services
            // This way Discord fetches the image directly from the URL
            return if (isValidImageUrl(image)) {
                // Try to optimize the URL for Discord
                optimizeUrlForDiscord(image)
            } else {
                // Fallback to a default music icon if the URL is invalid
                "music"
            }
        }
        
        private fun isValidImageUrl(url: String): Boolean {
            return url.startsWith("http") && 
                   (url.contains(".jpg") || url.contains(".jpeg") || 
                    url.contains(".png") || url.contains(".gif") || 
                    url.contains(".webp") || url.contains(".bmp") ||
                    url.contains("googleusercontent.com") || url.contains("ggpht.com"))
        }
        
        private fun optimizeUrlForDiscord(url: String): String {
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
    }
    
    /**
     * Fallback image for when external images can't be loaded
     */
    object FallbackImage : RpcImage() {
        override suspend fun resolveImage(repository: KizzyRepository): String {
            return "music"
        }
    }
}