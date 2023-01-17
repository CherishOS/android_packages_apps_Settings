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

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.SelectorWithWidgetPreference;

/**
 * Configures the behaviour of the radio selector to configure double tap power button for torch.
 */
public class DoubleTapPowerForTorchPreferenceController extends BasePreferenceController
        implements PowerMenuSettingsUtils.SettingsStateCallback,
                SelectorWithWidgetPreference.OnClickListener,
                LifecycleObserver {

    private SelectorWithWidgetPreference mPreference;
    private final PowerMenuSettingsUtils mUtils;
    private Context mContext;

    private static final String PREF_KEY_POWER_BUTTON_TORCH_LP = "gesture_power_button_torch_long_press";

    public DoubleTapPowerForTorchPreferenceController(Context context, String key) {
        super(context, key);
        mContext = context;
        mUtils = new PowerMenuSettingsUtils(context);
    }

    @Override
    public int getAvailabilityStatus() {
        if (isQuickOpenCameraGestureEnabled()) {
            return DISABLED_DEPENDENT_SETTING;
        }
        
        if (mContext.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return AVAILABLE;
        } else {
            return UNSUPPORTED_ON_DEVICE;
        }
    }

    private boolean isQuickOpenCameraGestureEnabled() {
        final int DOUBLE_TAP_POWER_LAUNCH_CAMERA_MODE = 1;
        final int DOUBLE_TAP_POWER_MULTI_TARGET_MODE = 2;
        final int LAUNCH_CAMERA_ON_DOUBLE_TAP_POWER = 0;

        int gestureMode = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_doubleTapPowerGestureMode);
        
        boolean hasWalletOption = (gestureMode == DOUBLE_TAP_POWER_MULTI_TARGET_MODE);

        if (!hasWalletOption) {
            // Legacy or camera-only mode.
            boolean isGestureSupported = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
            boolean isGestureEnabledByUser = Settings.Secure.getIntForUser(
                mContext.getContentResolver(),
                Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0, mContext.getUserId()) == 0;
            return isGestureSupported && isGestureEnabledByUser;
        } else {
            // Multi-target mode (camera or wallet).
            boolean isMultiTargetGestureEnabled = Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.DOUBLE_TAP_POWER_BUTTON_GESTURE_ENABLED, 1, mContext.getUserId()) == 1;

            int action = Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.DOUBLE_TAP_POWER_BUTTON_GESTURE,
                    LAUNCH_CAMERA_ON_DOUBLE_TAP_POWER, // default to camera
                    mContext.getUserId());
            
            return isMultiTargetGestureEnabled && (action == LAUNCH_CAMERA_ON_DOUBLE_TAP_POWER);
        }
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (mPreference != null) {
            mPreference.setOnClickListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SelectorWithWidgetPreference) {
            ((SelectorWithWidgetPreference) preference)
                    .setChecked(
                            PowerMenuSettingsUtils.isDoubleTapPowerForTorchEnabled(mContext));
        }
        preference.setEnabled(getAvailabilityStatus() == AVAILABLE);
    }

    @Override
    public CharSequence getSummary() {
        if (isQuickOpenCameraGestureEnabled()) {
            return mContext.getString(R.string.double_tap_power_for_torch_summary_disabled);
        }
        return "";
    }

@Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference preference) {
        if (PowerMenuSettingsUtils.isDoubleTapPowerForTorchEnabled(mContext)) {
            return;
        }

    Settings.Secure.putIntForUser(
        mContext.getContentResolver(),
        Settings.Secure.TORCH_DOUBLE_TAP_POWER_GESTURE_ENABLED,
        1,
        UserHandle.USER_CURRENT
    );

    // Then, turn OFF the other one.
    Settings.Secure.putIntForUser(
        mContext.getContentResolver(),
        Settings.Secure.TORCH_LONG_PRESS_POWER,
        0,
        UserHandle.USER_CURRENT
    );
}

    @Override
    public void onChange(Uri uri) {
        if (mPreference != null) {
            updateState(mPreference);
        }
    }

    /** @OnLifecycleEvent(Lifecycle.Event.ON_START) */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        mUtils.registerObserver(this);
    }

    /** @OnLifecycleEvent(Lifecycle.Event.ON_STOP) */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        mUtils.unregisterObserver();
    }
}
