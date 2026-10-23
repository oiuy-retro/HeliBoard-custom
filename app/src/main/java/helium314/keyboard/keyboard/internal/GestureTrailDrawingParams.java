/*
 * Copyright (C) 2013 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.keyboard.internal;

import android.content.res.TypedArray;

import helium314.keyboard.latin.R;
import helium314.keyboard.latin.common.ColorType;
import helium314.keyboard.latin.settings.Defaults;
import helium314.keyboard.latin.settings.Settings;
import helium314.keyboard.latin.settings.SettingsValues;

/**
 * This class holds parameters to control how a gesture trail is drawn and animated on the screen.
 * <p>
 * On the other hand, {@link GestureStrokeDrawingParams} class controls how each gesture stroke is
 * sampled and interpolated. This class controls how those gesture strokes are displayed as a
 * gesture trail and animated on the screen.
 */
final class GestureTrailDrawingParams {
    private static final int FADEOUT_START_DELAY_FOR_DEBUG = 2000; // millisecond
    private static final int FADEOUT_DURATION_FOR_DEBUG = 200; // millisecond

    public final int mTrailColor;
    public final float mTrailStartWidth;
    public final float mTrailEndWidth;
    public final float mTrailBodyRatio;
    public final boolean mTrailShadowEnabled;
    public final float mTrailShadowRatio;
    public final int mFadeoutStartDelay;
    public final int mFadeoutDuration;
    public final int mUpdateInterval;
    public final int mTrailColorSaturation;
    public final int mTrailColorHueStep;

    public final int mTrailLingerDuration;

    GestureTrailDrawingParams(
            final int trailColor,
            final float trailStartWidth,
            final float trailEndWidth,
            final float trailBodyRatio,
            final boolean trailShadowEnabled,
            final float trailShadowRatio,
            final int fadeoutStartDelay,
            final int fadeoutDuration,
            final int updateInterval) {
        this(trailColor, trailStartWidth, trailEndWidth, trailBodyRatio, trailShadowEnabled,
                trailShadowRatio, fadeoutStartDelay, fadeoutDuration, updateInterval,
                Defaults.PREF_GESTURE_TRAIL_COLOR_SATURATION,
                Defaults.PREF_GESTURE_TRAIL_COLOR_HUE_STEP);
    }

    GestureTrailDrawingParams(
            final int trailColor,
            final float trailStartWidth,
            final float trailEndWidth,
            final float trailBodyRatio,
            final boolean trailShadowEnabled,
            final float trailShadowRatio,
            final int fadeoutStartDelay,
            final int fadeoutDuration,
            final int updateInterval,
            final int trailColorSaturation,
            final int trailColorHueStep) {
        mTrailColor = trailColor;
        mTrailStartWidth = trailStartWidth;
        mTrailEndWidth = trailEndWidth;
        mTrailBodyRatio = trailBodyRatio;
        mTrailShadowEnabled = trailShadowEnabled;
        mTrailShadowRatio = trailShadowRatio;
        mFadeoutStartDelay = fadeoutStartDelay;
        mFadeoutDuration = fadeoutDuration;
        mTrailLingerDuration = mFadeoutStartDelay + mFadeoutDuration;
        mUpdateInterval = updateInterval;
        mTrailColorSaturation = trailColorSaturation;
        mTrailColorHueStep = trailColorHueStep;
    }

    public GestureTrailDrawingParams(final TypedArray mainKeyboardViewAttr) {
        final SettingsValues sv = Settings.getValues();
        mTrailColor = sv.mColors.get(ColorType.GESTURE_TRAIL);
        mTrailStartWidth = mainKeyboardViewAttr.getDimension(
                R.styleable.MainKeyboardView_gestureTrailStartWidth, 0.0f);
        mTrailEndWidth = mainKeyboardViewAttr.getDimension(
                R.styleable.MainKeyboardView_gestureTrailEndWidth, 0.0f);
        final int PERCENTAGE_INT = 100;
        mTrailBodyRatio = (float)mainKeyboardViewAttr.getInt(
                R.styleable.MainKeyboardView_gestureTrailBodyRatio, PERCENTAGE_INT)
                / (float)PERCENTAGE_INT;
        final int trailShadowRatioInt = mainKeyboardViewAttr.getInt(
                R.styleable.MainKeyboardView_gestureTrailShadowRatio, 0);
        mTrailShadowEnabled = (trailShadowRatioInt > 0);
        mTrailShadowRatio = (float)trailShadowRatioInt / (float)PERCENTAGE_INT;
        mFadeoutStartDelay = GestureTrailDrawingPoints.DEBUG_SHOW_POINTS
                ? FADEOUT_START_DELAY_FOR_DEBUG
                : mainKeyboardViewAttr.getInt(
                        R.styleable.MainKeyboardView_gestureTrailFadeoutStartDelay, 0);
        mFadeoutDuration = GestureTrailDrawingPoints.DEBUG_SHOW_POINTS
                ? FADEOUT_DURATION_FOR_DEBUG
                : sv.mGestureTrailFadeoutDuration;
        mTrailLingerDuration = mFadeoutStartDelay + mFadeoutDuration;
        mUpdateInterval = mainKeyboardViewAttr.getInt(
                R.styleable.MainKeyboardView_gestureTrailUpdateInterval, 0);
        mTrailColorSaturation = sv.mGestureTrailColorSaturation;
        mTrailColorHueStep = sv.mGestureTrailColorHueStep;
    }
}
