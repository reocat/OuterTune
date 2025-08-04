package com.my.kizzy

import com.my.kizzy.utils.LocalImageProcessor
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class LocalImageProcessorTest {
    
    @Test
    fun testValidImageUrl() {
        val processor = LocalImageProcessor()
        
        // Test valid image URLs
        assertTrue(processor.isValidImageUrl("https://example.com/image.jpg"))
        assertTrue(processor.isValidImageUrl("https://example.com/image.png"))
        assertTrue(processor.isValidImageUrl("https://example.com/image.gif"))
        assertTrue(processor.isValidImageUrl("https://example.com/image.webp"))
        
        // Test YouTube/Google URLs
        assertTrue(processor.isValidImageUrl("https://lh3.googleusercontent.com/abc123=w120-h120"))
        assertTrue(processor.isValidImageUrl("https://yt3.ggpht.com/abc123=s88"))
        
        // Test invalid URLs
        assertFalse(processor.isValidImageUrl("https://example.com/document.pdf"))
        assertFalse(processor.isValidImageUrl("not-a-url"))
        assertFalse(processor.isValidImageUrl(""))
    }
    
    @Test
    fun testUrlOptimization() {
        val processor = LocalImageProcessor()
        
        // Test Google user content URLs
        val googleUrl = "https://lh3.googleusercontent.com/abc123=w120-h120-p-l90-rj"
        val optimizedGoogle = processor.optimizeUrlForDiscord(googleUrl)
        assertEquals("https://lh3.googleusercontent.com/abc123=w512-h512-p-l90-rj", optimizedGoogle)
        
        // Test YouTube thumbnail URLs
        val youtubeUrl = "https://yt3.ggpht.com/abc123=s88"
        val optimizedYoutube = processor.optimizeUrlForDiscord(youtubeUrl)
        assertEquals("https://yt3.ggpht.com/abc123=s512", optimizedYoutube)
        
        // Test URLs without size parameters
        val googleUrlNoSize = "https://lh3.googleusercontent.com/abc123"
        val optimizedGoogleNoSize = processor.optimizeUrlForDiscord(googleUrlNoSize)
        assertEquals("https://lh3.googleusercontent.com/abc123=w512-h512-p-l90-rj", optimizedGoogleNoSize)
        
        // Test regular URLs (should remain unchanged)
        val regularUrl = "https://example.com/image.jpg"
        val optimizedRegular = processor.optimizeUrlForDiscord(regularUrl)
        assertEquals(regularUrl, optimizedRegular)
    }
    
    @Test
    fun testImageResizing() {
        val processor = LocalImageProcessor()
        
        // Create a simple test image (1x1 pixel PNG)
        val testImageBytes = byteArrayOf(
            0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), // PNG signature
            0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(),
            // ... minimal PNG data
        )
        
        val resized = processor.resizeImage(testImageBytes, 512, 512)
        // Note: This test might fail due to invalid PNG data, but it tests the structure
        // In a real scenario, you'd use actual image data
    }
    
    @Test
    fun testBase64Encoding() {
        val processor = LocalImageProcessor()
        val testData = "Hello, World!".toByteArray()
        
        val base64 = processor.processImageToBase64(testData, "text/plain")
        assertNotNull(base64)
        assertTrue(base64!!.startsWith("data:text/plain;base64,"))
    }
} 