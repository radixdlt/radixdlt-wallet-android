package com.radixdlt.android.apps.wallet.identity

import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.identity.RadixIdentity

/**
 * We use a simple object acting as a Singleton in Kotlin to keep a reference to
 * our identity during the lifecycle of the application.
 * */
object Identity {
    // Choose where to bootstrap
    private val bootstrap = BootStrapConfigAndroidImpl.macAndroidEmulator()

    var myIdentity: RadixIdentity? = null

    var api: RadixApplicationAPI? = null
        get() = field?.let {
            return it
        } ?: run {
            field = myIdentity?.let { RadixApplicationAPI.create(bootstrap, it) }
            field?.pull()
            return field
        }
        private set

    fun clear() {
        myIdentity = null
        api = null
    }
}
