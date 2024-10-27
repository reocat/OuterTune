@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.dd3boh.outertune.ui.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyList
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.animateToNextPage
import androidx.compose.foundation.pager.animateToPreviousPage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.pageDown
import androidx.compose.ui.semantics.pageLeft
import androidx.compose.ui.semantics.pageRight
import androidx.compose.ui.semantics.pageUp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
@ExperimentalFoundationApi
fun <T> HorizontalPager(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: PagerState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    beyondBoundsPageCount: Int = 0,
    pageSpacing: Dp = 0.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: SnapFlingBehavior = PagerDefaults.flingBehavior(state = state),
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    key: ((item: T) -> Any)? = null,
    pageNestedScrollConnection: NestedScrollConnection =
        PagerDefaults.pageNestedScrollConnection(
            state = state,
            orientation = Orientation.Horizontal,
        ),
    pageContent: @Composable (item: T) -> Unit,
) {
    Pager(
        modifier = modifier,
        state = state,
        items = items,
        pageSpacing = pageSpacing,
        userScrollEnabled = userScrollEnabled,
        orientation = Orientation.Horizontal,
        verticalAlignment = verticalAlignment,
        reverseLayout = reverseLayout,
        contentPadding = contentPadding,
        beyondBoundsPageCount = beyondBoundsPageCount,
        pageSize = pageSize,
        flingBehavior = flingBehavior,
        key = key,
        pageNestedScrollConnection = pageNestedScrollConnection,
        pageContent = pageContent,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun <T> Pager(
    modifier: Modifier,
    state: PagerState,
    items: List<T>,
    pageSize: PageSize,
    pageSpacing: Dp,
    orientation: Orientation,
    beyondBoundsPageCount: Int,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    contentPadding: PaddingValues,
    flingBehavior: SnapFlingBehavior,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    key: ((item: T) -> Any)?,
    pageNestedScrollConnection: NestedScrollConnection,
    pageContent: @Composable (item: T) -> Unit,
) {
    require(beyondBoundsPageCount >= 0) {
        "beyondBoundsPageCount should be greater than or equal to 0, " +
            "you selected $beyondBoundsPageCount"
    }

    val isVertical = orientation == Orientation.Vertical
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val calculatedContentPaddings =
        remember(contentPadding, orientation, layoutDirection) {
            calculateContentPaddings(
                contentPadding,
                orientation,
                layoutDirection,
            )
        }

    val pagerFlingBehavior =
        remember(flingBehavior, state) {
            PagerWrapperFlingBehavior(flingBehavior, state)
        }

    val pagerSemantics =
        if (userScrollEnabled) {
            Modifier.pagerSemantics(state, isVertical)
        } else {
            Modifier
        }

    BoxWithConstraints(modifier = modifier.then(pagerSemantics)) {
        val mainAxisSize = if (isVertical) constraints.maxHeight else constraints.maxWidth
        // Calculates how pages are shown across the main axis
        val pageAvailableSize =
            remember(
                density,
                mainAxisSize,
                pageSpacing,
                calculatedContentPaddings,
            ) {
                with(density) {
                    val pageSpacingPx = pageSpacing.roundToPx()
                    val contentPaddingPx = calculatedContentPaddings.roundToPx()
                    with(pageSize) {
                        density.calculateMainAxisPageSize(
                            mainAxisSize - contentPaddingPx,
                            pageSpacingPx,
                        )
                    }.toDp()
                }
            }

        val horizontalAlignmentForSpacedArrangement =
            if (!reverseLayout) Alignment.Start else Alignment.End
        val verticalAlignmentForSpacedArrangement =
            if (!reverseLayout) Alignment.Top else Alignment.Bottom

        LazyList(
            modifier = Modifier,
            state = rememberLazyListState(),
            contentPadding = contentPadding,
            flingBehavior = pagerFlingBehavior,
            horizontalAlignment = horizontalAlignment,
            horizontalArrangement =
                Arrangement.spacedBy(
                    pageSpacing,
                    horizontalAlignmentForSpacedArrangement,
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    pageSpacing,
                    verticalAlignmentForSpacedArrangement,
                ),
            verticalAlignment = verticalAlignment,
            isVertical = isVertical,
            reverseLayout = reverseLayout,
            userScrollEnabled = userScrollEnabled,
            beyondBoundsItemCount = beyondBoundsPageCount,
        ) {
            items(items = items, key = key) { item ->
                val pageMainAxisSizeModifier =
                    if (isVertical) {
                        Modifier.height(pageAvailableSize)
                    } else {
                        Modifier.width(pageAvailableSize)
                    }
                Box(
                    modifier =
                        Modifier
                            .then(pageMainAxisSizeModifier)
                            .nestedScroll(pageNestedScrollConnection),
                    contentAlignment = Alignment.Center,
                ) {
                    pageContent(item)
                }
            }
        }
    }
}

private fun calculateContentPaddings(
    contentPadding: PaddingValues,
    orientation: Orientation,
    layoutDirection: LayoutDirection,
): Dp {
    val startPadding =
        if (orientation == Orientation.Vertical) {
            contentPadding.calculateTopPadding()
        } else {
            contentPadding.calculateLeftPadding(layoutDirection)
        }

    val endPadding =
        if (orientation == Orientation.Vertical) {
            contentPadding.calculateBottomPadding()
        } else {
            contentPadding.calculateRightPadding(layoutDirection)
        }

    return startPadding + endPadding
}

@OptIn(ExperimentalFoundationApi::class)
private class PagerWrapperFlingBehavior(
    val originalFlingBehavior: SnapFlingBehavior,
    val pagerState: PagerState,
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float =
        with(originalFlingBehavior) {
            performFling(initialVelocity) { remainingScrollOffset ->
                pagerState.snapRemainingScrollOffset = remainingScrollOffset
            }
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("ComposableModifierFactory")
@Composable
private fun Modifier.pagerSemantics(
    state: PagerState,
    isVertical: Boolean,
): Modifier {
    val scope = rememberCoroutineScope()

    fun performForwardPaging(): Boolean =
        if (state.canScrollForward) {
            scope.launch {
                state.animateToNextPage()
            }
            true
        } else {
            false
        }

    fun performBackwardPaging(): Boolean =
        if (state.canScrollBackward) {
            scope.launch {
                state.animateToPreviousPage()
            }
            true
        } else {
            false
        }

    return this.then(
        Modifier.semantics {
            if (isVertical) {
                pageUp { performBackwardPaging() }
                pageDown { performForwardPaging() }
            } else {
                pageLeft { performBackwardPaging() }
                pageRight { performForwardPaging() }
            }
        },
    )
}
