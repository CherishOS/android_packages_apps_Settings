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
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.internal.util.cherish.CherishUtils;
import com.android.settings.R;
import java.util.Arrays;

public class SystemListPreference extends ListPreference {

    private static final String SYSTEMUI_RESTART = "systemui";
    private static final String SETTINGS_RESTART = "settings";
    private static final String SYSTEM = "system";
    private static final String SECURE = "secure";
    private static final String GLOBAL = "global";
    private static final String NONE = "none";

    private Context mContext;
    private AttributeSet mAttrs;

    public SystemListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SystemPreference);
        String restartLevel = typedArray.getString(R.styleable.SystemPreference_restart_level);
        if (restartLevel == null || restartLevel.isEmpty()) {
            restartLevel = "none";
        }
        typedArray.recycle();
        final String settingsKey = getSettingsType();
        final String restartKey = restartLevel;
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int settingsValue;
        switch (settingsKey) {
            case SYSTEM:
            default:
                settingsValue = Settings.System.getIntForUser(context.getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT);
                break;
            case SECURE:
                settingsValue = Settings.Secure.getIntForUser(context.getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT);
                break;
            case GLOBAL:
                settingsValue = Settings.Global.getInt(context.getContentResolver(), getKey(), 0);
                break;
        }
        String currentEntry = entries[settingsValue].toString();
        setValue(currentEntry);
        setSummary(currentEntry);
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String) newValue);
                String selectedEntry = entries[value].toString();
                setValue(selectedEntry);
                setSummary(selectedEntry);
                switch (settingsKey) {
                    case SYSTEM:
                    default:
                        Settings.System.putIntForUser(context.getContentResolver(), getKey(), value, UserHandle.USER_CURRENT);
                        break;
                    case SECURE:
                        Settings.Secure.putIntForUser(context.getContentResolver(), getKey(), value, UserHandle.USER_CURRENT);
                        break;
                    case GLOBAL:
                        Settings.Global.putInt(context.getContentResolver(), getKey(), value);
                        break;
                }
                switch (restartKey) {
                    case SYSTEM:
                        CherishUtils.showSystemUiRestartDialog(context);
                        break;
                    case SYSTEMUI_RESTART:
                        CherishUtils.showSystemRestartDialog(context);
                        break;
                    case SETTINGS_RESTART:
                        CherishUtils.showSettingsRestartDialog(context);
                        break;
                    case NONE:
                    default:
                        break;
                }
                return true;
            }
        });
    }
    
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int value;
        switch (getSettingsType()) {
            case SYSTEM:
            default:
                value = Settings.System.getIntForUser(getContext().getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT);
                break;
            case SECURE:
                value = Settings.Secure.getIntForUser(getContext().getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT);
                break;
            case GLOBAL:
                value = Settings.Global.getInt(getContext().getContentResolver(), getKey(), 0);
                break;
        }
        String currentEntry = entries[value].toString();
        setValue(currentEntry);
        setSummary(currentEntry);
        setValueIndex(value);
        setEntries(entries);
        setEntryValues(entryValues);
    }

    private String getSettingsType() {
        TypedArray typedArray = getContext().obtainStyledAttributes(mAttrs, R.styleable.SystemPreference);
        String settingsType = typedArray.getString(R.styleable.SystemPreference_settings_type);
        if (settingsType == null || settingsType.isEmpty()) {
            settingsType = "system";
        }
        typedArray.recycle();
        return settingsType;
    }
}
