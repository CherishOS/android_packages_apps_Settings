/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2023 the RisingOS Android Project
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
import android.os.Build;
import android.os.SystemProperties;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class cherishInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_CHERISH_INFO = "cherish_info";
    private static final String KEY_CHERISH_DEVICE = "cherish_device";
    private static final String KEY_CHERISH_VERSION = "cherish_version";
    private static final String KEY_BUILD_STATUS = "rom_build_status";
    private static final String KEY_BUILD_VERSION = "cherish_build_version";
    
    private static final String PROP_CHERISH_VERSION = "ro.cherish.version";
    private static final String PROP_CHERISH_RELEASETYPE = "ro.cherish.build_type";
    private static final String PROP_CHERISH_MAINTAINER = "ro.cherish.maintainer";
    private static final String PROP_CHERISH_DEVICE = "ro.cherish.device";
    private static final String PROP_CHERISH_BUILD_VERSION = "ro.cherish.version.display";


    public cherishInfoPreferenceController(Context context) {
        super(context);
    }

    private String getDeviceName() {
        String device = SystemProperties.get(PROP_CHERISH_DEVICE, "");
        if (device.equals("")) {
            device = Build.MANUFACTURER + " " + Build.MODEL;
        }
        return device;
    }

    private String getCherishBuildVersion() {
        final String buildVer = SystemProperties.get(PROP_CHERISH_BUILD_VERSION,
                this.mContext.getString(R.string.device_info_default));;

        return buildVer;
    }
    
    private String getCherishVersion() {
        final String version = SystemProperties.get(PROP_CHERISH_VERSION,
                this.mContext.getString(R.string.device_info_default));

        return version;
    }

    private String getCherishReleaseType() {
        final String releaseType = SystemProperties.get(PROP_CHERISH_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
	
        return releaseType.substring(0, 1).toUpperCase() +
                 releaseType.substring(1).toLowerCase();
    }
    
    private String getCherishbuildStatus() {
	final String buildType = SystemProperties.get(PROP_CHERISH_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String isOfficial = this.mContext.getString(R.string.build_is_official_title);
	final String isCommunity = this.mContext.getString(R.string.build_is_community_title);
	
	if (buildType.toLowerCase().equals("official")) {
		return isOfficial;
	} else {
		return isCommunity;
	}
    }

    private String getCherishMaintainer() {
	final String CherishMaintainer = SystemProperties.get(PROP_CHERISH_MAINTAINER,
                this.mContext.getString(R.string.device_info_default));
	final String buildType = SystemProperties.get(PROP_CHERISH_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String isOffFine = this.mContext.getString(R.string.build_is_official_summary, CherishMaintainer);
	final String isOffMiss = this.mContext.getString(R.string.build_is_official_summary_oopsie);
	final String isCommMiss = this.mContext.getString(R.string.build_is_community_summary_oopsie);
	final String isCommFine = this.mContext.getString(R.string.build_is_community_summary, CherishMaintainer);
	
	if (buildType.toLowerCase().equals("official") && !CherishMaintainer.equalsIgnoreCase("Unknown")) {
	    return isOffFine;
	} else if (buildType.toLowerCase().equals("official") && CherishMaintainer.equalsIgnoreCase("Unknown")) {
	     return isOffMiss;
	} else if (buildType.equalsIgnoreCase("Unofficial") && CherishMaintainer.equalsIgnoreCase("Unknown")) {
	     return isCommMiss;
	} else {
	    return isCommFine;
	}
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference arcVerPref = screen.findPreference(KEY_CHERISH_VERSION);
        final Preference arcDevPref = screen.findPreference(KEY_CHERISH_DEVICE);
        final Preference buildStatusPref = screen.findPreference(KEY_BUILD_STATUS);
        final Preference buildVerPref = screen.findPreference(KEY_BUILD_VERSION);
        final String CherishVersion = getCherishVersion();
        final String CherishDevice = getDeviceName();
        final String CherishReleaseType = getCherishReleaseType();
        final String CherishMaintainer = getCherishMaintainer();
	final String buildStatus = getCherishbuildStatus();
	final String buildVer = getCherishBuildVersion();
	final String isOfficial = SystemProperties.get(PROP_CHERISH_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
	buildStatusPref.setTitle(buildStatus);
	buildStatusPref.setSummary(CherishMaintainer);
	buildVerPref.setSummary(buildVer);
        arcVerPref.setSummary(CherishVersion);
        arcDevPref.setSummary(CherishDevice);
	if (isOfficial.toLowerCase().contains("official")) {
		 buildStatusPref.setIcon(R.drawable.verified);
	} else {
		buildStatusPref.setIcon(R.drawable.unverified);
	}
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
