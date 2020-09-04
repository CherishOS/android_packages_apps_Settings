package com.android.settings;
/**
 * Copyright (c) 2020 ZeniX Os Project
 
 * Design by: John Andrew Camu / MrSluffy
 *         Email: werdna.jac@gmail.com or droopsdroops27@gmail.com
 *       Github: www.github.com/MrSluffy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.android.settings.R;

/*  Wew*/
public class TopLevelPreferenceDivider extends Preference {
    private LayoutInflater inflater;
    private Context mContext;

    public TopLevelPreferenceDivider(Context context) {
        super(context);
        initViews(context);
    }

    public TopLevelPreferenceDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public TopLevelPreferenceDivider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(R.layout.top_level_preference_divider);
        setEnabled(false);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        TopLevelPreferenceDivider.super.onBindViewHolder(holder);
        holder.setDividerAllowedBelow(false);
        holder.setDividerAllowedAbove(false);
    }
}
