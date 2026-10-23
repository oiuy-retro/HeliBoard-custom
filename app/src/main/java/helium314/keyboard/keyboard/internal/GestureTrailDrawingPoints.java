/*
 * Copyright (C) 2012 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.keyboard.internal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.SystemClock;

import helium314.keyboard.latin.common.ResizableIntArray;
import helium314.keyboard.latin.settings.Defaults;

/**
 * This class holds drawing points to represent a gesture trail. The gesture trail may contain
 * multiple non-contiguous gesture strokes and will be animated asynchronously from gesture input.
 * <p>
 * On the other hand, {@link GestureStrokeDrawingPoints} class holds drawing points of each gesture
 * stroke. This class holds drawing points of those gesture strokes to draw as a gesture trail.
 * Drawing points in this class will be asynchronously removed when fading out animation goes.
 */
final class GestureTrailDrawingPoints {
    public static final boolean DEBUG_SHOW_POINTS = false;
    public static final int POINT_TYPE_SAMPLED = 1;
    public static final int POINT_TYPE_INTERPOLATED = 2;

    // Default OpenBoard/Material Teal 200 (#80CBC4) in HSV: H = 174.4°, S = 37% (0.37),
    // V = 203/255 ≈ 79.6% (default slider 80%). Every new gesture stroke advances the hue by the
    // configured angle step (default 138°, matching the golden angle ~137.5°) while using the
    // configured saturation (0..100%) and brightness/value (0..100%).
    static final float DEFAULT_TEAL_HUE = 174.4f;
    static final float DEFAULT_TRAIL_VALUE = 203.0f / 255.0f;

    private static int sNextStrokeColorIndex = 0;

    static synchronized int nextTrailColor() {
        return nextTrailColor(
                Defaults.PREF_GESTURE_TRAIL_COLOR_SATURATION,
                Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS,
                Defaults.PREF_GESTURE_TRAIL_COLOR_HUE_STEP);
    }

    static synchronized int nextTrailColor(final int saturationPercent, final int hueStepDegrees) {
        return nextTrailColor(
                saturationPercent,
                Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS,
                hueStepDegrees);
    }

    static synchronized int nextTrailColor(final int saturationPercent,
            final int brightnessPercent, final int hueStepDegrees) {
        final int color = getTrailColorForIndex(
                sNextStrokeColorIndex, saturationPercent, brightnessPercent, hueStepDegrees);
        sNextStrokeColorIndex++;
        return color;
    }

    static synchronized void resetStrokeColorIndexForTest() {
        sNextStrokeColorIndex = 0;
    }

    static int getTrailColorForIndex(final int index) {
        return getTrailColorForIndex(
                index,
                Defaults.PREF_GESTURE_TRAIL_COLOR_SATURATION,
                Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS,
                Defaults.PREF_GESTURE_TRAIL_COLOR_HUE_STEP);
    }

    static int getTrailColorForIndex(final int index, final int saturationPercent,
            final int hueStepDegrees) {
        return getTrailColorForIndex(
                index,
                saturationPercent,
                Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS,
                hueStepDegrees);
    }

    static int getTrailColorForIndex(final int index, final int saturationPercent,
            final int brightnessPercent, final int hueStepDegrees) {
        final int clampedStep = Math.max(0, Math.min(360, hueStepDegrees));
        final float saturation = Math.max(0.0f, Math.min(1.0f, saturationPercent / 100.0f));
        final float value = (brightnessPercent == Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS)
                ? DEFAULT_TRAIL_VALUE
                : Math.max(0.0f, Math.min(1.0f, brightnessPercent / 100.0f));
        final float hue = (DEFAULT_TEAL_HUE + (index & 0x7fffffff) * (float)clampedStep) % 360.0f;
        return hsvToColor(0xFF, hue, saturation, value);
    }

