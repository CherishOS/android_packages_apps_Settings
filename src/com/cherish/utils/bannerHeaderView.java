/*
 * Copyright (C) 2021 Project Radiant
 * Copyright (C) 2023 riceDroid Android Project
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

package com.cherish.utils;

import android.os.UserHandle;
import android.provider.Settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.concurrent.ThreadLocalRandom;


public class bannerHeaderView extends ImageView {

    Context mContext;

    public bannerHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public bannerHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }


    public bannerHeaderView(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    int mImageDrawable = Settings.System.getIntForUser(mContext.getContentResolver(),
                    "settings_header_image", 1, UserHandle.USER_CURRENT);
    boolean randomBanner = Settings.System.getIntForUser(mContext.getContentResolver(),
                    "settings_header_image_random", 1, UserHandle.USER_CURRENT) == 1;
    int randomBannerImage = ThreadLocalRandom.current().nextInt(1, 98);
	String bannerImage = "banner_" + String.valueOf(randomBanner ?  randomBannerImage : mImageDrawable);
	int resId = getResources().getIdentifier(bannerImage, "drawable", "com.android.settings");
        setImageResource(resId);
    }

}
