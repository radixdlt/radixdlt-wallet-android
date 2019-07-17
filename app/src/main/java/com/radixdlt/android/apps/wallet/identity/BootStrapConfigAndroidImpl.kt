package com.radixdlt.android.apps.wallet.identity

import com.google.common.collect.ImmutableSet
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
        fun localHost(address: String = "localhost"): BootstrapConfig {
            return BootStrapConfigAndroidImpl(address)
        }

        fun macAndroidEmulator(): BootstrapConfig {
            return BootStrapConfigAndroidImpl("10.0.2.2")
        }

        fun radixBetanetNode(): BootstrapConfig {
            return BootStrapConfigAndroidImpl("sunstone-emu.radixdlt.com", true, RadixUniverseConfigs::getBetanet)
        }
    }
}
