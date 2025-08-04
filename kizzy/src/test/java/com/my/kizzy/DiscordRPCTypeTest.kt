package com.my.kizzy

import com.my.kizzy.rpc.RpcImage
import org.junit.Test
import org.junit.Assert.*

class DiscordRPCTypeTest {
    
    @Test
    fun testNullableThumbnailUrlHandling() {
        // Test with non-null URL
        val validUrl = "https://example.com/image.jpg"
        val externalImage = RpcImage.ExternalImage(validUrl)
        assertNotNull(externalImage)
        
        // Test with empty string (should be handled gracefully)
        val emptyUrl = ""
        val emptyImage = RpcImage.ExternalImage(emptyUrl)
        assertNotNull(emptyImage)
        
        // Test fallback image
        val fallbackImage = RpcImage.FallbackImage
        assertNotNull(fallbackImage)
    }
    
    @Test
    fun testUrlOptimization() {
        val externalImage = RpcImage.ExternalImage("https://lh3.googleusercontent.com/abc123=w120-h120")
        assertNotNull(externalImage)
    }
    
    @Test
    fun testYouTubeUrlOptimization() {
        // Test with a realistic YouTube thumbnail URL
        val youtubeUrl = "https://lh3.googleusercontent.com/abc123=w120-h120-p-l90-rj"
        val externalImage = RpcImage.ExternalImage(youtubeUrl)
        assertNotNull(externalImage)
        
        // Test with YouTube ggpht URL
        val ggphtUrl = "https://yt3.ggpht.com/abc123=s88"
        val ggphtImage = RpcImage.ExternalImage(ggphtUrl)
        assertNotNull(ggphtImage)
    }
} 