    static int hsvToColor(final int alpha, final float hue, final float saturation,
            final float value) {
        final float h = ((hue % 360.0f) + 360.0f) % 360.0f;
        final float c = value * saturation;
        final float x = c * (1.0f - Math.abs(((h / 60.0f) % 2.0f) - 1.0f));
        final float m = value - c;
        final float rPrime;
        final float gPrime;
        final float bPrime;
        if (h < 60.0f) {
            rPrime = c;
            gPrime = x;
            bPrime = 0.0f;
        } else if (h < 120.0f) {
            rPrime = x;
            gPrime = c;
            bPrime = 0.0f;
        } else if (h < 180.0f) {
            rPrime = 0.0f;
            gPrime = c;
            bPrime = x;
        } else if (h < 240.0f) {
            rPrime = 0.0f;
            gPrime = x;
            bPrime = c;
        } else if (h < 300.0f) {
            rPrime = x;
            gPrime = 0.0f;
            bPrime = c;
        } else {
            rPrime = c;
            gPrime = 0.0f;
            bPrime = x;
        }
        final int r = Math.min(255, Math.max(0, Math.round((rPrime + m) * 255.0f)));
        final int g = Math.min(255, Math.max(0, Math.round((gPrime + m) * 255.0f)));
        final int b = Math.min(255, Math.max(0, Math.round((bPrime + m) * 255.0f)));
        return ((alpha & 0xFF) << 24) | (r << 16) | (g << 8) | b;
    }

    private static final int DEFAULT_CAPACITY = GestureStrokeDrawingPoints.PREVIEW_CAPACITY;

    // These {@link ResizableIntArray}s should be synchronized by {@link #mEventTimes}.
    private final ResizableIntArray mXCoordinates = new ResizableIntArray(DEFAULT_CAPACITY);
    private final ResizableIntArray mYCoordinates = new ResizableIntArray(DEFAULT_CAPACITY);
    private final ResizableIntArray mEventTimes = new ResizableIntArray(DEFAULT_CAPACITY);
    private final ResizableIntArray mPointColors = new ResizableIntArray(DEFAULT_CAPACITY);
    private final ResizableIntArray mPointTypes = new ResizableIntArray(
            DEBUG_SHOW_POINTS ? DEFAULT_CAPACITY : 0);
    private int mCurrentStrokeId = -1;
    private int mCurrentStrokeColor = getTrailColorForIndex(0);
    // The wall time of the zero value in {@link #mEventTimes}
    private long mCurrentTimeBase;
    private int mTrailStartIndex;
    private int mLastInterpolatedDrawIndex;

    // Use this value as imaginary zero because x-coordinates may be zero.
    private static final int DOWN_EVENT_MARKER = -128;

    private static int markAsDownEvent(final int xCoord) {
        return DOWN_EVENT_MARKER - xCoord;
    }

    private static boolean isDownEventXCoord(final int xCoordOrMark) {
        return xCoordOrMark <= DOWN_EVENT_MARKER;
    }

    private static int getXCoordValue(final int xCoordOrMark) {
        return isDownEventXCoord(xCoordOrMark)
                ? DOWN_EVENT_MARKER - xCoordOrMark : xCoordOrMark;
    }

    public void addStroke(final GestureStrokeDrawingPoints stroke, final long downTime) {
        addStroke(stroke, downTime,
                Defaults.PREF_GESTURE_TRAIL_COLOR_SATURATION,
                Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS,
                Defaults.PREF_GESTURE_TRAIL_COLOR_HUE_STEP);
    }

    public void addStroke(final GestureStrokeDrawingPoints stroke, final long downTime,
            final GestureTrailDrawingParams params) {
        final int saturation = (params != null)
                ? params.mTrailColorSaturation
                : Defaults.PREF_GESTURE_TRAIL_COLOR_SATURATION;
        final int brightness = (params != null)
                ? params.mTrailColorBrightness
                : Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS;
        final int hueStep = (params != null)
                ? params.mTrailColorHueStep
                : Defaults.PREF_GESTURE_TRAIL_COLOR_HUE_STEP;
        addStroke(stroke, downTime, saturation, brightness, hueStep);
    }

