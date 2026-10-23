// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.dialogs

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.previewDark
import kotlin.math.abs
import kotlin.math.roundToInt

const val SLIDER_DEFAULT_SNAP_FRACTION = 0.02f
private const val STEP_HAPTIC_MIN_INTERVAL_MS = 12L
private var lastStepHapticUptimeMs = 0L

fun snapSliderValueToDefault(
    rawValue: Float,
    defaultValue: Float?,
    range: ClosedFloatingPointRange<Float>,
    snapFraction: Float = SLIDER_DEFAULT_SNAP_FRACTION,
): Float {
    if (defaultValue == null || defaultValue !in range) return rawValue
    val span = range.endInclusive - range.start
    if (span <= 0f) return rawValue
    return if (abs(rawValue - defaultValue) <= span * snapFraction) defaultValue else rawValue
}

fun computeDefaultTickFraction(
    defaultValue: Float?,
    range: ClosedFloatingPointRange<Float>,
): Float? {
    if (defaultValue == null || defaultValue !in range) return null
    val span = range.endInclusive - range.start
    if (span <= 0f) return null
    return ((defaultValue - range.start) / span).coerceIn(0f, 1f)
}

fun defaultSliderStepValue(pos: Float, range: ClosedFloatingPointRange<Float>): Int {
    val span = range.endInclusive - range.start
    return if (span <= 10f) (pos * 100f).roundToInt() else pos.roundToInt()
}

fun Modifier.drawDefaultTickMark(
    defaultValue: Float?,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
): Modifier {
    val fraction = computeDefaultTickFraction(defaultValue, range) ?: return this
    return this.drawWithContent {
        drawContent()
        val insetPx = 10.dp.toPx()
        val trackWidth = (size.width - 2f * insetPx).coerceAtLeast(0f)
        val x = insetPx + fraction * trackWidth
        val tickHalfHeight = 8.dp.toPx()
        val strokeWidth = 2.5.dp.toPx()
        val centerY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(x, centerY - tickHalfHeight),
            end = Offset(x, centerY + tickHalfHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private fun getVibrator(context: Context): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

/** Light haptic tick fired on every +1 / -1 step change while dragging a slider. */
@Suppress("DEPRECATION")
fun performSliderStepHaptic(view: View) {
    val now = SystemClock.uptimeMillis()
    if (now - lastStepHapticUptimeMs < STEP_HAPTIC_MIN_INTERVAL_MS) return
    lastStepHapticUptimeMs = now

    val feedbackConstant = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> HapticFeedbackConstants.TEXT_HANDLE_MOVE
        else -> HapticFeedbackConstants.CLOCK_TICK
    }
    view.performHapticFeedback(feedbackConstant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
    val vibrator = getVibrator(view.context)
    if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(5L, 65))
        } else {
            vibrator.vibrate(5L)
        }
    }
}

/** Stronger haptic pulse fired when a slider snaps onto its default value. */
@Suppress("DEPRECATION")
fun performSliderDefaultSnapHaptic(view: View) {
    lastStepHapticUptimeMs = SystemClock.uptimeMillis()
    view.performHapticFeedback(
        HapticFeedbackConstants.LONG_PRESS,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
    val vibrator = getVibrator(view.context)
    if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(28L, 255))
        } else {
            vibrator.vibrate(28L)
        }
    }
}

@Composable
fun SliderDialog(
    onDismissRequest: () -> Unit,
    onDone: (Float) -> Unit,
    initialValue: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    showDefault: Boolean = false,
    onDefault: () -> Unit = { },
    defaultValue: Float? = null,
    isDefaultValue: ((Float) -> Boolean)? = null,
    stepValue: ((Float) -> Int)? = null,
    onValueChanged: (Float) -> Unit = { },
    title: (@Composable () -> Unit)? = null,
    intermediateSteps: Int? = null,
    positionString: (@Composable (Float) -> String) = { it.toString() },
) {
    val view = LocalView.current
    val checkDefault: (Float) -> Boolean = isDefaultValue
        ?: { v -> defaultValue != null && defaultValue in range && v == defaultValue }
    val getStep: (Float) -> Int = stepValue ?: { v -> defaultSliderStepValue(v, range) }
    var sliderPosition by remember { mutableFloatStateOf(initialValue) }
    var wasAtDefault by remember { mutableStateOf(checkDefault(initialValue)) }
    var lastStepValue by remember { mutableIntStateOf(getStep(initialValue)) }
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    val sliderModifier = Modifier.drawDefaultTickMark(defaultValue, range, tickColor)

    val handleValueChange: (Float) -> Unit = { raw ->
        val snapped = snapSliderValueToDefault(raw, defaultValue, range)
        val nowAtDefault = checkDefault(snapped)
        val currentStep = getStep(snapped)
        if (nowAtDefault && !wasAtDefault) {
            performSliderDefaultSnapHaptic(view)
        } else if (currentStep != lastStepValue) {
            performSliderStepHaptic(view)
        }
        wasAtDefault = nowAtDefault
        lastStepValue = currentStep
        sliderPosition = snapped
    }

    ThreeButtonAlertDialog(
        onDismissRequest = onDismissRequest,
        neutralButtonText = if (showDefault) stringResource(R.string.button_default) else null,
        onNeutral = { onDismissRequest(); onDefault() },
        onConfirmed = { onDone(sliderPosition) },
        modifier = modifier,
        title = title,
        content = {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge
            ) {
                Column {
                    if (intermediateSteps == null)
                        Slider(
                            value = sliderPosition,
                            onValueChange = handleValueChange,
                            onValueChangeFinished = { onValueChanged(sliderPosition) },
                            valueRange = range,
                            modifier = sliderModifier,
                        )
                    else
                        Slider(
                            value = sliderPosition,
                            onValueChange = handleValueChange,
                            onValueChangeFinished = { onValueChanged(sliderPosition) },
                            valueRange = range,
                            steps = intermediateSteps,
                            modifier = sliderModifier,
                        )
                    Text(positionString(sliderPosition))
                }
            }
        },
    )
}

@Preview
@Composable
private fun PreviewSliderDialog() {
    Theme(previewDark) {
        SliderDialog(
            onDismissRequest = { },
            onDone = { },
            initialValue = 100f,
            range = 0f..500f,
            defaultValue = 250f,
            title = { Text("move it") },
            showDefault = true
        )
    }
}
