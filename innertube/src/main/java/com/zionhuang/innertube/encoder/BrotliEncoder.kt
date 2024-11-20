package com.zionhuang.innertube.encoder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.brotli.dec.BrotliInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream

interface ContentEncoder {
    val name: String
    suspend fun decode(source: InputStream): InputStream
    suspend fun encode(source: InputStream): InputStream
}

object BrotliEncoder : ContentEncoder {
    override val name: String = "br"

    override suspend fun decode(source: InputStream): InputStream = withContext(Dispatchers.IO) {
        BrotliInputStream(source)
    }

    override suspend fun encode(source: InputStream): InputStream {
        throw UnsupportedOperationException("Encode not implemented by the library yet.")
    }
}

object GzipEncoder : ContentEncoder {
    override val name: String = "gzip"

    override suspend fun decode(source: InputStream): InputStream {
        throw UnsupportedOperationException("Gzip decoding not implemented.")
    }

    override suspend fun encode(source: InputStream): InputStream = withContext(Dispatchers.IO) {
        val outputStream = ByteBufferOutputStream()
        GZIPOutputStream(outputStream).use { it.write(source.readBytes()) }
        outputStream.toInputStream()
    }
}

object DeflateEncoder : ContentEncoder {
    override val name: String = "deflate"

    override suspend fun decode(source: InputStream): InputStream {
        throw UnsupportedOperationException("Deflate decoding not implemented.")
    }

    override suspend fun encode(source: InputStream): InputStream = withContext(Dispatchers.IO) {
        val outputStream = ByteBufferOutputStream()
        DeflaterOutputStream(outputStream).use { it.write(source.readBytes()) }
        outputStream.toInputStream()
    }
}

class ContentEncodingConfig {
    private val encoders: MutableMap<String, ContentEncoder> = mutableMapOf()

    fun customEncoder(encoder: ContentEncoder, quality: Float? = null) {
        encoders[encoder.name] = encoder
    }

    fun getEncoder(name: String): ContentEncoder? = encoders[name]
}

fun ContentEncodingConfig.brotli(quality: Float? = null) {
    customEncoder(BrotliEncoder, quality)
}

fun ContentEncodingConfig.gzip(quality: Float? = null) {
    customEncoder(GzipEncoder, quality)
}

fun ContentEncodingConfig.deflate(quality: Float? = null) {
    customEncoder(DeflateEncoder, quality)
}

class ByteBufferOutputStream : java.io.OutputStream() {
    private val buffer: ByteBuffer = ByteBuffer.allocate(1024 * 1024) // 1 MB buffer

    override fun write(b: Int) {
        buffer.put(b.toByte())
    }

    fun toInputStream(): InputStream {
        buffer.flip()
        return ByteBufferInputStream(buffer)
    }
}

class ByteBufferInputStream(private val buffer: ByteBuffer) : InputStream() {
    override fun read(): Int {
        return if (buffer.hasRemaining()) {
            buffer.get().toInt() and 0xFF
        } else {
            -1
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (!buffer.hasRemaining()) return -1
        val bytesRead = Math.min(len, buffer.remaining())
        buffer.get(b, off, bytesRead)
        return bytesRead
    }
}
