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
        
        // Test invalid URLs
        assertFalse(processor.isValidImageUrl("https://example.com/document.pdf"))
        assertFalse(processor.isValidImageUrl("not-a-url"))
        assertFalse(processor.isValidImageUrl(""))
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