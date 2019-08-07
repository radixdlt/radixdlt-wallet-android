/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.radixdlt.android.apps.wallet;

import android.app.Activity;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;

/**
 * Copy paste of IntentsTestRule with initialization moved to beforeActivityLaunched.
 */
public class WorkaroundIntentsTestRule<T extends Activity> extends ActivityTestRule<T> {

  private boolean isInitialized;

  public WorkaroundIntentsTestRule(Class<T> activityClass) {
    super(activityClass);
  }

  public WorkaroundIntentsTestRule(Class<T> activityClass, boolean initialTouchMode) {
    super(activityClass, initialTouchMode);
  }

  public WorkaroundIntentsTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
    super(activityClass, initialTouchMode, launchActivity);
  }

  @Override
  protected void beforeActivityLaunched() {
    Intents.init();
    isInitialized = true;
    super.beforeActivityLaunched();
  }

  @Override
  protected void afterActivityFinished() {
    super.afterActivityFinished();
    if (isInitialized) {
      // Otherwise will throw a NPE if Intents.init() wasn't called.
      Intents.release();
      isInitialized = false;
    }
  }
}
