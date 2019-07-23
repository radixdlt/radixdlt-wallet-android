package com.radixdlt.android.apps.wallet.identity

import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.identity.RadixIdentity
import com.radixdlt.client.core.BootstrapConfig

/**
 * We use a simple object acting as a Singleton in Kotlin to keep a reference to
 * our identity during the lifecycle of the application.
 * */
object Identity {
    private lateinit var bootstrapConfig: BootstrapConfig

    var myIdentity: RadixIdentity? = null

    var api: RadixApplicationAPI? = null
        get() = field?.let {
            return it
        } ?: run {
            field = myIdentity?.let { RadixApplicationAPI.create(bootstrapConfig, it) }
            field?.pull()
            return field
        }
        private set

    fun init(bootstrapConfig: BootstrapConfig) {
        this.bootstrapConfig = bootstrapConfig
    }

    fun clear() {
        myIdentity = null
        api = null
    }
}
