/*
 * Copyright (C) 2023 the CherishOS Android Project
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

package com.android.settings.preferences.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemProperties
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.DeviceInfoUtils
import com.android.settingslib.widget.LayoutPreference

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class mtxInfoPreferenceController(context: Context) : AbstractPreferenceController(context) {

    private val defaultFallback = mContext.getString(R.string.device_info_default)

    private fun getPropertyOrDefault(propName: String): String {
        return SystemProperties.get(propName, defaultFallback)
    }

    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun getCherishBuildVersion(): String {
        return getPropertyOrDefault(PROP_CHERISH_BUILD_VERSION)
    }

    private fun getCherishStorage(): String {
        return SystemProperties.get(PROP_CHERISH_RAM, "0gb") + "/" + SystemProperties.get(PROP_CHERISH_STORAGE, "0gb")
    }

    private fun getCherishChipset(): String {
        return getPropertyOrDefault(PROP_CHERISH_CHIPSET)
    }

    private fun getCherishBattery(): String {
        return getPropertyOrDefault(PROP_CHERISH_BATTERY)
    }
    
    private fun getCherishResolution(): String {
        return getPropertyOrDefault(PROP_CHERISH_DISPLAY)
    }

    private fun getCherishSecurity(): String {
        return getPropertyOrDefault(PROP_CHERISH_SECURITY)
    }

    private fun getCherishVersion(): String {
        return SystemProperties.get(PROP_CHERISH_VERSION)
    }

    private fun getCherishReleaseType(): String {
        val releaseType = getPropertyOrDefault(PROP_CHERISH_RELEASETYPE)
        return releaseType.substring(0, 1).uppercase() +
               releaseType.substring(1).lowercase()
    }

    private fun getCherishBuildStatus(releaseType: String): String {
        return mContext.getString(if (releaseType == "official") R.string.build_is_official_title else R.string.build_is_community_title)
    }

    private fun getCherishMaintainer(releaseType: String): String {
        val cherishMaintainer = getPropertyOrDefault(PROP_CHERISH_MAINTAINER)
        if (cherishMaintainer.equals("Unknown", ignoreCase = true)) {
            return mContext.getString(R.string.unknown_maintainer)
        }
        return mContext.getString(R.string.maintainer_summary, cherishMaintainer)
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val releaseType = getPropertyOrDefault(PROP_CHERISH_RELEASETYPE).lowercase()
        val cherishMaintainer = getCherishMaintainer(releaseType)
        val isOfficial = releaseType == "OFFICIAL"
        
        val hwInfoPreference = screen.findPreference<LayoutPreference>(KEY_HW_INFO)!!
        val swInfoPreference = screen.findPreference<LayoutPreference>(KEY_SW_INFO)!!
        val sw2InfoPreference = screen.findPreference<LayoutPreference>(KEY_SW2_INFO)!!
        val deviceInfoPreference = screen.findPreference<LayoutPreference>(KEY_DEVICE_INFO)!!
        val aboutHwInfoView: View = hwInfoPreference.findViewById(R.id.about_device_hardware)
        val hwInfoView: View = hwInfoPreference.findViewById(R.id.device_hardware)
        val phoneImage: View = hwInfoPreference.findViewById(R.id.phone_image_container)
        val blurView: View = hwInfoPreference.findViewById(R.id.blurView)

        deviceInfoPreference.apply {
            findViewById<TextView>(R.id.firmware_version).text = "Cherish" + " " + getCherishVersion()
            findViewById<TextView>(R.id.firmware_build_summary).text = cherishMaintainer
            findViewById<TextView>(R.id.build_variant_title).text = getCherishBuildStatus(releaseType)
        }

        hwInfoPreference.apply {
            findViewById<TextView>(R.id.device_name).text = getDeviceName()
            findViewById<TextView>(R.id.device_chipset).text = getCherishChipset()
            findViewById<TextView>(R.id.device_storage).text = getCherishStorage()
            findViewById<TextView>(R.id.device_battery_capacity).text = getCherishBattery()
            findViewById<TextView>(R.id.device_resolution).text = getCherishResolution()
            findViewById<TextView>(R.id.device_name_model).text = getDeviceName()
        }

        aboutHwInfoView.setOnClickListener {
            if (hwInfoView.visibility == View.VISIBLE) {
                hwInfoView.visibility = View.GONE
                blurView.visibility = View.GONE
                phoneImage.visibility = View.VISIBLE
            } else {
                hwInfoView.visibility = View.VISIBLE
                blurView.visibility = View.VISIBLE
                phoneImage.visibility = View.GONE
            }
        }

        swInfoPreference.apply {
            findViewById<TextView>(R.id.android_version_summary).text = mContext.getString(R.string.device_info_platform_version)
        }
        
        sw2InfoPreference.apply {
            findViewById<TextView>(R.id.security_patch_summary).text = getCherishSecurity()
            findViewById<TextView>(R.id.kernel_info_summary).text = DeviceInfoUtils.getFormattedKernelVersion(mContext)
        }

        val clickMap = mapOf(
            R.id.android_version_details to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$FirmwareVersionActivity"))
           // R.id.chipset_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DevRunningServicesActivity")),
          //  R.id.display_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DisplaySettingsActivity")),
           // R.id.storage_info to Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$StorageDashboardActivity"))
       )

        clickMap.forEach { (id, intent) ->
            swInfoPreference.findViewById<View>(id)?.setOnClickListener {
                mContext.startActivity(intent)
            }
       }
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun getPreferenceKey(): String {
        return KEY_DEVICE_INFO
    }

    companion object {
        private const val KEY_HW_INFO = "my_device_hw_header"
        private const val KEY_SW_INFO = "my_device_sw_header"
        private const val KEY_SW2_INFO = "my_device_sw2_header"
        private const val KEY_DEVICE_INFO = "my_device_info_header"
        
        private const val KEY_STORAGE = "device_storage"
        private const val KEY_CHIPSET = "device_chipset"
        private const val KEY_BATTERY = "device_battery_capacity"
        private const val KEY_DISPLAY = "device_resolution"

        private const val PROP_CHERISH_VERSION = "ro.cherish.version"
        private const val PROP_CHERISH_RELEASETYPE = "ro.cherish.build_type"
        private const val PROP_CHERISH_MAINTAINER = "ro.cherish.maintainer"
        private const val PROP_CHERISH_DEVICE = "ro.cherish.device"
        private const val PROP_CHERISH_BUILD_TYPE = "ro.cherish.build_type"
        private const val PROP_CHERISH_BUILD_VERSION = "ro.cherish.version"
        private const val PROP_CHERISH_CHIPSET = "ro.cherish.chipset"
        private const val PROP_CHERISH_STORAGE = "ro.cherish.storage"
        private const val PROP_CHERISH_RAM = "ro.cherish.ram"
        private const val PROP_CHERISH_BATTERY = "ro.cherish.battery"
        private const val PROP_CHERISH_DISPLAY = "ro.cherish.display_resolution"
        private const val PROP_CHERISH_SECURITY = "ro.build.version.security_patch"
    }
}