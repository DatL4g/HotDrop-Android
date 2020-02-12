package de.datlag.hotdrop.p2p.near.discovery

import android.os.Looper
import com.adroitandroid.near.discovery.NearDiscovery
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.p2p.Host
import de.datlag.hotdrop.p2p.HostCallback
import de.datlag.hotdrop.util.DeviceUtil

class NearHostDiscovery(private val advancedActivity: AdvancedActivity, private val callback: HostCallback) {
    val nearDiscovery = NearDiscovery.Builder()
            .setContext(advancedActivity)
            .setDiscoveryTimeoutMillis(Long.MAX_VALUE)
            .setDiscoverableTimeoutMillis(Long.MAX_VALUE)
            .setDiscoverablePingIntervalMillis(PING_INTERVAL)
            .setFilter(Regex(advancedActivity.packageName))
            .setPort(DISCOVERY_PORT)
            .setDiscoveryListener(discoveryListener, Looper.getMainLooper())
            .build()

    fun start() {
        nearDiscovery.makeDiscoverable(DeviceUtil.getDeviceType(advancedActivity).toString() +
                DeviceUtil.getDeviceName(advancedActivity), advancedActivity.packageName)
        nearDiscovery.startDiscovery()
    }

    fun stop() {
        nearDiscovery.makeNonDiscoverable()
        nearDiscovery.stopDiscovery()
    }

    private val discoveryListener: NearDiscovery.Listener
        get() = object : NearDiscovery.Listener {
            override fun onDiscoverableTimeout() {
                stop()
                start()
            }

            override fun onDiscoveryFailure(e: Throwable) {}

            override fun onDiscoveryTimeout() {
                stop()
                start()
            }

            override fun onPeersUpdate(host: Set<com.adroitandroid.near.model.Host>) {
                val hosts: MutableSet<Host> = mutableSetOf()
                host.forEach {
                    hosts.add(Host(advancedActivity, it, nearDiscovery))
                }
                callback.onHostUpdate(hosts.toSet())
            }
        }

    companion object {
        const val PING_INTERVAL: Long = 750
        const val DISCOVERY_PORT: Int = 8241
    }
}