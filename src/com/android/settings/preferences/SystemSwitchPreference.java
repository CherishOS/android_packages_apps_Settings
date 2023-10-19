/*
 * Copyright (C) 2023 The risingOS Android Project
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
 
package com.android.settings.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;

import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.internal.util.cherish.CherishUtils;

import com.android.settings.R;
import com.android.settings.preferences.ui.AdaptivePreferenceUtils;

public class SystemSwitchPreference extends SwitchPreference {

    private static final String PREFS_NAME = "system_store_";
    private static final String SYSTEMUI_RESTART = "systemui";
    private static final String SETTINGS_RESTART = "settings";
    private static final String NONE = "none";
    private static final String SYSTEM = "system";
    private static final String SECURE = "secure";
    private static final String GLOBAL = "global";

    private Context mContext;
    private AttributeSet mAttrs;

    public SystemSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        initLayout(context, attrs);
    }
    
    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        init();
    }

    private void initLayout(Context context, AttributeSet attrs) {
        int layoutResId = AdaptivePreferenceUtils.getLayoutResourceId(context, attrs);
        setLayoutResource(layoutResId);
    }

    private void init() {
        TypedArray typedArray = mContext.obtainStyledAttributes(mAttrs, R.styleable.SystemPreference);
        String restartLevel = typedArray.getString(R.styleable.SystemPreference_restart_level);
        if (restartLevel == null || restartLevel.isEmpty()) {
            restartLevel = "none";
        }
        String settingsType = typedArray.getString(R.styleable.SystemPreference_settings_type);
        if (settingsType == null || settingsType.isEmpty()) {
            settingsType = "system";
        }
        typedArray.recycle();
        final String settingsKey = settingsType;
        final String restartKey = restartLevel;
        boolean isChecked;
        switch (settingsKey) {
            case SYSTEM:
            default:
                isChecked = Settings.System.getIntForUser(mContext.getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT) != 0;
                break;
            case SECURE:
                isChecked = Settings.Secure.getIntForUser(mContext.getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT) != 0;
                break;
            case GLOBAL:
                isChecked = Settings.Global.getInt(mContext.getContentResolver(), getKey(), 0) != 0;
                break;
        }
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean value = (boolean) newValue;
                int intValue = value ? 1 : 0;
                setChecked(value);
                switch (settingsKey) {
                    case SYSTEM:
                    default:
                        Settings.System.putIntForUser(mContext.getContentResolver(), getKey(), intValue, UserHandle.USER_CURRENT);
                        break;
                    case SECURE:
                        Settings.Secure.putIntForUser(mContext.getContentResolver(), getKey(), intValue, UserHandle.USER_CURRENT);
                        break;
                    case GLOBAL:
                        Settings.Global.putInt(mContext.getContentResolver(), getKey(), intValue);
                        break;
                }
                switch (restartKey) {
                    case SYSTEM:
                        CherishUtils.showSystemUiRestartDialog(mContext);
                        break;
                    case SYSTEMUI_RESTART:
                        CherishUtils.showSystemRestartDialog(mContext);
                        break;
                    case SETTINGS_RESTART:
                        CherishUtils.showSettingsRestartDialog(mContext);
                        break;
                    case NONE:
                    default:
                        break;
                }
                return true;
            }
        });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setChecked(isChecked);
            }
        });
    }
}
