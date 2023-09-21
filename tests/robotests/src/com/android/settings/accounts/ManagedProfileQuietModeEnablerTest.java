/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.settings.accounts;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;

import java.util.Collections;


@RunWith(RobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
public class ManagedProfileQuietModeEnablerTest {
    private static final int MANAGED_USER_ID = 10;
    private Context mContext;
    private ManagedProfileQuietModeEnabler mQuietModeEnabler;
    private LifecycleOwner mLifecycleOwner = new LifecycleOwner() {
        public LifecycleRegistry registry = new LifecycleRegistry(this);

        @Override
        public Lifecycle getLifecycle() {
            return registry;
        }
    };

    @Mock
    private ManagedProfileQuietModeEnabler.QuietModeChangeListener mOnQuietModeChangeListener;
    @Mock
    private UserManager mUserManager;
    @Mock
    private UserHandle mManagedUser;
    @Mock
    private UserInfo mUserInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = spy(ApplicationProvider.getApplicationContext());
        when(mContext.getSystemService(Context.USER_SERVICE)).thenReturn(mUserManager);
        when(mUserInfo.isManagedProfile()).thenReturn(true);
        when(mUserManager.getUserInfo(anyInt())).thenReturn(mUserInfo);
        when(mUserManager.getProcessUserId()).thenReturn(0);
        when(mManagedUser.getIdentifier()).thenReturn(MANAGED_USER_ID);
        when(mUserManager.getUserProfiles()).thenReturn(Collections.singletonList(mManagedUser));
        mQuietModeEnabler = new ManagedProfileQuietModeEnabler(mContext,
                mOnQuietModeChangeListener);
    }

    @Test
    public void onSetQuietMode_shouldRequestQuietModeEnabled() {
        mQuietModeEnabler.setQuietModeEnabled(false);
        verify(mUserManager).requestQuietModeEnabled(false, mManagedUser);
        mQuietModeEnabler.setQuietModeEnabled(true);
        verify(mUserManager).requestQuietModeEnabled(true, mManagedUser);
    }

    @Test
    public void onIsQuietModeEnabled_shouldCallIsQuietModeEnabled() {
        assertThat(mQuietModeEnabler.isQuietModeEnabled()).isEqualTo(
                verify(mUserManager).isQuietModeEnabled(any()));
    }

    @Test
    public void onQuietModeChanged_listenerNotified() {
        mQuietModeEnabler.onStart(mLifecycleOwner);
        mContext.sendBroadcast(new Intent(Intent.ACTION_MANAGED_PROFILE_AVAILABLE).putExtra(
                Intent.EXTRA_USER_HANDLE, MANAGED_USER_ID));
        mContext.sendBroadcast(new Intent(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE).putExtra(
                Intent.EXTRA_USER_HANDLE, MANAGED_USER_ID));
        verify(mOnQuietModeChangeListener, times(2)).onQuietModeChanged();
    }

    @Test
    public void onStart_shouldRegisterReceiver() {
        mQuietModeEnabler.onStart(mLifecycleOwner);
        verify(mContext).registerReceiver(eq(mQuietModeEnabler.mReceiver), any(), anyInt());
    }

    @Test
    public void onStop_shouldUnregisterReceiver() {
        // register it first
        mContext.registerReceiver(mQuietModeEnabler.mReceiver, null,
                Context.RECEIVER_EXPORTED/*UNAUDITED*/);

        mQuietModeEnabler.onStop(mLifecycleOwner);
        verify(mContext).unregisterReceiver(mQuietModeEnabler.mReceiver);
    }
}
