/*
 * Copyright (C) 2024 The Android Open Source Project
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
import android.provider.Settings;
import android.text.TextUtils;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class DoubleTapPowerToOpenCameraPreferenceController extends TogglePreferenceController {

    static final int ON = 0;
    static final int OFF = 1;

    private Preference mPreference;
 
     private final ContentObserver mSettingsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
         @Override
         public void onChange(boolean selfChange) {
             if (mPreference != null) {
                 updateState(mPreference);
             }
         }
     };

    public DoubleTapPowerToOpenCameraPreferenceController(
            @NonNull Context context, @NonNull String key) {
        super(context, key);
    }

    @Override
public int getAvailabilityStatus() {
    boolean isGestureEnabled = mContext.getResources().getBoolean(
            com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    if (isGestureEnabled) {
        return PowerMenuSettingsUtils.isDoubleTapPowerForTorchEnabled(mContext)
                ? DISABLED_DEPENDENT_SETTING
                : AVAILABLE;
    } else {
        return UNSUPPORTED_ON_DEVICE;
    }
}

     public void onStart() {
    mContext.getContentResolver().registerContentObserver(
        Settings.Secure.getUriFor(Settings.Secure.TORCH_DOUBLE_TAP_POWER_GESTURE_ENABLED),
        false,
        mSettingsObserver
    );
}

     public void onStop() {
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
        return Settings.Secure.getInt(
                mContext.getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, ON)
                == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(
                mContext.getContentResolver(),
                CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                isChecked ? ON : OFF);
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
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_system;
    }
}
