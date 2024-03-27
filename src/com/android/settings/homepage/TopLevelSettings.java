/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.homepage;

import static com.android.settings.search.actionbar.SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR;
import static com.android.settingslib.search.SearchIndexable.MOBILE;

import android.app.ActivityManager;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;
import androidx.window.embedding.ActivityEmbeddingController;

import com.android.internal.util.UserIcons;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.activityembedding.ActivityEmbeddingRulesController;
import com.android.settings.activityembedding.ActivityEmbeddingUtils;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.support.SupportPreferenceController;
import com.android.settings.widget.HomepagePreference;
import com.android.settings.widget.HomepagePreferenceLayoutHelper.HomepagePreferenceLayout;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.drawable.CircleFramedDrawable;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

@SearchIndexable(forTarget = MOBILE)
public class TopLevelSettings extends DashboardFragment implements SplitLayoutListener,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "TopLevelSettings";
    private static final String SAVED_HIGHLIGHT_MIXIN = "highlight_mixin";
    private static final String PREF_KEY_SUPPORT = "top_level_support";

    private boolean mIsEmbeddingActivityEnabled;
    private TopLevelHighlightMixin mHighlightMixin;
    private int mPaddingHorizontal;
    private boolean mScrollNeeded = true;
    private boolean mFirstStarted = true;
    private ActivityEmbeddingController mActivityEmbeddingController;
    
    private boolean googleServicesAvailable;
    private int extraPreferenceOrder = -151;

    public TopLevelSettings() {
        final Bundle args = new Bundle();
        // Disable the search icon because this page uses a full search view in actionbar.
        args.putBoolean(NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        setArguments(args);
    }

    /** Dependency injection ctor only for testing. */
    @VisibleForTesting
    public TopLevelSettings(TopLevelHighlightMixin highlightMixin) {
        this();
        mHighlightMixin = highlightMixin;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.top_level_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DASHBOARD_SUMMARY;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        HighlightableMenu.fromXml(context, getPreferenceScreenResId());
        use(SupportPreferenceController.class).setActivity(getActivity());
    }

    @Override
    public int getHelpResource() {
        // Disable the help icon because this page uses a full search view in actionbar.
        return 0;
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (isDuplicateClick(preference)) {
            return true;
        }

        // Register SplitPairRule for SubSettings.
        ActivityEmbeddingRulesController.registerSubSettingsPairRule(getContext(),
                true /* clearTop */);

        setHighlightPreferenceKey(preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        new SubSettingLauncher(getActivity())
                .setDestination(pref.getFragment())
                .setArguments(pref.getExtras())
                .setSourceMetricsCategory(caller instanceof Instrumentable
                        ? ((Instrumentable) caller).getMetricsCategory()
                        : Instrumentable.METRICS_CATEGORY_UNKNOWN)
                .setTitleRes(-1)
                .setIsSecondLayerPage(true)
                .launch();
        return true;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mIsEmbeddingActivityEnabled =
                ActivityEmbeddingUtils.isEmbeddingActivityEnabled(getContext());
        if (!mIsEmbeddingActivityEnabled) {
            return;
        }

        boolean activityEmbedded = isActivityEmbedded();
        if (icicle != null) {
            mHighlightMixin = icicle.getParcelable(SAVED_HIGHLIGHT_MIXIN);
            if (mHighlightMixin != null) {
                mScrollNeeded = !mHighlightMixin.isActivityEmbedded() && activityEmbedded;
                mHighlightMixin.setActivityEmbedded(activityEmbedded);
            }
        }
        if (mHighlightMixin == null) {
            mHighlightMixin = new TopLevelHighlightMixin(activityEmbedded);
        }
    }

    private void initHomepageWidgetsView() {
        final FragmentActivity activity = getActivity();
        final LayoutPreference bannerPreference =
                        (LayoutPreference) getPreferenceScreen().findPreference("top_level_homepage_banner_view");
        final LayoutPreference widgetPreference =
                        (LayoutPreference) getPreferenceScreen().findPreference("top_level_homepage_widgets");
        final LayoutPreference searchWidgetPreference =
                        (LayoutPreference) getPreferenceScreen().findPreference("top_level_search_widget");
        final boolean enableHomepageWidgets = Settings.System.getIntForUser(getContext().getContentResolver(),
                "settings_homepage_widgets", 0, UserHandle.USER_CURRENT) != 0;
        if (bannerPreference != null && enableHomepageWidgets) {
            final ImageView avatarView = bannerPreference.findViewById(R.id.account_avatar);
            avatarView.setImageDrawable(getCircularUserIcon(getActivity()));
            avatarView.bringToFront();
            avatarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchComponent("com.android.settings", "com.android.settings.Settings$UserSettingsActivity");
                }
            });
            final String wppClass = getContext().getResources().getString(R.string.config_styles_and_wallpaper_picker_class);
            final String wppPkg = getContext().getResources().getString(R.string.config_wallpaper_picker_package);
            final String wppExtraIntent = getContext().getResources().getString(R.string.config_wallpaper_picker_launch_extra);
            final Intent wallpaperIntent = new Intent()
                    .setComponent(new ComponentName(wppPkg, wppClass))
                    .putExtra(wppExtraIntent, "app_launched_settings")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            final View bannerView = bannerPreference.findViewById(R.id.homepage_banner);
            bannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(wallpaperIntent);
                }
            });
        }
        if (widgetPreference != null && enableHomepageWidgets) {
            // widgets elements
            final ImageView searchIcon = widgetPreference.findViewById(R.id.search_widget_icon);
            final ImageView systemIcon = widgetPreference.findViewById(R.id.system_widget_icon);
            searchIcon.bringToFront();
            systemIcon.bringToFront();
            systemIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchComponent("com.android.settings", "com.android.settings.Settings$SystemDashboardActivity");
                }
            });

            // widgets
            final View batteryView = widgetPreference.findViewById(R.id.battery_widget);
            batteryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchComponent("com.android.settings", "com.android.settings.Settings$PowerUsageSummaryActivity");
                }
            });
            final View searchView = widgetPreference.findViewById(R.id.search_widget);
            final View systemView = widgetPreference.findViewById(R.id.system_widget);
            systemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchComponent("com.android.settings", "com.android.settings.Settings$SystemDashboardActivity");
                }
            });
            final View storageView = widgetPreference.findViewById(R.id.storage_widget);
            storageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchComponent("com.android.settings", "com.android.settings.Settings$StorageDashboardActivity");
                }
            });
            if (activity != null) {
                FeatureFactory.getFeatureFactory().getSearchFeatureProvider().initSearchToolbar(activity /* activity */, searchView, (View) searchIcon, SettingsEnums.SETTINGS_HOMEPAGE);
            }
        } else {
            if (searchWidgetPreference != null) {
                final ImageView avatarView = searchWidgetPreference.findViewById(R.id.avatar_widget_icon);
                avatarView.setImageDrawable(getCircularUserIcon(getActivity()));
                avatarView.bringToFront();
                avatarView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchComponent("com.android.settings", "com.android.settings.Settings$UserSettingsActivity");
                    }
                });
                final ImageView searchIcon = searchWidgetPreference.findViewById(R.id.search_widget_icon);
                final View searchView = searchWidgetPreference.findViewById(R.id.search_widget);
                final TextView searchTextView = searchWidgetPreference.findViewById(R.id.homepage_search_text);
                searchIcon.bringToFront();
                if (activity != null) {
                    FeatureFactory.getFeatureFactory().getSearchFeatureProvider().initSearchToolbar(activity /* activity */, searchView, (View) searchIcon, SettingsEnums.SETTINGS_HOMEPAGE);
                }
            }
        }
    }

    private Drawable getCircularUserIcon(Context context) {
        final UserManager mUserManager = getSystemService(UserManager.class);
        Bitmap bitmapUserIcon = mUserManager.getUserIcon(UserHandle.myUserId());

        if (bitmapUserIcon == null) {
            // get default user icon.
            final Drawable defaultUserIcon = UserIcons.getDefaultUserIcon(
                    context.getResources(), UserHandle.myUserId(), false);
            bitmapUserIcon = UserIcons.convertToBitmap(defaultUserIcon);
        }
        Drawable drawableUserIcon = new CircleFramedDrawable(bitmapUserIcon,
                (int) context.getResources().getDimension(R.dimen.homepage_user_icon_size));

        return drawableUserIcon;
    }

    private String getOwnerName(){
        final UserManager mUserManager = getSystemService(UserManager.class);
        final UserInfo userInfo = com.android.settings.Utils.getExistingUser(mUserManager,
                    UserHandle.of(UserHandle.myUserId()));
        return userInfo.name != null ? userInfo.name : getString(R.string.default_user);
    }

    private void launchComponent(String packageName, String className) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, className));
        startActivity(intent);
    }

    /** Wrap ActivityEmbeddingController#isActivityEmbedded for testing. */
    @VisibleForTesting
    public boolean isActivityEmbedded() {
        if (mActivityEmbeddingController == null) {
            mActivityEmbeddingController = ActivityEmbeddingController.getInstance(getActivity());
        }
        return mActivityEmbeddingController.isActivityEmbedded(getActivity());
    }

    @Override
    public void onStart() {
        if (mFirstStarted) {
            mFirstStarted = false;
            FeatureFactory.getFeatureFactory().getSearchFeatureProvider().sendPreIndexIntent(
                    getContext());
        } else if (mIsEmbeddingActivityEnabled && isOnlyOneActivityInTask()
                && !isActivityEmbedded()) {
            // Set default highlight menu key for 1-pane homepage since it will show the placeholder
            // page once changing back to 2-pane.
            Log.i(TAG, "Set default menu key");
            setHighlightMenuKey(getString(SettingsHomepageActivity.DEFAULT_HIGHLIGHT_MENU_KEY),
                    /* scrollNeeded= */ false);
        }
        super.onStart();
        RecyclerView recyclerView = getListView();
        if (recyclerView != null) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    initHomepageWidgetsView();
                }
            });
        }
    }

    private boolean isOnlyOneActivityInTask() {
        final ActivityManager.RunningTaskInfo taskInfo = getSystemService(ActivityManager.class)
                .getRunningTasks(1).get(0);
        return taskInfo.numActivities == 1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mHighlightMixin != null) {
            outState.putParcelable(SAVED_HIGHLIGHT_MIXIN, mHighlightMixin);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        final LayoutPreference bannerPreference =
                        (LayoutPreference) getPreferenceScreen().findPreference("top_level_homepage_banner_view");
        final LayoutPreference widgetPreference =
                        (LayoutPreference) getPreferenceScreen().findPreference("top_level_homepage_widgets");
        final LayoutPreference searchWidgetPreference =
                        (LayoutPreference) getPreferenceScreen().findPreference("top_level_search_widget");
        final boolean enableHomepageWidgets = Settings.System.getIntForUser(getContext().getContentResolver(),
                "settings_homepage_widgets", 0, UserHandle.USER_CURRENT) != 0;
        if (!enableHomepageWidgets) {
            if (widgetPreference != null) {
                getPreferenceScreen().removePreference(widgetPreference);
            }
            if (bannerPreference != null) {
                getPreferenceScreen().removePreference(bannerPreference);
            }
        } else {
            if (searchWidgetPreference != null) {
                getPreferenceScreen().removePreference(searchWidgetPreference);
            }
        }
        int tintColor = Utils.getHomepageIconColor(getContext());
        iteratePreferences(preference -> {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                icon.setTint(tintColor);
            }
            String preferenceKey = preference.getKey();
            if (preferenceKey != null && !("top_level_homepage_widgets".equals(preferenceKey) ||
                                           "top_level_homepage_banner_view".equals(preferenceKey)|| 
                                           "top_level_search_widget".equals(preferenceKey))) {
                setUpPreferenceLayout(preference);
            }
        });
    }

    private void setUpPreferenceLayout(Preference preference) {
        String key = preference.getKey();

        //Log.d("PreferenceLogging", "Setting up layout for preference key: " + key);

        Set<String> topPreferences = new HashSet<>(Arrays.asList(
                "top_level_cherish",
                "top_level_system", 
                "top_level_apps",
                "top_level_accessibility",
                "top_level_emergency",
                "top_level_display"
        ));

        Set<String> middlePreferences = new HashSet<>(Arrays.asList(
                "top_level_network",
                "top_level_battery", 
                "top_level_security",
                "top_level_privacy", 
                "top_level_storage", 
                "top_level_notifications",
                "top_level_communal",
                "top_level_safety_center"
        ));
        
        Set<String> bottomPreferences = new HashSet<>(Arrays.asList(
                "top_level_connected_devices",
                "top_level_sound",
                "top_level_wallpaper",
                "top_level_location",
                "top_level_accounts", 
                "top_level_about_device"
        ));

        if ("top_level_wellbeing".equals(key)) {
            preference.setLayoutResource(R.layout.top_level_preference_wellbeing_card);
        } else if ("top_level_google".equals(key)) {
            preference.setLayoutResource(R.layout.top_level_preference_google_card);
            googleServicesAvailable = true;
        } else if (topPreferences.contains(key)) {
            preference.setLayoutResource(R.layout.top_level_preference_top_card);
        } else if (middlePreferences.contains(key)) {
            preference.setLayoutResource(R.layout.top_level_preference_middle_card);
        } else if (key.equals("top_level_accounts") && googleServicesAvailable) {
            preference.setLayoutResource(R.layout.top_level_preference_middle_card);
        } else if (bottomPreferences.contains(key)) {
            preference.setLayoutResource(R.layout.top_level_preference_bottom_card);
        } else {
            // highlight injected top level preference e.g OEM parts
            int order = extraPreferenceOrder - 1;
            extraPreferenceOrder = order;
            preference.setOrder(order);
            preference.setLayoutResource(R.layout.top_level_preference_solo_card);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        highlightPreferenceIfNeeded();
    }

    @Override
    public void onSplitLayoutChanged(boolean isRegularLayout) {
        iteratePreferences(preference -> {
            if (preference instanceof HomepagePreferenceLayout) {
                ((HomepagePreferenceLayout) preference).getHelper().setIconVisible(isRegularLayout);
            }
        });
    }

    @Override
    public void highlightPreferenceIfNeeded() {
        if (mHighlightMixin != null) {
            mHighlightMixin.highlightPreferenceIfNeeded();
        }
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent,
                savedInstanceState);
        recyclerView.setPadding(mPaddingHorizontal, 0, mPaddingHorizontal, 0);
        return recyclerView;
    }

    /** Sets the horizontal padding */
    public void setPaddingHorizontal(int padding) {
        mPaddingHorizontal = padding;
        RecyclerView recyclerView = getListView();
        if (recyclerView != null) {
            recyclerView.setPadding(padding, 0, padding, 0);
        }
    }

    /** Updates the preference internal paddings */
    public void updatePreferencePadding(boolean isTwoPane) {
        iteratePreferences(new PreferenceJob() {
            private int mIconPaddingStart;
            private int mTextPaddingStart;

            @Override
            public void init() {
                mIconPaddingStart = getResources().getDimensionPixelSize(isTwoPane
                        ? R.dimen.homepage_preference_icon_padding_start_two_pane
                        : R.dimen.homepage_preference_icon_padding_start);
                mTextPaddingStart = getResources().getDimensionPixelSize(isTwoPane
                        ? R.dimen.homepage_preference_text_padding_start_two_pane
                        : R.dimen.homepage_preference_text_padding_start);
            }

            @Override
            public void doForEach(Preference preference) {
                if (preference instanceof HomepagePreferenceLayout) {
                    ((HomepagePreferenceLayout) preference).getHelper()
                            .setIconPaddingStart(mIconPaddingStart);
                    ((HomepagePreferenceLayout) preference).getHelper()
                            .setTextPaddingStart(mTextPaddingStart);
                }
            }
        });
    }

    /** Returns a {@link TopLevelHighlightMixin} that performs highlighting */
    public TopLevelHighlightMixin getHighlightMixin() {
        return mHighlightMixin;
    }

    /** Highlight a preference with specified preference key */
    public void setHighlightPreferenceKey(String prefKey) {
        // Skip Tips & support since it's full screen
        if (mHighlightMixin != null && !TextUtils.equals(prefKey, PREF_KEY_SUPPORT)) {
            mHighlightMixin.setHighlightPreferenceKey(prefKey);
        }
    }

    /** Returns whether clicking the specified preference is considered as a duplicate click. */
    public boolean isDuplicateClick(Preference pref) {
        /* Return true if
         * 1. the device supports activity embedding, and
         * 2. the target preference is highlighted, and
         * 3. the current activity is embedded */
        return mHighlightMixin != null
                && TextUtils.equals(pref.getKey(), mHighlightMixin.getHighlightPreferenceKey())
                && isActivityEmbedded();
    }

    /** Show/hide the highlight on the menu entry for the search page presence */
    public void setMenuHighlightShowed(boolean show) {
        if (mHighlightMixin != null) {
            mHighlightMixin.setMenuHighlightShowed(show);
        }
    }

    /** Highlight and scroll to a preference with specified menu key */
    public void setHighlightMenuKey(String menuKey, boolean scrollNeeded) {
        if (mHighlightMixin != null) {
            mHighlightMixin.setHighlightMenuKey(menuKey, scrollNeeded);
        }
    }

    @Override
    protected boolean shouldForceRoundedIcon() {
        return getContext().getResources()
                .getBoolean(R.bool.config_force_rounded_icon_TopLevelSettings);
    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        if (!mIsEmbeddingActivityEnabled || !(getActivity() instanceof SettingsHomepageActivity)) {
            return super.onCreateAdapter(preferenceScreen);
        }
        return mHighlightMixin.onCreateAdapter(this, preferenceScreen, mScrollNeeded);
    }

    @Override
    protected Preference createPreference(Tile tile) {
        return new HomepagePreference(getPrefContext());
    }

    void reloadHighlightMenuKey() {
        if (mHighlightMixin != null) {
            mHighlightMixin.reloadHighlightMenuKey(getArguments());
        }
    }

    private void iteratePreferences(PreferenceJob job) {
        if (job == null || getPreferenceManager() == null) {
            return;
        }
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }

        job.init();
        int count = screen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = screen.getPreference(i);
            if (preference == null) {
                break;
            }
            job.doForEach(preference);
        }
    }

    private interface PreferenceJob {
        default void init() {
        }

        void doForEach(Preference preference);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.top_level_settings) {

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Never searchable, all entries in this page are already indexed elsewhere.
                    return false;
                }
            };
}
