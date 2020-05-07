package de.datlag.hotdrop.p2p

import de.datlag.hotdrop.extend.AdvancedActivity

abstract class DiscoveryService(advancedActivity: AdvancedActivity,
                                discoveryCallback: HostCallback) {

    abstract fun start()
    abstract fun stop()

}