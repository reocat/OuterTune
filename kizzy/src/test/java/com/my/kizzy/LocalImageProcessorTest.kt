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
    fun testBase64Encoding() {
        val processor = LocalImageProcessor()
        val testData = "Hello, World!".toByteArray()
        
        val base64 = processor.processImageToBase64(testData, "text/plain")
        assertNotNull(base64)
        assertTrue(base64!!.startsWith("data:text/plain;base64,"))
    }
} 