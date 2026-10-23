// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard.internal

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.SystemClock
import helium314.keyboard.settings.dialogs.computeDefaultTickFraction
import helium314.keyboard.settings.dialogs.defaultSliderStepValue
import helium314.keyboard.settings.dialogs.snapSliderValueToDefault
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import org.robolectric.shadows.ShadowSystemClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class GestureTrailDrawingPointsTest {
    @Test
    fun `consecutive gesture strokes cycle through distinct light colors starting with default teal`() {
        GestureTrailDrawingPoints.resetStrokeColorIndexForTest()

        // Index 0 matches default OpenBoard/Material Teal 200 (#80CBC4).
        val firstColor = GestureTrailDrawingPoints.getTrailColorForIndex(0)
        assertEquals(0xFF80CBC4.toInt(), firstColor)

        val colors = (0 until 12).map { GestureTrailDrawingPoints.nextTrailColor() }
        assertEquals(12, colors.toSet().size, "Each consecutive swipe should get a distinct color")
        assertEquals(0xFF80CBC4.toInt(), colors.first())

        // Every generated color must have the same light HSV saturation (37%) and value (79.6%) as #80CBC4
        // (RGB channels bounded between 0x80 and 0xCB).
        for (color in colors) {
            val r = (color ushr 16) and 0xFF
            val g = (color ushr 8) and 0xFF
            val b = color and 0xFF
            assertEquals(0xFF, (color ushr 24) and 0xFF)
            assertEquals(0x80, minOf(r, g, b), "Minimum RGB channel should match light teal floor (0x80)")
            assertEquals(0xCB, maxOf(r, g, b), "Maximum RGB channel should match light teal ceiling (0xCB)")
        }
    }

    @Test
    fun `custom saturation and angle step control trail colors`() {
        GestureTrailDrawingPoints.resetStrokeColorIndexForTest()

        // Angle step 0° keeps the default teal hue constant across swipes
        val stepZeroColors = (0 until 5).map { GestureTrailDrawingPoints.nextTrailColor(37, 0) }
        assertEquals(1, stepZeroColors.toSet().size, "0° angle step should keep trail color constant")
        assertEquals(0xFF80CBC4.toInt(), stepZeroColors.first())

        // Saturation 0% produces neutral light grey (all RGB channels equal to round(0.796 * 255) = 0xCB)
        val desaturated = GestureTrailDrawingPoints.getTrailColorForIndex(3, 0, 138)
        assertEquals(0xFFCBCBCB.toInt(), desaturated)

        // Saturation 100% produces full-chroma color with floor 0x00 and ceiling 0xCB
        val fullSaturation = GestureTrailDrawingPoints.getTrailColorForIndex(0, 100, 138)
        val r = (fullSaturation ushr 16) and 0xFF
        val g = (fullSaturation ushr 8) and 0xFF
        val b = fullSaturation and 0xFF
        assertEquals(0x00, minOf(r, g, b))
        assertEquals(0xCB, maxOf(r, g, b))
    }

    @Test
    fun `slider snaps to default value and computes tick mark and step values`() {
        // 0..100 range with default 37: 2% window is [35, 39]
        assertEquals(37f, snapSliderValueToDefault(36f, 37f, 0f..100f))
        assertEquals(37f, snapSliderValueToDefault(38.5f, 37f, 0f..100f))
        assertEquals(30f, snapSliderValueToDefault(30f, 37f, 0f..100f))

        // Out-of-range default (e.g. 0 on 21..36) does not snap and has no tick mark
        assertEquals(21f, snapSliderValueToDefault(21f, 0f, 21f..36f))
        assertNull(computeDefaultTickFraction(0f, 21f..36f))
        assertNull(computeDefaultTickFraction(null, 0f..100f))

        // Tick mark fraction for valid defaults
        assertEquals(0.37f, computeDefaultTickFraction(37f, 0f..100f)!!, 0.0001f)
        assertEquals(138f / 180f, computeDefaultTickFraction(138f, 0f..180f)!!, 0.0001f)
        assertEquals(0.5f, computeDefaultTickFraction(1.0f, 0.5f..1.5f)!!, 0.0001f)

        // Step quantization: integer ranges step by 1, small float scale ranges step by 1% (0.01)
        assertEquals(37, defaultSliderStepValue(37.2f, 0f..100f))
        assertEquals(38, defaultSliderStepValue(37.8f, 0f..100f))
        assertEquals(100, defaultSliderStepValue(1.00f, 0.5f..1.5f))
        assertEquals(101, defaultSliderStepValue(1.01f, 0.5f..1.5f))
    }

    @Test
    fun `lingering strokes keep their own color when new strokes are added and drawn`() {
        GestureTrailDrawingPoints.resetStrokeColorIndexForTest()
        val strokeParams = GestureStrokeDrawingParams()
        val strokePoints = GestureStrokeDrawingPoints(strokeParams)
        val trail = GestureTrailDrawingPoints()
        val drawParams = GestureTrailDrawingParams(
            0xFF80CBC4.toInt(),
            10.0f,
            2.5f,
            1.0f,
            false,
            0.0f,
            100,
            19900, // 20 s total linger duration
            20
        )

        val startUptime = SystemClock.uptimeMillis()

        // First swipe
        strokePoints.onDownEvent(10, 20, 0)
        trail.addStroke(strokePoints, startUptime, drawParams)
        strokePoints.onMoveEvent(50, 60, 40)
        strokePoints.onMoveEvent(90, 100, 80)
        trail.addStroke(strokePoints, startUptime, drawParams)

        val firstStrokePointCount = trail.pointCountForTest
        assertTrue(firstStrokePointCount >= 3)
        val expectedFirstColor = GestureTrailDrawingPoints.getTrailColorForIndex(0)
        for (i in 0 until firstStrokePointCount) {
            assertEquals(expectedFirstColor, trail.getPointColorForTest(i))
        }

        // Advance clock by 500 ms and start a second swipe while first swipe is still lingering
        ShadowSystemClock.advanceBy(Duration.ofMillis(500))
        val secondDownTime = SystemClock.uptimeMillis()
        strokePoints.onDownEvent(200, 210, 0)
        trail.addStroke(strokePoints, secondDownTime, drawParams)
        strokePoints.onMoveEvent(260, 270, 50)
        strokePoints.onMoveEvent(320, 330, 100)
        trail.addStroke(strokePoints, secondDownTime, drawParams)

        val totalPointsAfterSecond = trail.pointCountForTest
        assertTrue(totalPointsAfterSecond > firstStrokePointCount)
        val expectedSecondColor = GestureTrailDrawingPoints.getTrailColorForIndex(1)
        assertNotEquals(expectedFirstColor, expectedSecondColor)

        // All points from the first swipe must still have the first swipe's color
        for (i in 0 until firstStrokePointCount) {
            assertEquals(expectedFirstColor, trail.getPointColorForTest(i), "First stroke point $i changed color")
        }
        // All points from the second swipe must have the second swipe's color
        assertTrue(trail.isDownEventPointForTest(firstStrokePointCount))
        for (i in firstStrokePointCount until totalPointsAfterSecond) {
            assertEquals(expectedSecondColor, trail.getPointColorForTest(i), "Second stroke point $i has wrong color")
        }

        // Verify drawGestureTrail draws segments with both stroke RGB colors
        val drawnRgbColors = mutableListOf<Int>()
        val recordingCanvas = object : Canvas() {
            override fun drawPath(path: Path, paint: Paint) {
                drawnRgbColors.add(paint.color and 0x00FFFFFF)
            }
        }
        val outBounds = Rect()
        val stillActive = trail.drawGestureTrail(recordingCanvas, Paint(), outBounds, drawParams)
        assertTrue(stillActive)
        assertTrue((expectedFirstColor and 0x00FFFFFF) in drawnRgbColors, "First stroke color should be drawn")
        assertTrue((expectedSecondColor and 0x00FFFFFF) in drawnRgbColors, "Second stroke color should be drawn")
        assertEquals(expectedFirstColor and 0x00FFFFFF, drawnRgbColors.first())
        assertEquals(expectedSecondColor and 0x00FFFFFF, drawnRgbColors.last())
    }
}
