/*
 * Copyright (C) 2019 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.gestures;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

/**
 * Dialog to set the back gesture's sensitivity in Gesture navigation mode.
 */
public class GestureNavigationBackSensitivityDialog extends InstrumentedDialogFragment {
    private boolean mArrowSwitchChecked;
    private boolean mBlockIMESwitchChecked;
    private boolean mHapticSwitchChecked;

    private static final String TAG = "GestureNavigationBackSensitivityDialog";
    private static final String KEY_BACK_SENSITIVITY = "back_sensitivity";
    private static final String KEY_BACK_HEIGHT = "back_height";
	 private static final String KEY_HOME_HANDLE_SIZE = "home_handle_width";
    private static final String KEY_HOME_HANDLE_HEIGHT = "home_handle_height";

    public static void show(SystemNavigationGestureSettings parent, int sensitivity,
            int height, int length, int handleHeight) {
        if (!parent.isAdded()) {
            return;
        }

        final GestureNavigationBackSensitivityDialog dialog =
                new GestureNavigationBackSensitivityDialog();
        final Bundle bundle = new Bundle();
        bundle.putInt(KEY_BACK_SENSITIVITY, sensitivity);
        bundle.putInt(KEY_BACK_HEIGHT, height);
        bundle.putInt(KEY_HOME_HANDLE_SIZE, length);
        bundle.putInt(KEY_HOME_HANDLE_HEIGHT, handleHeight);
        dialog.setArguments(bundle);
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURE_NAV_BACK_SENSITIVITY_DLG;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_back_gesture_options, null);
        final SeekBar seekBarSensitivity = view.findViewById(R.id.back_sensitivity_seekbar);
        seekBarSensitivity.setProgress(getArguments().getInt(KEY_BACK_SENSITIVITY));
        final SeekBar seekBarHeight = view.findViewById(R.id.back_height_seekbar);
        seekBarHeight.setProgress(getArguments().getInt(KEY_BACK_HEIGHT));
        final SeekBar seekBarHandleSize = view.findViewById(R.id.home_handle_seekbar);
        seekBarHandleSize.setProgress(getArguments().getInt(KEY_HOME_HANDLE_SIZE));
		final SeekBar seekBarHandleHeight = view.findViewById(R.id.home_handle_height_seekbar);
        seekBarHandleHeight.setProgress(getArguments().getInt(KEY_HOME_HANDLE_HEIGHT));
        final Switch arrowSwitch = view.findViewById(R.id.back_arrow_gesture_switch);
        mArrowSwitchChecked = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.SHOW_BACK_ARROW_GESTURE, 1) == 1;
        arrowSwitch.setChecked(mArrowSwitchChecked);
        arrowSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrowSwitchChecked = arrowSwitch.isChecked() ? true : false;
            }
        });
        final Switch imeSwitch = view.findViewById(R.id.back_block_ime);
        mBlockIMESwitchChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BACK_GESTURE_BLOCK_IME, 1) == 1;
        imeSwitch.setChecked(mBlockIMESwitchChecked);
        imeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBlockIMESwitchChecked = imeSwitch.isChecked() ? true : false;
            }
        });
        final Switch hapticSwitch = view.findViewById(R.id.back_gesture_haptic);
        mHapticSwitchChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BACK_GESTURE_HAPTIC, 1) == 1;
        hapticSwitch.setChecked(mHapticSwitchChecked);
        hapticSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHapticSwitchChecked = hapticSwitch.isChecked() ? true : false;
            }
        });
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.back_options_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    int sensitivity = seekBarSensitivity.getProgress();
                    getArguments().putInt(KEY_BACK_SENSITIVITY, sensitivity);
                    int height = seekBarHeight.getProgress();
                    getArguments().putInt(KEY_BACK_HEIGHT, height);
                    int length = seekBarHandleSize.getProgress();
                    getArguments().putInt(KEY_HOME_HANDLE_SIZE, length);
                    int handleHeight = seekBarHandleHeight.getProgress();
                    getArguments().putInt(KEY_HOME_HANDLE_HEIGHT, handleHeight);
                    SystemNavigationGestureSettings.setBackHeight(getActivity(), height);
                    SystemNavigationGestureSettings.setHomeHandleSize(getActivity(), length);
                    SystemNavigationGestureSettings.setHomeHandleHeight(getActivity(), handleHeight);
                    SystemNavigationGestureSettings.setBackSensitivity(getActivity(),
                            getOverlayManager(), sensitivity);
                    Settings.Secure.putInt(getActivity().getContentResolver(),
                            Settings.Secure.SHOW_BACK_ARROW_GESTURE, mArrowSwitchChecked ? 1 : 0);
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.BACK_GESTURE_BLOCK_IME, mBlockIMESwitchChecked ? 1 : 0);
                    Settings.System.putInt(getContext().getContentResolver(),
                            Settings.System.BACK_GESTURE_HAPTIC, mHapticSwitchChecked ? 1 : 0);
                })
                .create();
    }

    private IOverlayManager getOverlayManager() {
        return IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
    }
}
