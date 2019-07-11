package com.radixdlt.android.apps.wallet.identity

import com.google.common.collect.ImmutableSet
import com.radixdlt.client.core.BootstrapConfig
import com.radixdlt.client.core.address.RadixUniverseConfig
import com.radixdlt.client.core.address.RadixUniverseConfigs
import com.radixdlt.client.core.network.RadixNetworkEpic
import com.radixdlt.client.core.network.RadixNode
import java.util.function.Supplier

class BootStrapConfigAndroidImpl(
    location: String,
    private val config: Supplier<RadixUniverseConfig> = Supplier { RadixUniverseConfigs.getLocalnet() },
    vararg nodes: RadixNode
) : BootstrapConfig {

    private val node: RadixNode = RadixNode(location, false, 8080)

    private val discoveryEpics = Supplier<List<RadixNetworkEpic>> { emptyList() }
    private val initialNetwork = ImmutableSet.Builder<RadixNode>().add(node).add(*nodes).build()

    override fun getConfig(): RadixUniverseConfig {
        return config.get()
    }

    override fun getDiscoveryEpics(): List<RadixNetworkEpic> {
        return discoveryEpics.get()
    }

    override fun getInitialNetwork(): Set<RadixNode> {
        return initialNetwork
    }

    companion object {
        fun macAndroidEmulator(): BootStrapConfigAndroidImpl {
            return BootStrapConfigAndroidImpl("10.0.2.2")
        }

        fun address(localHost: String): BootStrapConfigAndroidImpl {
            return BootStrapConfigAndroidImpl(localHost)
        }
    }
}
