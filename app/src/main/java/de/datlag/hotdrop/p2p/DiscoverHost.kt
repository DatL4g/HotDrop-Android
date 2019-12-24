package de.datlag.hotdrop.p2p

import android.content.DialogInterface
import android.os.Looper
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.discovery.NearDiscovery
import com.adroitandroid.near.model.Host
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.datlag.hotdrop.MainActivity
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.fragment.ChooseDeviceFragment
import de.datlag.hotdrop.fragment.ChooseDeviceFragment.Companion.newInstance
import de.datlag.hotdrop.fragment.TransferFragment
import de.datlag.hotdrop.fragment.TransferFragment.Companion.newInstance
import de.datlag.hotdrop.util.DeviceUtil
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import org.jetbrains.annotations.Contract
import java.net.InetAddress

class DiscoverHost(private val activity: AdvancedActivity) {
    private var nearDiscovery: NearDiscovery
    private var nearConnect: NearConnect
    private var chooseDeviceFragment: ChooseDeviceFragment? = null
    private lateinit var transferFragment: TransferFragment
    private var markwon: Markwon
    private lateinit var inetAddress: InetAddress

    init {
        nearDiscovery = NearDiscovery.Builder()
                .setContext(activity)
                .setDiscoverableTimeoutMillis(Long.MAX_VALUE)
                .setDiscoveryTimeoutMillis(Long.MAX_VALUE)
                .setDiscoverablePingIntervalMillis(750)
                .setDiscoveryListener(nearDiscoveryListener, Looper.getMainLooper())
                .build()
        nearConnect = NearConnect.Builder()
                .fromDiscovery(nearDiscovery)
                .setContext(activity)
                .setListener(nearConnectListener, Looper.getMainLooper())
                .build()
        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .usePlugin(TablePlugin.create(activity))
                .build()
    }

    fun startDiscovery() {
        if (!nearDiscovery.isDiscovering) {
            nearDiscovery.makeDiscoverable(DeviceUtil.getDeviceType(activity).toString() + activity.packageName + DeviceUtil.getDeviceName(activity))
            if (!nearConnect.isReceiving) {
                nearConnect.startReceiving()
            }
            nearDiscovery.startDiscovery()
        }
    }

    fun stopDiscovery() {
        if (nearDiscovery.isDiscovering) {
            nearDiscovery.makeNonDiscoverable()
            nearDiscovery.stopDiscovery()
        }
        if (nearConnect.isReceiving) {
            nearConnect.stopReceiving(false)
            if (activity is MainActivity) {
                activity.searchDeviceFragment.setSearch(false)
            }
        }
    }

    fun send(host: Host?, bytes: ByteArray?) {
        nearConnect.send(bytes, host)
    }


    private val nearDiscoveryListener: NearDiscovery.Listener
        get() = object : NearDiscovery.Listener {
            override fun onPeersUpdate(hosts: MutableSet<Host>) {
                inetAddress = InetAddress.getLoopbackAddress()
                for (host in hosts) {
                    if (!host.name.contains(activity.packageName)) {
                        hosts.remove(host)
                    }
                    if (host.hostAddress == inetAddress.hostAddress) {
                        hosts.remove(host)
                    }
                }
                if (hosts.size > 0) {
                    if (chooseDeviceFragment == null) {
                        chooseDeviceFragment = newInstance(hosts)
                    } else {
                        chooseDeviceFragment!!.setHosts(hosts)
                    }
                    activity.switchFragment(chooseDeviceFragment!!, R.id.fragment_view)
                } else {
                    (activity as? MainActivity)?.switchFragment(activity.searchDeviceFragment, R.id.fragment_view)
                    if (nearDiscovery.isDiscovering) {
                        if (activity is MainActivity) {
                            activity.searchDeviceFragment.setSearch(true)
                        }
                    } else {
                        if (activity is MainActivity) {
                            activity.searchDeviceFragment.setSearch(false)
                        }
                    }
                }
            }

            override fun onDiscoveryTimeout() {
                stopDiscovery()
                if (activity is MainActivity) {
                    activity.searchDeviceFragment.setSearch(false)
                }
            }

            override fun onDiscoveryFailure(e: Throwable) {
                stopDiscovery()
                if (activity is MainActivity) {
                    activity.searchDeviceFragment.setSearch(false)
                }
            }

            override fun onDiscoverableTimeout() {
                stopDiscovery()
                if (activity is MainActivity) {
                    activity.searchDeviceFragment.setSearch(false)
                }
            }
        }

    private val nearConnectListener: NearConnect.Listener
        get() = object : NearConnect.Listener {
            override fun onReceive(bytes: ByteArray, sender: Host) {
                when (String(bytes)) {
                    MESSAGE_REQUEST_START_TRANSFER, MESSAGE_RESPONSE_ACCEPT_REQUEST, MESSAGE_RESPONSE_DECLINE_REQUEST -> startHostTransfer(sender, bytes)
                    else -> nearConnect.send(MESSAGE_RESPONSE_DECLINE_REQUEST.toByteArray(), sender)
                }
            }

            override fun onSendComplete(jobId: Long) {}
            override fun onSendFailure(e: Throwable, jobId: Long) {}
            override fun onStartListenFailure(e: Throwable) {}
        }

    private fun startHostTransfer(sender: Host, bytes: ByteArray) {
        val senderName = sender.name.substring(sender.name.indexOf(activity.packageName) + activity.packageName.length)
        when (String(bytes)) {
            MESSAGE_REQUEST_START_TRANSFER -> activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                    .setMessage(senderName + activity.getString(R.string.want2connect))
                    .setPositiveButton(activity.getString(R.string.start)) { _: DialogInterface?, _: Int ->
                        nearConnect.send(MESSAGE_RESPONSE_ACCEPT_REQUEST.toByteArray(), sender)
                        stopDiscoveryAndStartTransfer(sender)
                    }
                    .setNegativeButton(activity.getString(R.string.cancel)) { _: DialogInterface?, _: Int -> nearConnect.send(MESSAGE_RESPONSE_DECLINE_REQUEST.toByteArray(), sender) }.create()).show()
            MESSAGE_RESPONSE_DECLINE_REQUEST -> activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                    .setMessage(senderName + activity.getString(R.string.is_busy))
                    .setNeutralButton(activity.getString(R.string.okay), null).create()).show()
            MESSAGE_RESPONSE_ACCEPT_REQUEST -> stopDiscoveryAndStartTransfer(sender)
            else -> nearConnect.send(MESSAGE_RESPONSE_DECLINE_REQUEST.toByteArray(), sender)
        }
    }

    private fun stopDiscoveryAndStartTransfer(host: Host) {
        nearConnect.stopReceiving(false)
        nearDiscovery.stopDiscovery()
        transferFragment = newInstance(activity, host)
        activity.switchFragment(transferFragment, R.id.fragment_view)
    }

    companion object {
        const val MESSAGE_REQUEST_START_TRANSFER = "START_TRANSFER"
        const val MESSAGE_RESPONSE_DECLINE_REQUEST = "DECLINE_REQUEST"
        const val MESSAGE_RESPONSE_ACCEPT_REQUEST = "ACCEPT_REQUEST"
    }
}