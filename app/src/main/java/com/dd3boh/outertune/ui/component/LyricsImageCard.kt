package com.dd3boh.outertune.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dd3boh.outertune.R
import com.dd3boh.outertune.models.MediaMetadata
import kotlin.math.max

@Composable
fun rememberAdjustedFontSize(
    text: String,
    maxWidth: Dp,
    maxHeight: Dp,
    density: Density,
    initialFontSize: TextUnit = 20.sp,
    minFontSize: TextUnit = 14.sp,
    style: TextStyle = TextStyle.Default,
    textMeasurer: androidx.compose.ui.text.TextMeasurer? = null,
    maxLines: Int = Int.MAX_VALUE
): TextUnit {
    val measurer = textMeasurer ?: rememberTextMeasurer()

    var calculatedFontSize by remember(text, maxWidth, maxHeight, style, density, maxLines) {

        val lineCount = text.split('\n').size
        val avgLineLength = text.length / max(1, lineCount)

        val initialSize = when {
            lineCount <= 2 && avgLineLength < 30 -> (initialFontSize.value * 1.2f).sp
            lineCount <= 3 && avgLineLength < 40 -> initialFontSize
            lineCount <= 5 && avgLineLength < 50 -> (initialFontSize.value * 0.85f).sp
            lineCount <= 8 -> (initialFontSize.value * 0.7f).sp
            else -> (initialFontSize.value * 0.6f).sp
        }
        mutableStateOf(initialSize)
    }

    LaunchedEffect(text, maxWidth, maxHeight, maxLines) {
        val targetWidthPx = with(density) { maxWidth.toPx() }
        val targetHeightPx = with(density) { maxHeight.toPx() }

        if (text.isBlank()) {
            calculatedFontSize = minFontSize
            return@LaunchedEffect
        }

        var minSize = minFontSize.value
        var maxSize = initialFontSize.value * 1.5f
        var bestFit = minSize
        var iterations = 0

        while (minSize <= maxSize && iterations < 25) {
            iterations++
            val midSize = (minSize + maxSize) / 2
            val midSizeSp = midSize.sp

            val result = measurer.measure(
                text = AnnotatedString(text),
                style = style.copy(
                    fontSize = midSizeSp,
                    lineHeight = (midSize * 1.3f).sp
                ),
                constraints = Constraints(
                    maxWidth = targetWidthPx.toInt(),
                    maxHeight = targetHeightPx.toInt()
                ),
                maxLines = maxLines
            )

            if (result.size.width <= targetWidthPx && result.size.height <= targetHeightPx) {
                bestFit = midSize
                minSize = midSize + 0.3f
            } else {
                maxSize = midSize - 0.3f
            }
        }

        calculatedFontSize = max(bestFit, minFontSize.value).sp
    }

    return calculatedFontSize
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LyricsImageCard(
    lyricText: String,
    mediaMetadata: MediaMetadata,
    darkBackground: Boolean = true,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    secondaryTextColor: Color? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val cardCornerRadius = 24.dp
    val padding = 24.dp
    val coverArtSize = 65.dp

    val backgroundGradient = backgroundColor ?: if (darkBackground) {
        Color(0xFF0A0A0A)
    } else {
        Color(0xFFFAFAFA)
    }

    val mainTextColor = textColor ?: if (darkBackground) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    val secondaryColor = secondaryTextColor ?: if (darkBackground) {
        Color(0xFFCBD5E1)
    } else {
        Color(0xFF64748B)
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(mediaMetadata.thumbnailUrl)
            .crossfade(true)
            .placeholder(R.drawable.music_note)
            .error(R.drawable.music_note)
            .build()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        backgroundGradient,
                        if (darkBackground) Color(0xFF1A1A1A) else Color(0xFFE2E8F0)
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .clip(RoundedCornerShape(cardCornerRadius))
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(cardCornerRadius),
                    ambientColor = if (darkBackground) Color.Black.copy(0.5f) else Color.Gray.copy(0.3f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundGradient,
                            if (darkBackground) Color(0xFF111827) else Color(0xFFF1F5F9)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (darkBackground) Color.White.copy(0.1f) else Color.Black.copy(0.1f),
                    shape = RoundedCornerShape(cardCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(coverArtSize)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            secondaryColor.copy(0.6f),
                                            secondaryColor.copy(0.3f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = mediaMetadata.title,
                            color = mainTextColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = mediaMetadata.artists.joinToString { it.name },
                            color = secondaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val availableWidth = maxWidth
                    val availableHeight = maxHeight

                    val textAlign = when {
                        lyricText.any { it.code in 0x0600..0x06FF } -> TextAlign.End
                        else -> TextAlign.Center
                    }

                    val textStyle = TextStyle(
                        color = mainTextColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = textAlign,
                        letterSpacing = 0.3.sp,
                    )

                    val textMeasurer = rememberTextMeasurer()

                    val cleanText = lyricText.trim()
                    val lineCount = cleanText.split('\n').size
                    val avgLineLength = cleanText.length / max(1, lineCount)

                    val initialSize = when {
                        lineCount <= 1 && avgLineLength <= 20 -> 32.sp
                        lineCount <= 2 && avgLineLength <= 30 -> 28.sp
                        lineCount <= 3 && avgLineLength <= 40 -> 24.sp
                        lineCount <= 5 -> 20.sp
                        lineCount <= 8 -> 18.sp
                        else -> 16.sp
                    }

                    val estimatedLineHeight = with(density) { (initialSize * 1.3f).toPx() }
                    val maxLines = max(3, (maxHeight.value * density.density / estimatedLineHeight).toInt())

                    val dynamicFontSize = rememberAdjustedFontSize(
                        text = cleanText,
                        maxWidth = availableWidth - 16.dp,
                        maxHeight = availableHeight - 16.dp,
                        density = density,
                        initialFontSize = initialSize,
                        minFontSize = 14.sp,
                        style = textStyle,
                        textMeasurer = textMeasurer,
                        maxLines = maxLines
                    )

                    Text(
                        text = cleanText,
                        style = textStyle.copy(
                            fontSize = dynamicFontSize,
                            lineHeight = (dynamicFontSize.value * 1.3f).sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = if (darkBackground) Color.Black.copy(0.3f) else Color.Gray.copy(0.2f),
                                offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = maxLines,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(50)
                            )
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        secondaryColor,
                                        secondaryColor.copy(0.8f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.small_icon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = context.getString(R.string.app_name),
                        color = secondaryColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}