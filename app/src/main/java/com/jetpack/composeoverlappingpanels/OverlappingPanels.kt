package com.jetpack.composeoverlappingpanels

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

enum class OverlappingPanelsValue {
    OpenStart, OpenEnd, Closed
}

@ExperimentalMaterialApi
class OverlappingPanelsState(
    initialValue: OverlappingPanelsValue,
    confirmStateChange: (OverlappingPanelsValue) -> Boolean = { true },
) {
    val swipeableState = SwipeableState(
        initialValue = initialValue,
        animationSpec = spring(),
        confirmStateChange = confirmStateChange
    )
    val currentValue
        get() = swipeableState.currentValue
    val targetValue
        get() = swipeableState.targetValue
    val offset
        get() = swipeableState.offset
    val offsetIsPositive
        get() = offset.value > 0f
    val offsetIsNegative
        get() = offset.value < 0f
    val offsetNotZero
        get() = offset.value != 0f
    val isPanelsClosed
        get() = currentValue == OverlappingPanelsValue.Closed
    val isEndPanelOpen
        get() = currentValue == OverlappingPanelsValue.OpenStart
    val isStartPanelOpen
        get() = currentValue == OverlappingPanelsValue.OpenEnd

    suspend fun openStartPanel() {
        swipeableState.animateTo(OverlappingPanelsValue.OpenEnd)
    }

    suspend fun openEndPanel() {
        swipeableState.animateTo(OverlappingPanelsValue.OpenStart)
    }

    suspend fun closePanels() {
        swipeableState.animateTo(OverlappingPanelsValue.Closed)
    }

    companion object {
        fun Saver(confirmStateChange: (OverlappingPanelsValue) -> Boolean) =
            Saver<OverlappingPanelsState, OverlappingPanelsValue>(
                save = { it.currentValue },
                restore = { OverlappingPanelsState(it, confirmStateChange) }
            )
    }
}

@ExperimentalMaterialApi
@Composable
fun rememberOverlappingPanelsState(
    initialValue: OverlappingPanelsValue = OverlappingPanelsValue.Closed,
    confirmStateChange: (OverlappingPanelsValue) -> Boolean = { true },
): OverlappingPanelsState {
    return rememberSaveable(saver = OverlappingPanelsState.Saver(confirmStateChange)) {
        OverlappingPanelsState(initialValue, confirmStateChange)
    }
}

@ExperimentalMaterialApi
@Composable
fun OverlappingPanels(
    panelStart: @Composable BoxScope.() -> Unit,
    panelCenter: @Composable BoxScope.() -> Unit,
    panelEnd: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    panelsState: OverlappingPanelsState = rememberOverlappingPanelsState(initialValue = OverlappingPanelsValue.Closed),
    gesturesEnabled: Boolean = true,
    velocityThreshold: Dp = 400.dp,
    resistance: (anchors: Set<Float>) -> ResistanceConfig? = { null },
    sidePanelWidthFraction: SidePanelWidthFraction = PanelDefaults.sidePanelWidthFraction(),
    centerPanelAlpha: CenterPanelAlpha = PanelDefaults.centerPanelAlpha(),
    centerPanelElevation: Dp = 8.dp,
) {
    val resources = LocalContext.current.resources
    val layoutDirection = LocalLayoutDirection.current

    BoxWithConstraints(modifier.fillMaxSize()) {
        val fraction =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                sidePanelWidthFraction.portrait()
            else
                sidePanelWidthFraction.landscape()

        val offsetValue = (constraints.maxWidth * fraction) + PanelDefaults.MarginBetweenPanels.value

        val animatedCenterPanelAlpha by animateFloatAsState(
            targetValue =
            if (abs(panelsState.offset.value) == abs(offsetValue))
                centerPanelAlpha.sidesOpened()
            else
                centerPanelAlpha.sidesClosed(),
        )

        val anchors = mapOf(
            offsetValue to OverlappingPanelsValue.OpenEnd,
            0f to OverlappingPanelsValue.Closed,
            -offsetValue to OverlappingPanelsValue.OpenStart
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .swipeable(
                    state = panelsState.swipeableState,
                    orientation = Orientation.Horizontal,
                    velocityThreshold = velocityThreshold,
                    anchors = anchors,
                    enabled = gesturesEnabled,
                    reverseDirection = layoutDirection == LayoutDirection.Rtl,
                    resistance = resistance(anchors.keys),
                )
        ) {
            val sidePanelAlignment = organizeSidePanel(
                panelsState,
                onStartPanel = { Alignment.CenterStart },
                onEndPanel = { Alignment.CenterEnd },
                onNeither = { Alignment.Center }
            )
            val sidePanelContent = organizeSidePanel(
                panelsState,
                onStartPanel = { panelStart },
                onEndPanel = { panelEnd },
                onNeither = { {} }
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .align(sidePanelAlignment),
                content = sidePanelContent
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .alpha(animatedCenterPanelAlpha)
                    .offset {
                        IntOffset(
                            x = panelsState.offset.value.roundToInt(),
                            y = 0
                        )
                    }
                    .shadow(centerPanelElevation),
                content = panelCenter
            )
        }
    }
}

interface SidePanelWidthFraction {
    @Composable
    fun portrait(): Float

    @Composable
    fun landscape(): Float
}

interface CenterPanelAlpha {
    @Composable
    fun sidesOpened(): Float

    @Composable
    fun sidesClosed(): Float
}

@ExperimentalMaterialApi
private inline fun <T> organizeSidePanel(
    panelsState: OverlappingPanelsState,
    onStartPanel: () -> T,
    onEndPanel: () -> T,
    onNeither: () -> T,
) = when {
    panelsState.offsetIsPositive -> onStartPanel()
    panelsState.offsetIsNegative -> onEndPanel()
    else -> onNeither()
}

object PanelDefaults {
    val MarginBetweenPanels = 16.dp

    @Composable
    fun sidePanelWidthFraction(
        portrait: Float = 0.85f,
        landscape: Float = 0.45f,
    ): SidePanelWidthFraction = DefaultSidePanelWidthFraction(
        portrait = portrait,
        landscape = landscape,
    )

    @Composable
    fun centerPanelAlpha(
        sidesOpened: Float = 0.7f,
        sidesClosed: Float = 1f
    ): CenterPanelAlpha = DefaultCenterPanelAlpha(
        sidesOpened = sidesOpened,
        sidesClosed = sidesClosed,
    )
}

private class DefaultSidePanelWidthFraction(
    private val portrait: Float,
    private val landscape: Float,
) : SidePanelWidthFraction {
    @Composable
    override fun portrait() = portrait

    @Composable
    override fun landscape() = landscape
}

private class DefaultCenterPanelAlpha(
    private val sidesOpened: Float,
    private val sidesClosed: Float,
) : CenterPanelAlpha {
    @Composable
    override fun sidesOpened() = sidesOpened

    @Composable
    override fun sidesClosed() = sidesClosed
}