package com.dd3boh.outertune.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TextPlaceholder(
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.7f,
    height: Dp = 16.dp
) {
    Spacer(
        modifier = modifier
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            .fillMaxWidth(widthFraction)
            .height(height)
    )
}
