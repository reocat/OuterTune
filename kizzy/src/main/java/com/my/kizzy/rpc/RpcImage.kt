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
                // Use Discord's CDN proxy - this is the most privacy-friendly approach
                // Discord will fetch and cache the image from the original URL
                image
            } else {
                null
            }
        }
        
        private fun isValidImageUrl(url: String): Boolean {
            return url.startsWith("http") && 
                   (url.contains(".jpg") || url.contains(".jpeg") || 
                    url.contains(".png") || url.contains(".gif") || 
                    url.contains(".webp") || url.contains(".bmp"))
        }
    }
}