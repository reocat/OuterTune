package com.dd3boh.outertune.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.dd3boh.outertune.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object ComposeToImage {

    suspend fun createLyricsImage(
        context: Context,
        coverArtUrl: String?,
        songTitle: String,
        artistName: String,
        lyrics: String,
        width: Int,
        height: Int,
        backgroundColor: Int? = null,
        textColor: Int? = null,
        secondaryTextColor: Int? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        val cardSize = min(width, height)
        val bitmap = createBitmap(cardSize, cardSize)
        val canvas = Canvas(bitmap)

        val defaultBackgroundColor = 0xFF0A0A0A.toInt()
        val defaultTextColor = 0xFFF8FAFC.toInt()
        val defaultSecondaryTextColor = 0xFFCBD5E1.toInt()

        val bgColor = backgroundColor ?: defaultBackgroundColor
        val mainTextColor = textColor ?: defaultTextColor
        val secondaryTxtColor = secondaryTextColor ?: defaultSecondaryTextColor

        val gradientPaint = Paint().apply {
            shader = RadialGradient(
                cardSize / 2f, cardSize / 2f, cardSize * 0.8f,
                intArrayOf(bgColor, ColorUtils.blendARGB(bgColor, 0xFF1A1A1A.toInt(), 0.7f)),
                null,
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
        }

        val cornerRadius = 24f
        val backgroundRect = RectF(0f, 0f, cardSize.toFloat(), cardSize.toFloat())
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, gradientPaint)

        val borderPaint = Paint().apply {
            color = ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 25)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, borderPaint)

        var coverArtBitmap: Bitmap? = null
        if (coverArtUrl != null) {
            try {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(coverArtUrl)
                    .size(320)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                coverArtBitmap = result.image?.toBitmap(320, 320, Bitmap.Config.ARGB_8888)
            } catch (_: Exception) {}
        }

        val padding = 24f
        val imageCornerRadius = 14f
        val coverArtSize = cardSize * 0.14f

        coverArtBitmap?.let { bitmap ->
            val shadowPaint = Paint().apply {
                color = 0x40000000
                maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
                isAntiAlias = true
            }
            val shadowRect = RectF(
                padding + 4f, padding + 4f,
                padding + coverArtSize + 4f, padding + coverArtSize + 4f
            )
            canvas.drawRoundRect(shadowRect, imageCornerRadius, imageCornerRadius, shadowPaint)

            val rect = RectF(padding, padding, padding + coverArtSize, padding + coverArtSize)
            val path = Path().apply {
                addRoundRect(rect, imageCornerRadius, imageCornerRadius, Path.Direction.CW)
            }
            canvas.withClip(path) {
                drawBitmap(bitmap, null, rect, null)
            }

            val borderGradient = Paint().apply {
                shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    intArrayOf(
                        ColorUtils.setAlphaComponent(secondaryTxtColor, 150),
                        ColorUtils.setAlphaComponent(secondaryTxtColor, 80)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawRoundRect(rect, imageCornerRadius, imageCornerRadius, borderGradient)
        }

        val titlePaint = TextPaint().apply {
            color = mainTextColor
            textSize = cardSize * 0.04f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(2f, 0f, 2f, ColorUtils.setAlphaComponent(0xFF000000.toInt(), 60))
        }

        val artistPaint = TextPaint().apply {
            color = secondaryTxtColor
            textSize = cardSize * 0.03f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isAntiAlias = true
            letterSpacing = 0.02f
        }

        val textMaxWidth = (cardSize - (padding * 2 + coverArtSize + 20f)).toInt()
        val textStartX = padding + coverArtSize + 20f

        val titleLayout =
            StaticLayout.Builder.obtain(songTitle, 0, songTitle.length, titlePaint, textMaxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(2)
                .setLineSpacing(4f, 1.2f)
                .build()

        val artistLayout =
            StaticLayout.Builder.obtain(artistName, 0, artistName.length, artistPaint, textMaxWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(1)
                .build()

        val imageCenter = padding + coverArtSize / 2f
        val textBlockHeight = titleLayout.height + artistLayout.height + 12f
        val textBlockY = imageCenter - textBlockHeight / 2f

        canvas.withTranslation(textStartX, textBlockY) {
            titleLayout.draw(this)
            translate(0f, titleLayout.height.toFloat() + 12f)
            artistLayout.draw(this)
        }

        val lyricsPaint = TextPaint().apply {
            color = mainTextColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(2f, 0f, 2f, ColorUtils.setAlphaComponent(0xFF000000.toInt(), 40))
            letterSpacing = 0.02f
        }

        val cleanLyrics = lyrics.trim()
        val lineCount = cleanLyrics.split('\n').size
        val avgLineLength = cleanLyrics.length / max(1, lineCount)

        var lyricsTextSize = when {
            lineCount <= 1 && avgLineLength <= 20 -> cardSize * 0.1f
            lineCount <= 2 && avgLineLength <= 30 -> cardSize * 0.09f
            lineCount <= 3 && avgLineLength <= 40 -> cardSize * 0.08f
            lineCount <= 5 -> cardSize * 0.065f
            lineCount <= 8 -> cardSize * 0.05f
            else -> cardSize * 0.04f
        }

        var lyricsLayout: StaticLayout
        val lyricsMaxWidth = (cardSize * 0.9f).toInt()
        val headerHeight = padding + coverArtSize + 32f
        val footerHeight = 100f
        val availableHeight = cardSize - headerHeight - footerHeight
        val maxTextSize = cardSize * 0.12f
        val minTextSize = cardSize * 0.03f

        do {
            lyricsPaint.textSize = lyricsTextSize
            lyricsLayout =
                StaticLayout.Builder.obtain(cleanLyrics, 0, cleanLyrics.length, lyricsPaint, lyricsMaxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .setLineSpacing(6f, 1.3f)
                    .setMaxLines(10)
                    .build()

            if (lyricsLayout.height > availableHeight) {
                lyricsTextSize = max(lyricsTextSize * 0.9f, minTextSize)
            } else break
        } while (lyricsTextSize > minTextSize)

        val lyricsStartY = headerHeight + (availableHeight - lyricsLayout.height) / 2f
        val lyricsStartX = (cardSize - lyricsLayout.width) / 2f

        canvas.withTranslation(lyricsStartX, lyricsStartY) {
            lyricsLayout.draw(this)
        }

        AppLogo(context, canvas, cardSize, padding, secondaryTxtColor)

        return@withContext bitmap
    }

    private fun AppLogo(
        context: Context,
        canvas: Canvas,
        cardSize: Int,
        padding: Float,
        secondaryTxtColor: Int,
    ) {
        val circleRadius = (cardSize * 0.03f)
        val logoSize = (circleRadius * 1.2f).toInt()

        val rawLogo = context.getDrawable(R.drawable.small_icon)?.toBitmap(logoSize, logoSize)
        val logo = rawLogo?.let { source ->
            val colored = createBitmap(source.width, source.height)
            val canvasLogo = Canvas(colored)
            val paint = Paint().apply {
                colorFilter = PorterDuffColorFilter(0xFFFFFFFF.toInt(), PorterDuff.Mode.SRC_IN)
                isAntiAlias = true
            }
            canvasLogo.drawBitmap(source, 0f, 0f, paint)
            colored
        }

        val appName = context.getString(R.string.app_name)
        val appNamePaint = TextPaint().apply {
            color = secondaryTxtColor
            textSize = cardSize * 0.030f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isAntiAlias = true
            letterSpacing = 0.02f
        }

        val circleX = padding + circleRadius
        val circleY = cardSize - padding - circleRadius

        val logoX = circleX - logoSize / 2f
        val logoY = circleY - logoSize / 2f

        val textX = padding + circleRadius * 2 + 16f
        val textY = circleY + appNamePaint.textSize * 0.3f

        val circlePaint = Paint().apply {
            shader = LinearGradient(
                circleX - circleRadius, circleY - circleRadius,
                circleX + circleRadius, circleY + circleRadius,
                intArrayOf(secondaryTxtColor, ColorUtils.blendARGB(secondaryTxtColor, 0xFF000000.toInt(), 0.2f)),
                null,
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val shadowPaint = Paint().apply {
            color = 0x30000000
            maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)
            isAntiAlias = true
        }

        canvas.drawCircle(circleX + 2f, circleY + 2f, circleRadius, shadowPaint)
        canvas.drawCircle(circleX, circleY, circleRadius, circlePaint)
        logo?.let {
            canvas.drawBitmap(it, logoX, logoY, null)
        }
        canvas.drawText(appName, textX, textY, appNamePaint)
    }
    fun saveBitmapAsFile(context: Context, bitmap: Bitmap, fileName: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/OuterTune")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw IllegalStateException("Failed to create new MediaStore record")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            uri
        } else {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val imageFile = File(cachePath, "$fileName.png")
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.FileProvider",
                imageFile
            )
        }
    }
}