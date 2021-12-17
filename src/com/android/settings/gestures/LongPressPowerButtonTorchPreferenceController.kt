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
import androidx.preference.SwitchPreference

import com.android.settings.R
import com.android.settings.core.TogglePreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle

class LongPressPowerButtonTorchPreferenceController(
    context: Context,
    lifecycle: Lifecycle
): TogglePreferenceController(context, KEY), LifecycleEventObserver {

    init {
        lifecycle.addObserver(this)
    }

    private var switchPreference: SwitchPreference? = null
    private var footerPreference: Preference? = null

    private val settingsObserver = object: ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            switchPreference?.let{ updateState(it) }
        }
    }

    override fun onStateChanged(owner: LifecycleOwner, event: Event) {
        if (event == Event.ON_START) {
            mContext.contentResolver.registerContentObserver(Settings.Global.getUriFor(
                Settings.Global.POWER_BUTTON_LONG_PRESS),
                false, settingsObserver)
        } else if (event == Event.ON_STOP) {
            mContext.contentResolver.unregisterContentObserver(settingsObserver)
        }
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        switchPreference = screen.findPreference(preferenceKey)
        footerPreference = screen.findPreference(FOOTER_PREF_KEY)
    }

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        val enabled = getAvailabilityStatus() == AVAILABLE
        preference.setEnabled(enabled)
        footerPreference?.setVisible(enabled && (preference as SwitchPreference).isChecked())
    }

    override fun getAvailabilityStatus(): Int {
        val assistEnabled = mContext.resources.getBoolean(
                com.android.internal.R.bool.config_longPressOnPowerForAssistantSettingAvailable)
        val assistEnabledUser = PowerMenuSettingsUtils.isLongPressPowerForAssistEnabled(mContext)
        return if (assistEnabled && assistEnabledUser) DISABLED_DEPENDENT_SETTING else AVAILABLE
    }

    override fun isChecked() = PowerMenuSettingsUtils.isLongPressPowerForTorchEnabled(mContext)

    override fun setChecked(isChecked: Boolean): Boolean {
        footerPreference?.setVisible(isChecked)
        return Settings.Secure.putInt(mContext.contentResolver,
            Settings.Secure.TORCH_LONG_PRESS_POWER, if (isChecked) 1 else 0)
    }

    companion object {
        private const val KEY = "torch_long_press_power"
        private const val FOOTER_PREF_KEY = "torch_long_press_power_footer"
    }
}
