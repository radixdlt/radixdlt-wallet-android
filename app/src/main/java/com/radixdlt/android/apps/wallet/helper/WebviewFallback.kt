// Copyright 2015 Google Inc. All Rights Reserved.
//
// Modifications copyright (C) 2019 Radix DLT
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.radixdlt.android.apps.wallet.helper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.radixdlt.android.apps.wallet.ui.activity.WebViewActivity

/**
 * A Fallback that opens a Webview when Custom Tabs is not available
 */
class WebviewFallback : CustomTabsHelper.CustomTabFallback {
    override fun openUri(activity: Activity, uri: Uri) {
        val intent = Intent(activity, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString())
        activity.startActivity(intent)
    }
}
