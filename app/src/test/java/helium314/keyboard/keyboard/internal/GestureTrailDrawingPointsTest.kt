// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard.internal

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.SystemClock
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import org.robolectric.shadows.ShadowSystemClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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

        // Every generated color must have the same light HSV saturation and value as #80CBC4
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
        trail.addStroke(strokePoints, startUptime)
        strokePoints.onMoveEvent(50, 60, 40)
        strokePoints.onMoveEvent(90, 100, 80)
        trail.addStroke(strokePoints, startUptime)

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
        trail.addStroke(strokePoints, secondDownTime)
        strokePoints.onMoveEvent(260, 270, 50)
        strokePoints.onMoveEvent(320, 330, 100)
        trail.addStroke(strokePoints, secondDownTime)

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