    public void addStroke(final GestureStrokeDrawingPoints stroke, final long downTime,
            final int saturationPercent, final int hueStepDegrees) {
        addStroke(stroke, downTime, saturationPercent,
                Defaults.PREF_GESTURE_TRAIL_COLOR_BRIGHTNESS, hueStepDegrees);
    }

    public void addStroke(final GestureStrokeDrawingPoints stroke, final long downTime,
            final int saturationPercent, final int brightnessPercent, final int hueStepDegrees) {
        synchronized (mEventTimes) {
            addStrokeLocked(stroke, downTime, saturationPercent, brightnessPercent, hueStepDegrees);
        }
    }

    private void addStrokeLocked(final GestureStrokeDrawingPoints stroke, final long downTime,
            final int saturationPercent, final int brightnessPercent, final int hueStepDegrees) {
        final int trailSize = mEventTimes.getLength();
        stroke.appendPreviewStroke(mEventTimes, mXCoordinates, mYCoordinates, mPointTypes);
        if (mEventTimes.getLength() == trailSize) {
            return;
        }
        final int[] eventTimes = mEventTimes.getPrimitiveArray();
        final int strokeId = stroke.getGestureStrokeId();
        // Because interpolation algorithm in {@link GestureStrokeDrawingPoints} can't determine
        // the interpolated points in the last segment of gesture stroke, it may need recalculation
        // of interpolation when new segments are added to the stroke.
        // {@link #mLastInterpolatedDrawIndex} holds the start index of the last segment. It may
        // be updated by the interpolation
        // {@link GestureStrokeDrawingPoints#interpolatePreviewStroke}
        // or by animation {@link #drawGestureTrail(Canvas,Paint,Rect,GestureTrailDrawingParams)}
        // below.
        final int lastInterpolatedIndex = (strokeId == mCurrentStrokeId)
                ? mLastInterpolatedDrawIndex : trailSize;
        mLastInterpolatedDrawIndex = stroke.interpolateStrokeAndReturnStartIndexOfLastSegment(
                lastInterpolatedIndex, mEventTimes, mXCoordinates, mYCoordinates, mPointTypes);
        if (strokeId != mCurrentStrokeId) {
            final int elapsedTime = (int)(downTime - mCurrentTimeBase);
            for (int i = mTrailStartIndex; i < trailSize; i++) {
                // Decay the previous strokes' event times.
                eventTimes[i] -= elapsedTime;
            }
            final int[] xCoords = mXCoordinates.getPrimitiveArray();
            xCoords[trailSize] = markAsDownEvent(xCoords[trailSize]);
            mCurrentTimeBase = downTime - eventTimes[trailSize];
            mCurrentStrokeId = strokeId;
            mCurrentStrokeColor = nextTrailColor(
                    saturationPercent, brightnessPercent, hueStepDegrees);
        }
        final int newTrailSize = mEventTimes.getLength();
        if (newTrailSize > lastInterpolatedIndex) {
            mPointColors.fill(mCurrentStrokeColor, lastInterpolatedIndex,
                    newTrailSize - lastInterpolatedIndex);
        }
        mPointColors.setLength(newTrailSize);
    }

    int getPointCountForTest() {
        synchronized (mEventTimes) {
            return mEventTimes.getLength();
        }
    }

    int getPointColorForTest(final int index) {
        synchronized (mEventTimes) {
            return mPointColors.get(index);
        }
    }

    boolean isDownEventPointForTest(final int index) {
        synchronized (mEventTimes) {
            return isDownEventXCoord(mXCoordinates.get(index));
        }
    }

