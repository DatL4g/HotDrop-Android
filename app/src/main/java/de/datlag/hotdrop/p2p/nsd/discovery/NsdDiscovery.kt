package de.datlag.hotdrop.p2p.nsd.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.p2p.DiscoveryService
import de.datlag.hotdrop.p2p.HostCallback
import java.net.ServerSocket

class NsdDiscovery(private val advancedActivity: AdvancedActivity,
                   private val callback: HostCallback) : DiscoveryService(advancedActivity, callback) {

    private var nsdManager: NsdManager? = null
    private var nsdServiceInfo: NsdServiceInfo? = null

    private var registered: Boolean = false
    private var discoveryStarted: Boolean = false

    private fun registerService() {
        val port = getAvailablePort()
        if (port == 0) return

        nsdServiceInfo = NsdServiceInfo().apply {
            serviceName = NSD_SERVICENAME
            serviceType = NSD_SERVICETYPE
            setPort(port)
        }

        nsdManager?.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener)
    }

    private fun getAvailablePort(): Int {
        return try {
            val socket = ServerSocket(0)
            socket.localPort
        } catch (ignored: Exception) {
            0
        }
    }

    private fun unregisterService() {
        if (registered) {
            registered = false
            nsdManager?.unregisterService(nsdRegistrationListener)
        }
    }

    private var nsdRegistrationListener = object: NsdManager.RegistrationListener {
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {

        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
        }

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
            registered = true
        }

    }

    override fun start() {
        if (!discoveryStarted) {
            discoveryStarted = true

        }
    }

    override fun stop() {

    }

    companion object {
        const val NSD_SERVICENAME = "_nsdhotdrop"
        const val NSD_TRANSPORTLAYER = "_tcp"
        const val NSD_SERVICETYPE = "$NSD_SERVICENAME.$NSD_TRANSPORTLAYER"
    }

}