/*
 * Copyright (C) 2020 Wave-OS
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

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class CherishInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_CHERISH_INFO = "cherish_info";

    private static final String PROP_CHERISH_VERSION = "ro.cherish.version";
    private static final String PROP_CHERISH_VERSION_CODE = "ro.cherish.codename";
    private static final String PROP_CHERISH_RELEASETYPE = "ro.cherish.build_type";
    private static final String PROP_CHERISH_MAINTAINER = "ro.cherish.maintainer";

    public CherishInfoPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference cherishInfoPreference = screen.findPreference(KEY_CHERISH_INFO);
        final TextView version = (TextView) cherishInfoPreference.findViewById(R.id.version_message);
        final TextView versionCode = (TextView) cherishInfoPreference.findViewById(R.id.version_code_message);
        final TextView releaseType = (TextView) cherishInfoPreference.findViewById(R.id.release_type_message);
        final TextView maintainer = (TextView) cherishInfoPreference.findViewById(R.id.maintainer_message);
        final String cherishVersion = SystemProperties.get(PROP_CHERISH_VERSION,
                this.mContext.getString(R.string.device_info_default));
        final String cherishVersionCode = SystemProperties.get(PROP_CHERISH_VERSION_CODE,
                this.mContext.getString(R.string.device_info_default));
        final String cherishReleaseType = SystemProperties.get(PROP_CHERISH_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String cherishMaintainer = SystemProperties.get(PROP_CHERISH_MAINTAINER,
                this.mContext.getString(R.string.device_info_default));
        version.setText(cherishVersion);
        versionCode.setText(cherishVersionCode);
        releaseType.setText(cherishReleaseType);
        maintainer.setText(cherishMaintainer);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CHERISH_INFO;
    }
}
