package de.datlag.hotdrop.p2p

import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArraySet
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.model.Host
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.datlag.hotdrop.MainActivity
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.util.FileUtil
import de.datlag.hotdrop.util.ReceiveFileUtil
import java.io.File

class HostTransfer(val activity: AdvancedActivity, val host: Host? = null) {
    private var nearConnect: NearConnect
    private var hostTransfer: HostTransfer = this
    private var receiveFileUtil: ReceiveFileUtil
    private lateinit var alertDialog: AlertDialog

    init {
        val peers = ArraySet<Host>()
        peers.add(host)
        nearConnect = NearConnect.Builder()
                .forPeers(peers)
                .setContext(activity)
                .setListener(nearConnectListener, Looper.getMainLooper()).build()
        nearConnect.startReceiving()
        receiveFileUtil = ReceiveFileUtil(activity)
    }

    private fun send(host: Host?, bytes: ByteArray?) {
        nearConnect.send(bytes, host)
    }

    fun startTransfer(file: File?) {
        alertDialog = MaterialAlertDialogBuilder(activity)
                .setView(R.layout.progress_dialog)
                .setCancelable(false)
                .create()
        activity.applyDialogAnimation(alertDialog).show()
        val bytes = FileUtil.byteArraysFromFile(file!!)
        for (i in bytes.indices) {
            hostTransfer.send(host, FileUtil.jsonObjectToBytes(FileUtil.jsonObjectFromFile(activity, file, bytes, i)))
        }
    }

    private val nearConnectListener: NearConnect.Listener
        get() = object : NearConnect.Listener {
            override fun onReceive(bytes: ByteArray, sender: Host) {
                if (String(bytes) == HOST_DISCONNECTED) {
                    if (activity is MainActivity) {
                        activity.switchFragment(activity.searchDeviceFragment, R.id.fragment_view)
                    }
                } else {
                    receiveFileUtil.onReceive(host, bytes)
                }
            }

            override fun onSendComplete(jobId: Long) {
                alertDialog.cancel()
            }

            override fun onSendFailure(e: Throwable, jobId: Long) {}
            override fun onStartListenFailure(e: Throwable) {}
        }

    fun stopTransferAndDisconnect() {
        nearConnect.stopReceiving(true)
        send(host, HOST_DISCONNECTED.toByteArray())
    }

    companion object {
        const val HOST_DISCONNECTED = "HOST_DISCONNECTED"
    }
}