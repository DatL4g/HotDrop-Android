package de.datlag.hotdrop.p2p

import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.util.DeviceUtil
import java.net.InetAddress

abstract class Host {
    var name = "DUMMY"
        protected set
    var type = DeviceUtil.DeviceType.PHONE
    var address: String = InetAddress.getLoopbackAddress().hostAddress
    open lateinit var activity: AdvancedActivity

    abstract fun send(byteArray: ByteArray)
    abstract fun stop()
}
