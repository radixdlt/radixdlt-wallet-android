package com.radixdlt.android.apps.wallet.identity

import android.content.Context
import com.google.common.collect.ImmutableSet
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.client.core.BootstrapConfig
import com.radixdlt.client.core.address.RadixUniverseConfig
import com.radixdlt.client.core.address.RadixUniverseConfigs
import com.radixdlt.client.core.network.RadixNetworkEpic
import com.radixdlt.client.core.network.RadixNode

class BootStrapConfigAndroidImpl(
    location: String,
    useSSL: Boolean = false,
    private val config: () -> RadixUniverseConfig = RadixUniverseConfigs::getLocalnet,
    vararg nodes: RadixNode
) : BootstrapConfig {

    private val node: RadixNode = RadixNode(location, useSSL, if (useSSL) 443 else 8080)
    private val discoveryEpics: () -> List<RadixNetworkEpic> = ::emptyList
    private val initialNetwork = ImmutableSet.Builder<RadixNode>().add(node).add(*nodes).build()

    override fun getConfig(): RadixUniverseConfig {
        return config()
    }

    override fun getDiscoveryEpics(): List<RadixNetworkEpic> {
        return discoveryEpics()
    }

    override fun getInitialNetwork(): Set<RadixNode> {
        return initialNetwork
    }

    companion object {
        fun localHost(context: Context, address: String = "localhost"): BootstrapConfig {
            QueryPreferences.setRemoteFaucet(context, false)
            return BootStrapConfigAndroidImpl(address)
        }

        fun macAndroidEmulator(context: Context): BootstrapConfig {
            QueryPreferences.setRemoteFaucet(context, false)
            return BootStrapConfigAndroidImpl("10.0.2.2")
        }

        fun radixBetanetNode(context: Context): BootstrapConfig {
            QueryPreferences.setRemoteFaucet(context, true)
            return BootStrapConfigAndroidImpl("sunstone-emu.radixdlt.com", true, RadixUniverseConfigs::getBetanet)
        }
    }
}
