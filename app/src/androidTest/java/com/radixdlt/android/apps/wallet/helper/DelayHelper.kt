package com.radixdlt.android.apps.wallet.helper

import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import com.radixdlt.android.apps.wallet.ElapsedTimeIdlingResource
import java.util.concurrent.TimeUnit

object DelayHelper {

    var idlingResource: ElapsedTimeIdlingResource? = null

    fun waitTime(waitingTime: Long) {
        // Make sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(waitingTime * 2, TimeUnit.MILLISECONDS)
        IdlingPolicies.setIdlingResourceTimeout(waitingTime * 2, TimeUnit.MILLISECONDS)

        // Now we wait
        idlingResource =
            ElapsedTimeIdlingResource(waitingTime)
        IdlingRegistry.getInstance().register(idlingResource)
    }
}
