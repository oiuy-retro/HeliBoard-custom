// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.dialogs

import android.content.Context
import android.os.Build
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.previewDark
import kotlin.math.abs

const val SLIDER_DEFAULT_SNAP_FRACTION = 0.02f

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

@Suppress("DEPRECATION")
fun performSliderDefaultSnapHaptic(view: View) {
    val feedbackConstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        HapticFeedbackConstants.TEXT_HANDLE_MOVE
    else
        HapticFeedbackConstants.CLOCK_TICK
    view.performHapticFeedback(feedbackConstant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (view.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        view.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(15L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(15L)
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
    onValueChanged: (Float) -> Unit = { },
    title: (@Composable () -> Unit)? = null,
    intermediateSteps: Int? = null,
    positionString: (@Composable (Float) -> String) = { it.toString() },
) {
    val view = LocalView.current
    val checkDefault: (Float) -> Boolean = isDefaultValue
        ?: { v -> defaultValue != null && defaultValue in range && v == defaultValue }
    var sliderPosition by remember { mutableFloatStateOf(initialValue) }
    var wasAtDefault by remember { mutableStateOf(checkDefault(initialValue)) }

    val handleValueChange: (Float) -> Unit = { raw ->
        val snapped = snapSliderValueToDefault(raw, defaultValue, range)
        val nowAtDefault = checkDefault(snapped)
        if (nowAtDefault && !wasAtDefault) {
            performSliderDefaultSnapHaptic(view)
        }
        wasAtDefault = nowAtDefault
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
                        )
                    else
                        Slider(
                            value = sliderPosition,
                            onValueChange = handleValueChange,
                            onValueChangeFinished = { onValueChanged(sliderPosition) },
                            valueRange = range,
                            steps = intermediateSteps
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
            title = { Text("move it") },
            showDefault = true
        )
    }
}
