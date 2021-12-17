/*
 * Copyright (C) 2021 AOSP-Krypton Project
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

package com.android.settings.gestures

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings

import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.PreferenceScreen

import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle

class DoubleTapPowerTorchPreferenceController(
    context: Context,
    lifecycle: Lifecycle,
): BasePreferenceController(context, KEY), LifecycleEventObserver {

    init {
        lifecycle.addObserver(this)
    }

    private val settingsObserver = object: ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            preference?.let { updateState(it) }
        }
    }

    private var preference: Preference? = null

    override fun onStateChanged(owner: LifecycleOwner, event: Event) {
        if (event == Event.ON_START) {
            mContext.contentResolver.registerContentObserver(Settings.Secure.getUriFor(
                Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED),
                false, settingsObserver)
        } else if (event == Event.ON_STOP) {
            mContext.contentResolver.unregisterContentObserver(settingsObserver)
        }
    }

    override fun getAvailabilityStatus(): Int {
        val isQuickOpenCameraGestureEnabled = mContext.resources.getBoolean(
            com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled)
        val isQuickOpenCameraGestureEnabledUser = Settings.Secure.getInt(mContext.contentResolver,
                Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0) == 0
        return if (isQuickOpenCameraGestureEnabled && isQuickOpenCameraGestureEnabledUser)
            DISABLED_DEPENDENT_SETTING else AVAILABLE
    }

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        preference.setEnabled(getAvailabilityStatus() == AVAILABLE)
    }

    override fun getSummary(): CharSequence =
        if (getAvailabilityStatus() == AVAILABLE)
            mContext.getString(R.string.double_tap_power_for_torch_summary_enabled)
        else
            mContext.getString(R.string.double_tap_power_for_torch_summary_disabled)

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }

    companion object {
        private const val KEY = "torch_double_tap_power_gesture_enabled"
    }
}
