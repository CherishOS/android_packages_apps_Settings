/*
 * Copyright (C) 2023 The PixelDust Project
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

import static android.provider.Settings.Secure.TORCH_DOUBLE_TAP_POWER_GESTURE_ENABLED;
import static android.provider.Settings.Secure.TORCH_LONG_PRESS_POWER;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.gestures.GesturePreferenceController;

public class PowerButtonTorchGesturePreferenceController extends GesturePreferenceController {

    private final int ON = 1;
    private final int OFF = 0;

    private final String PREF_KEY_VIDEO = "gesture_quick_torch_video";
    private final String PREF_KEY_TORCH = "power_button_torch";

    public PowerButtonTorchGesturePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) ? AVAILABLE :
                        UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), PREF_KEY_TORCH);
    }

    @Override
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    @Override
    public boolean isChecked() {
        return PowerMenuSettingsUtils.isDoubleTapPowerForTorchEnabled(mContext) ||
                PowerMenuSettingsUtils.isLongPressPowerForTorchEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        if (isChecked) {
            return Settings.Secure.putIntForUser(mContext.getContentResolver(), TORCH_LONG_PRESS_POWER,
                    ON, UserHandle.USER_CURRENT);
        } else {
            Settings.Secure.putIntForUser(mContext.getContentResolver(), TORCH_LONG_PRESS_POWER,
                    OFF, UserHandle.USER_CURRENT);
            Settings.Secure.putIntForUser(mContext.getContentResolver(), TORCH_DOUBLE_TAP_POWER_GESTURE_ENABLED,
                    OFF, UserHandle.USER_CURRENT);
            return true;
        }
    }
}
