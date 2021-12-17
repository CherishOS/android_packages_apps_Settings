/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.gestures;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;

public class DoubleTapPowerPreferenceController extends GesturePreferenceController {

    @VisibleForTesting
    static final int ON = 0;
    @VisibleForTesting
    static final int OFF = 1;

    private static final String PREF_KEY_VIDEO = "gesture_double_tap_power_video";

    private final String SECURE_KEY = CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

    private static final String PREF_KEY_DOUBLE_TAP_POWER_TORCH = "torch_double_tap_power_gesture_enabled";

    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            if (mPreference != null) {
                updateState(mPreference);
            }
        }
    };

    private Preference mPreference;

    public DoubleTapPowerPreferenceController(Context context, String key) {
        super(context, key);
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        return !isGestureAvailable(context)
                || prefs.getBoolean(DoubleTapPowerSettings.PREF_KEY_SUGGESTION_COMPLETE, false);
    }

    private static boolean isGestureAvailable(Context context) {
        return context.getResources()
                .getBoolean(com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }

    @Override
    public int getAvailabilityStatus() {
        if (isGestureAvailable(mContext)) {
            final boolean isDoubleTapPowerTorchGestureEnabled = Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.TORCH_DOUBLE_TAP_POWER_GESTURE_ENABLED, 0) == 1;
            return isDoubleTapPowerTorchGestureEnabled ? DISABLED_DEPENDENT_SETTING : AVAILABLE;
        } else {
            return UNSUPPORTED_ON_DEVICE;
        }
    }

    @Override
    public CharSequence getSummary() {
        return getAvailabilityStatus() == AVAILABLE
            ? super.getSummary()
            : mContext.getString(R.string.gesture_double_tap_power_summary_disabled);
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_double_tap_power");
    }

    @Override
    public boolean isPublicSlice() {
        return true;
    }

    @Override
    protected String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    @Override
    public void onStart() {
        super.onStart();
        mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(
            Settings.Secure.TORCH_DOUBLE_TAP_POWER_GESTURE_ENABLED),
                false, mSettingsObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(getAvailabilityStatus() == AVAILABLE);
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), SECURE_KEY, ON) == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(mContext.getContentResolver(), SECURE_KEY,
                isChecked ? ON : OFF);
    }
}