    /**
     * Calculate the alpha of a gesture trail.
     * A gesture trail starts from fully opaque. After mFadeStartDelay has been passed, the alpha
     * of a trail reduces in proportion to the elapsed time. Then after mFadeDuration has been
     * passed, a trail becomes fully transparent.
     *
     * @param elapsedTime the elapsed time since a trail has been made.
     * @param params gesture trail display parameters
     * @return the alpha of a gesture trail
     */
    private static int getAlpha(final int elapsedTime, final GestureTrailDrawingParams params) {
        final int paramAlpha = params.mTrailColor >>> 24;
        final int fullAlpha = (paramAlpha != 0) ? paramAlpha : 0xFF;
        if (elapsedTime < params.mFadeoutStartDelay) {
            return fullAlpha;
        }
        final int decreasingAlpha = fullAlpha
                * (elapsedTime - params.mFadeoutStartDelay)
                / params.mFadeoutDuration;
        return fullAlpha - decreasingAlpha;
    }

    /**
     * Calculate the width of a gesture trail.
     * A gesture trail starts from the width of mTrailStartWidth and reduces its width in proportion
     * to the elapsed time. After mTrailEndWidth has been passed, the width becomes mTraiLEndWidth.
     *
     * @param elapsedTime the elapsed time since a trail has been made.
     * @param params gesture trail display parameters
     * @return the width of a gesture trail
     */
    private static float getWidth(final int elapsedTime, final GestureTrailDrawingParams params) {
        final float deltaWidth = params.mTrailStartWidth - params.mTrailEndWidth;
        return params.mTrailStartWidth - (deltaWidth * elapsedTime) / params.mTrailLingerDuration;
    }

    private final RoundedLine mRoundedLine = new RoundedLine();
    private final Rect mRoundedLineBounds = new Rect();

    /**
     * Draw gesture trail
     * @param canvas The canvas to draw the gesture trail
     * @param paint The paint object to be used to draw the gesture trail
     * @param outBoundsRect the bounding box of this gesture trail drawing
     * @param params The drawing parameters of gesture trail
     * @return true if some gesture trails remain to be drawn
     */
    public boolean drawGestureTrail(final Canvas canvas, final Paint paint,
            final Rect outBoundsRect, final GestureTrailDrawingParams params) {
        synchronized (mEventTimes) {
            return drawGestureTrailLocked(canvas, paint, outBoundsRect, params);
        }
    }

