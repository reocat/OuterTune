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
            // Use the repository to download and upload the image to Discord's CDN
            return repository.getImage(image)
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