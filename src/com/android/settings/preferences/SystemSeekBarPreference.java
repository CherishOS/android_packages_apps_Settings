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
import android.view.View;
import android.widget.SeekBar;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.internal.util.cherish.CherishUtils;
import com.android.settings.R;

import com.android.settings.widget.SeekBarPreference;

public class SystemSeekBarPreference extends SeekBarPreference {

    private static final String SYSTEMUI_RESTART = "systemui";
    private static final String SETTINGS_RESTART = "settings";
    private static final String SYSTEM = "system";
    private static final String SECURE = "secure";
    private static final String GLOBAL = "global";
    private static final String NONE = "none";

    private Context mContext;
    private AttributeSet mAttrs;
    private SeekBar mSeekBar;

    public SystemSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context,
                R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
        mContext = context;
        mAttrs = attrs;
        setLayoutResource(R.layout.preference_system_seekbar);
    }
    
    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        mSeekBar = (SeekBar) view.findViewById(com.android.internal.R.id.seekbar);
        init();
    }

    private void init() {
        if (mSeekBar == null) {
            return;
        }
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
        int currentValue;
        switch (settingsKey) {
            case SYSTEM:
            default:
                currentValue = Settings.System.getIntForUser(mContext.getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT);
                break;
            case SECURE:
                currentValue = Settings.Secure.getIntForUser(mContext.getContentResolver(), getKey(), 0, UserHandle.USER_CURRENT);
                break;
            case GLOBAL:
                currentValue = Settings.Global.getInt(mContext.getContentResolver(), getKey(), 0);
                break;
        }
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = (int) newValue;
                mSeekBar.setProgress(value);
                switch (settingsKey) {
                    case SYSTEM:
                    default:
                        Settings.System.putIntForUser(mContext.getContentResolver(), getKey(), value, UserHandle.USER_CURRENT);
                        break;
                    case SECURE:
                        Settings.Secure.putIntForUser(mContext.getContentResolver(), getKey(), value, UserHandle.USER_CURRENT);
                        break;
                    case GLOBAL:
                        Settings.Global.putInt(mContext.getContentResolver(), getKey(), value);
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
        mSeekBar.setProgress(currentValue);
    }
}
