package com.radixdlt.android.identity

import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.identity.RadixIdentity
import com.radixdlt.client.core.Bootstrap

/**
 * We use a simple object acting as a Singleton in Kotlin to keep a reference to
 * our identity during the lifecycle of the application.
 * */
object Identity {
    var myIdentity: RadixIdentity? = null

    var api: RadixApplicationAPI? = null
        get() = field?.let {
            return it
        } ?: run {
            field = myIdentity?.let { RadixApplicationAPI.create(Bootstrap.SUNSTONE, it) }
            field?.pull()
            return field
        }
        private set

    fun clear() {
        myIdentity = null
        api = null
    }
}
