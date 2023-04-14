 /* Copyright (C) 2021 Project Radiant
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
package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;
import android.widget.TextView;
import android.text.TextUtils;
import com.cherish.utils.SpecUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class InfoPrefsController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_CHERISH_INFO = "cherish_info";
    private static final String KEY_STORAGE = "storage";
    private static final String KEY_CHIPSET = "chipset";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_DISPLAY = "display";

    public InfoPrefsController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference cherishInfoPreference = screen.findPreference(KEY_CHERISH_INFO);
        final TextView chipText = (TextView) cherishInfoPreference.findViewById(R.id.chipset_summary);
        final TextView storText = (TextView) cherishInfoPreference.findViewById(R.id.cust_storage_summary);
        final TextView battText = (TextView) cherishInfoPreference.findViewById(R.id.cust_battery_summary);
        final TextView dispText = (TextView) cherishInfoPreference.findViewById(R.id.cust_display_summary);

        chipText.setText(SpecUtils.getProcessorModel());
        storText.setText(String.valueOf(SpecUtils.getTotalInternalMemorySize()) + "GB ROM + " + SpecUtils.getTotalRAM() + " RAM");
        battText.setText(SpecUtils.getBatteryCapacity(mContext) + " mAh");
        dispText.setText(SpecUtils.getScreenRes(mContext)); 
    }

    @Override
    public String getPreferenceKey() {
        return KEY_STORAGE;
    }
} 
