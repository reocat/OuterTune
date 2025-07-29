package com.dd3boh.outertune.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/*
 *   Partially reuses code from https://github.com/saket/squiggly-slider; Thanks, nya! :3
 */

@Composable
fun SquigglySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    squigglesSpec: SquigglySlider.SquigglesSpec = SquigglySlider.SquigglesSpec(),
    squigglesAnimator: SquigglySlider.SquigglesAnimator = SquigglySlider.rememberSquigglesAnimator(),
    colors: SquigglySliderColors = SquigglySliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    var isDragging by remember { mutableFloatStateOf(0f) }

    val sliderHeight = (squigglesSpec.amplitude + squigglesSpec.strokeWidth) * 2
    val thumbSize = remember(squigglesSpec.strokeWidth) {
        DpSize(
            width = squigglesSpec.strokeWidth.coerceAtLeast(4.dp),
            height = (squigglesSpec.strokeWidth * 4).coerceAtLeast(16.dp)
        )
    }

    val rangeSize = valueRange.endInclusive - valueRange.start
    val hasValidRange = rangeSize > 0f
    val normalizedValue = remember(value, valueRange, hasValidRange) {
        derivedStateOf {
            if (hasValidRange) {
                ((value - valueRange.start) / rangeSize).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }.value

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sliderHeight.coerceAtLeast(44.dp))
            .pointerInput(enabled, valueRange) {
                if (enabled && hasValidRange) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = 1f
                            val newNormalizedValue = (offset.x / size.width).coerceIn(0f, 1f)
                            onValueChange(valueRange.start + newNormalizedValue * rangeSize)
                        },
                        onDragEnd = {
                            isDragging = 0f
                            onValueChangeFinished?.invoke()
                        }
                    ) { change, _ ->
                        val newNormalizedValue = (change.position.x / size.width).coerceIn(0f, 1f)
                        onValueChange(valueRange.start + newNormalizedValue * rangeSize)
                    }
                }
            }
    ) {
        SquigglySliderTrack(
            normalizedValue = normalizedValue,
            squigglesSpec = squigglesSpec,
            squigglesAnimator = squigglesAnimator,
            interactionSource = interactionSource,
            enabled = enabled,
            isDragging = isDragging > 0f,
            hasValidRange = hasValidRange,
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .height(sliderHeight)
                .align(Alignment.Center)
        )

        SquigglySliderThumb(
            normalizedValue = normalizedValue,
            thumbSize = thumbSize,
            enabled = enabled,
            hasValidRange = hasValidRange,
            colors = colors,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
private fun SquigglySliderTrack(
    normalizedValue: Float,
    squigglesSpec: SquigglySlider.SquigglesSpec,
    squigglesAnimator: SquigglySlider.SquigglesAnimator,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    isDragging: Boolean,
    hasValidRange: Boolean,
    colors: SquigglySliderColors,
    modifier: Modifier = Modifier
) {
    val isDraggedState by interactionSource.collectIsDraggedAsState()
    val isInteracting = isDraggedState || isDragging

    val targetAmplitude = if (isInteracting) {
        squigglesSpec.amplitude * 0.3f
    } else {
        squigglesSpec.amplitude
    }

    val animatedAmplitude by animateDpAsState(
        targetValue = targetAmplitude,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "Squiggles amplitude"
    )

    val activeColor = if (enabled) colors.activeTrackColor else colors.disabledActiveTrackColor
    val inactiveColor = if (enabled) colors.inactiveTrackColor else colors.disabledInactiveTrackColor


    Canvas(modifier = modifier) {
        val strokeWidth = squigglesSpec.strokeWidth.toPx()
        val centerY = size.height * 0.5f
        val trackLength = size.width - strokeWidth
        val strokeWidthHalf = strokeWidth * 0.5f

        if (!hasValidRange) {
            drawLine(
                color = inactiveColor,
                start = Offset(strokeWidthHalf, centerY),
                end = Offset(size.width - strokeWidthHalf, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            return@Canvas
        }

        val activeTrackEnd = strokeWidthHalf + trackLength * normalizedValue

        if (activeTrackEnd < size.width - strokeWidthHalf) {
            drawLine(
                color = inactiveColor,
                start = Offset(activeTrackEnd, centerY),
                end = Offset(size.width - strokeWidthHalf, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        if (normalizedValue > 0f && activeTrackEnd > strokeWidthHalf) {
            drawSquigglyLine(
                startX = strokeWidthHalf,
                endX = activeTrackEnd,
                centerY = centerY,
                color = activeColor,
                strokeWidth = strokeWidth,
                wavelength = squigglesSpec.wavelength.toPx(),
                amplitude = animatedAmplitude.toPx(),
                animationProgress = squigglesAnimator.animationProgress.value
            )
        }
    }
}

@Composable
private fun SquigglySliderThumb(
    normalizedValue: Float,
    thumbSize: DpSize,
    enabled: Boolean,
    hasValidRange: Boolean,
    colors: SquigglySliderColors,
    modifier: Modifier = Modifier
) {
    val thumbColor = if (enabled) colors.thumbColor else colors.disabledThumbColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize.height.coerceAtLeast(20.dp))
    ) {
        val thumbWidthPx = thumbSize.width.toPx()
        val thumbHeightPx = thumbSize.height.toPx()
        val availableWidth = size.width - thumbWidthPx

        val thumbX = if (hasValidRange) {
            availableWidth * normalizedValue
        } else {
            0f
        }

        val thumbY = (size.height - thumbHeightPx) * 0.5f

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(thumbX, thumbY),
            size = Size(thumbWidthPx, thumbHeightPx),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}

private fun DrawScope.drawSquigglyLine(
    startX: Float,
    endX: Float,
    centerY: Float,
    color: Color,
    strokeWidth: Float,
    wavelength: Float,
    amplitude: Float,
    animationProgress: Float
) {
    if (startX >= endX || wavelength <= 0f) return

    val segmentWidth = wavelength * 0.1f
    val numPoints = ceil((endX - startX) / segmentWidth).toInt() + 1

    if (numPoints <= 1) {
        drawLine(
            color = color,
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        return
    }

    val path = Path()
    val twoPi = (2 * PI).toFloat()
    val animationOffset = twoPi * animationProgress

    var currentX = startX
    repeat(numPoints) { i ->
        val proportionOfWavelength = (currentX - startX) / wavelength
        val radians = proportionOfWavelength * twoPi + animationOffset
        val offsetY = centerY + sin(radians) * amplitude

        when (i) {
            0 -> path.moveTo(currentX, offsetY)
            else -> path.lineTo(currentX, offsetY)
        }

        currentX = (currentX + segmentWidth).coerceAtMost(endX)
    }

    if (currentX < endX) {
        val proportionOfWavelength = (endX - startX) / wavelength
        val radians = proportionOfWavelength * twoPi + animationOffset
        val offsetY = centerY + sin(radians) * amplitude
        path.lineTo(endX, offsetY)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(radius = wavelength * 0.125f)
        )
    )
}

@Immutable
data class SquigglySliderColors(
    val thumbColor: Color,
    val activeTrackColor: Color,
    val inactiveTrackColor: Color,
    val disabledThumbColor: Color,
    val disabledActiveTrackColor: Color,
    val disabledInactiveTrackColor: Color
)

object SquigglySliderDefaults {
    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colorScheme.primary,
        activeTrackColor: Color = MaterialTheme.colorScheme.primary,
        inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        disabledThumbColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledActiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledInactiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    ): SquigglySliderColors = SquigglySliderColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        inactiveTrackColor = inactiveTrackColor,
        disabledThumbColor = disabledThumbColor,
        disabledActiveTrackColor = disabledActiveTrackColor,
        disabledInactiveTrackColor = disabledInactiveTrackColor
    )
}

object SquigglySlider {
    @Immutable
    data class SquigglesSpec(
        val strokeWidth: Dp = 4.dp,
        val wavelength: Dp = 24.dp,
        val amplitude: Dp = 2.dp,
    )

    @Stable
    class SquigglesAnimator internal constructor(
        val animationProgress: State<Float>
    )

    @Composable
    fun rememberSquigglesAnimator(duration: Duration = 4.seconds): SquigglesAnimator {
        val animationProgress = rememberInfiniteTransition(label = "Squiggles animation")
            .animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = duration.inWholeMilliseconds.toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "Squiggles progress"
            )

        return remember {
            SquigglesAnimator(animationProgress)
        }
    }
}