package de.datlag.hotdrop.p2p

import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.fragment.ChooseDeviceFragment
import de.datlag.hotdrop.p2p.near.discovery.NearHostDiscovery

class HostDiscovery(private val advancedActivity: AdvancedActivity) : HostCallback {

    private val nearHostDiscovery: NearHostDiscovery
    private var chooseDeviceFragment: ChooseDeviceFragment? = null
    private lateinit var discoveryCallback: DiscoveryCallback

    init {
        nearHostDiscovery = NearHostDiscovery(advancedActivity, this)
    }

    fun start(discoveryCallback: DiscoveryCallback) {
        this.discoveryCallback = discoveryCallback
        nearHostDiscovery.start()
    }

    fun stop() {
        nearHostDiscovery.stop()
    }

    override fun onHostUpdate(hosts: Set<Host>) {
        discoveryCallback.onHostsFound(hosts)
    }
}

interface DiscoveryCallback {
    fun onHostsFound(hosts: Set<Host>)
}

interface HostCallback {
    fun onHostUpdate(hosts: Set<Host>)
}
