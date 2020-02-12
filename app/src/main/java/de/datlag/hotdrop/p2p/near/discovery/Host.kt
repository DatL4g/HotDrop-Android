package de.datlag.hotdrop.p2p.near.discovery

import android.os.Looper
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.discovery.NearDiscovery
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.p2p.Host
import de.datlag.hotdrop.util.DeviceUtil
import de.datlag.hotdrop.util.ReceiveFileUtil

class Host(override var activity: AdvancedActivity,
           private val nearHost: com.adroitandroid.near.model.Host,
           private val nearDiscovery: NearDiscovery) : Host() {

    val nearConnect = NearConnect.Builder()
            .setContext(activity)
            .setListener(connectListener, Looper.getMainLooper())
            .setPort(CONNECT_PORT)
            .fromDiscovery(nearDiscovery)
            .build()

    val receiveFileUtil = ReceiveFileUtil(activity)

    init {
        this.name = nearHost.name.substring(1)
        DeviceUtil.DeviceType.getType(Character.getNumericValue(nearHost.name[0]))?.let {
            this.type = it
        }
        this.address = nearHost.hostAddress
        nearConnect.startReceiving()
    }

    override fun send(byteArray: ByteArray) {
        nearConnect.send(byteArray, nearHost)
    }

    override fun stop() {
        nearConnect.stopReceiving(true)
    }

    private val connectListener: NearConnect.Listener
        get() = object : NearConnect.Listener {
            override fun onReceive(bytes: ByteArray, sender: com.adroitandroid.near.model.Host) {
                receiveFileUtil.onReceive(sender.name.substring(1), bytes)
            }

            override fun onSendComplete(jobId: Long) {
            }

            override fun onSendFailure(e: Throwable?, jobId: Long) {
            }

            override fun onStartListenFailure(e: Throwable?) {
            }

        }

    companion object {
        const val CONNECT_PORT: Int = 8201
    }
}