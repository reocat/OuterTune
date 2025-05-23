/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp


@Composable
fun <E> ChipsRow(
    chips: List<Pair<E, String>>,
    currentValue: E,
    onValueUpdate: (E) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    isLoading: (E) -> Boolean = { false }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.width(12.dp))

        chips.forEach { (value, label) ->
            FilterChip(
                label = { Text(label) },
                selected = currentValue == value,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = containerColor,
                ),
                onClick = { onValueUpdate(value) },
                trailingIcon = {
                    if (isLoading(value)) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                border = null
            )

            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun <E> ChipsLazyRow(
    chips: List<Pair<E, String>>,
    currentValue: E,
    onValueUpdate: (E) -> Unit,
    modifier: Modifier = Modifier,
    selected: ((E) -> Boolean)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    isLoading: (E) -> Boolean = { false }
) {
    val haptic = LocalHapticFeedback.current
    val tween: FiniteAnimationSpec<Float> = tween(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val placementTween: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = 300,
        easing = LinearOutSlowInEasing
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
    ) {
        item(
            key = "spacer"
        ) {
            Spacer(Modifier.width(12.dp))
        }

        items(
            items = chips,
            key = { it.second }
        ) {(value, label) ->
            FilterChip(
                label = { Text(label) },
                selected = selected?.let { it(value) } ?: (currentValue == value),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = containerColor,
                ),
                onClick = {
                    onValueUpdate(value)
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                },
                modifier = Modifier
                    .animateItem(
                        fadeInSpec =  tween,
                        placementSpec = placementTween,
                        fadeOutSpec = tween
                    ),
                trailingIcon = {
                    if (isLoading(value)) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                border = null
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun AnimatedNavigationChips(
    chips: List<Pair<String, String>>,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    chipContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    animatedBorderColors: Pair<Color, Color> = Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        itemsIndexed(chips) { index, (value, label) ->
            val infiniteTransition = rememberInfiniteTransition(label = "chipBorderAnimation_$value")

            val animatedBorderColor by infiniteTransition.animateColor(
                initialValue = animatedBorderColors.first,
                targetValue = animatedBorderColors.second,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1500 + (index % 3) * 250,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "chipPulsingBorderColor_$value"
            )

            FilterChip(
                label = { Text(label) },
                selected = false,
                onClick = { onChipClick(value) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = chipContainerColor,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = animatedBorderColor
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}