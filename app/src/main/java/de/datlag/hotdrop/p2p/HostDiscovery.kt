package de.datlag.hotdrop.p2p

import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.p2p.near.discovery.NearHostDiscovery

class HostDiscovery(private val advancedActivity: AdvancedActivity) : HostCallback {

    private val nearHostDiscovery = NearHostDiscovery(advancedActivity, this)
    private lateinit var discoveryCallback: DiscoveryCallback

    fun start(discoveryCallback: DiscoveryCallback) {
        this.discoveryCallback = discoveryCallback
        nearHostDiscovery.start()
    }

    fun stop() {
        nearHostDiscovery.stop()
    }

    override fun onHostUpdate(hosts: Set<Host>, service: Int) {
        discoveryCallback.onHostsFound(hosts)
    }

    companion object {
        const val NSD_SERVICE = 0
        const val NEAR_SERVICE = 1
        const val WEB_SERVICE = 2
    }
}

interface DiscoveryCallback {
    fun onHostsFound(hosts: Set<Host>)
}

interface HostCallback {
    fun onHostUpdate(hosts: Set<Host>, service: Int)
}