    private boolean drawGestureTrailLocked(final Canvas canvas, final Paint paint,
            final Rect outBoundsRect, final GestureTrailDrawingParams params) {
        // Initialize bounds rectangle.
        outBoundsRect.setEmpty();
        final int trailSize = mEventTimes.getLength();
        if (trailSize == 0) {
            return false;
        }

        final int[] eventTimes = mEventTimes.getPrimitiveArray();
        final int[] xCoords = mXCoordinates.getPrimitiveArray();
        final int[] yCoords = mYCoordinates.getPrimitiveArray();
        final int[] pointColors = mPointColors.getPrimitiveArray();
        final int[] pointTypes = mPointTypes.getPrimitiveArray();
        final int sinceDown = (int)(SystemClock.uptimeMillis() - mCurrentTimeBase);
        int startIndex;
        for (startIndex = mTrailStartIndex; startIndex < trailSize; startIndex++) {
            final int elapsedTime = sinceDown - eventTimes[startIndex];
            // Skip too old trail points.
            if (elapsedTime < params.mTrailLingerDuration) {
                break;
            }
        }
        mTrailStartIndex = startIndex;

        if (startIndex < trailSize) {
            paint.setStyle(Paint.Style.FILL);
            final RoundedLine roundedLine = mRoundedLine;
            int p1x = getXCoordValue(xCoords[startIndex]);
            int p1y = yCoords[startIndex];
            final int lastTime = sinceDown - eventTimes[startIndex];
            float r1 = getWidth(lastTime, params) / 2.0f;
            for (int i = startIndex + 1; i < trailSize; i++) {
                final int elapsedTime = sinceDown - eventTimes[i];
                final int p2x = getXCoordValue(xCoords[i]);
                final int p2y = yCoords[i];
                final float r2 = getWidth(elapsedTime, params) / 2.0f;
                // Draw trail line only when the current point isn't a down point.
                if (!isDownEventXCoord(xCoords[i])) {
                    final float body1 = r1 * params.mTrailBodyRatio;
                    final float body2 = r2 * params.mTrailBodyRatio;
                    final Path path = roundedLine.makePath(p1x, p1y, body1, p2x, p2y, body2);
                    if (!path.isEmpty()) {
                        final int trailColor = pointColors[i];
                        paint.setColor(trailColor);
                        roundedLine.getBounds(mRoundedLineBounds);
                        if (params.mTrailShadowEnabled) {
                            final float shadow2 = r2 * params.mTrailShadowRatio;
                            paint.setShadowLayer(shadow2, 0.0f, 0.0f, trailColor);
                            final int shadowInset = -(int)Math.ceil(shadow2);
                            mRoundedLineBounds.inset(shadowInset, shadowInset);
                        }
                        // Take union for the bounds.
                        outBoundsRect.union(mRoundedLineBounds);
                        final int alpha = getAlpha(elapsedTime, params);
                        paint.setAlpha(alpha);
                        canvas.drawPath(path, paint);
                    }
                }
                p1x = p2x;
                p1y = p2y;
                r1 = r2;
            }
            if (DEBUG_SHOW_POINTS) {
                debugDrawPoints(canvas, startIndex, trailSize, paint);
            }
        }

        final int newSize = trailSize - startIndex;
        if (newSize < startIndex) {
            mTrailStartIndex = 0;
            if (newSize > 0) {
                System.arraycopy(eventTimes, startIndex, eventTimes, 0, newSize);
                System.arraycopy(xCoords, startIndex, xCoords, 0, newSize);
                System.arraycopy(yCoords, startIndex, yCoords, 0, newSize);
                System.arraycopy(pointColors, startIndex, pointColors, 0, newSize);
                if (DEBUG_SHOW_POINTS) {
                    System.arraycopy(pointTypes, startIndex, pointTypes, 0, newSize);
                }
            }
            mEventTimes.setLength(newSize);
            mXCoordinates.setLength(newSize);
            mYCoordinates.setLength(newSize);
            mPointColors.setLength(newSize);
            if (DEBUG_SHOW_POINTS) {
                mPointTypes.setLength(newSize);
            }
            // The start index of the last segment of the stroke
            // {@link mLastInterpolatedDrawIndex} should also be updated because all array
            // elements have just been shifted for compaction or been zeroed.
            mLastInterpolatedDrawIndex = Math.max(mLastInterpolatedDrawIndex - startIndex, 0);
        }
        return newSize > 0;
    }

    private void debugDrawPoints(final Canvas canvas, final int startIndex, final int endIndex,
            final Paint paint) {
        final int[] xCoords = mXCoordinates.getPrimitiveArray();
        final int[] yCoords = mYCoordinates.getPrimitiveArray();
        final int[] pointTypes = mPointTypes.getPrimitiveArray();
        // {@link Paint} that is zero width stroke and anti alias off draws exactly 1 pixel.
        paint.setAntiAlias(false);
        paint.setStrokeWidth(0);
        for (int i = startIndex; i < endIndex; i++) {
            final int pointType = pointTypes[i];
            if (pointType == POINT_TYPE_INTERPOLATED) {
                paint.setColor(Color.RED);
            } else if (pointType == POINT_TYPE_SAMPLED) {
                paint.setColor(0xFFA000FF);
            } else {
                paint.setColor(Color.GREEN);
            }
            canvas.drawPoint(getXCoordValue(xCoords[i]), yCoords[i], paint);
        }
        paint.setAntiAlias(true);
    }
}
