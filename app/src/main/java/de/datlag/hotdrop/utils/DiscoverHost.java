package de.datlag.hotdrop.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Looper;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.discovery.NearDiscovery;
import com.adroitandroid.near.model.Host;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import androidx.annotation.NonNull;
import de.datlag.hotdrop.ChooseDeviceFragment;
import de.datlag.hotdrop.MainActivity;
import de.datlag.hotdrop.R;
import de.datlag.hotdrop.TransferFragment;
import github.nisrulz.easydeviceinfo.base.EasyDeviceMod;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class DiscoverHost {

    private Activity activity;
    private NearDiscovery nearDiscovery;
    private NearConnect nearConnect;
    private ChooseDeviceFragment chooseDeviceFragment = null;
    private TransferFragment transferFragment;
    private DiscoverHost discoverHost;
    private Markwon markwon;

    public static final String MESSAGE_REQUEST_START_TRANSFER = "start_chat";
    public static final String MESSAGE_RESPONSE_DECLINE_REQUEST = "decline_request";
    public static final String MESSAGE_RESPONSE_ACCEPT_REQUEST = "accept_request";

    public DiscoverHost(Activity activity) {
        this.activity = activity;
        discoverHost = this;
        init();
    }

    private void init() {
        nearDiscovery = new NearDiscovery.Builder()
                .setContext(activity)
                .setDiscoverableTimeoutMillis(Long.MAX_VALUE)
                .setDiscoveryTimeoutMillis(Long.MAX_VALUE)
                .setDiscoverablePingIntervalMillis(500)
                .setDiscoveryListener(getNearDiscoveryListener(), Looper.getMainLooper())
                .build();

        nearConnect = new NearConnect.Builder()
                .fromDiscovery(nearDiscovery)
                .setContext(activity)
                .setListener(getNearConnectListener(), Looper.getMainLooper())
                .build();

        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .usePlugin(TablePlugin.create(activity))
                .build();
    }

    public void startDiscovery() {
        EasyDeviceMod easyDeviceMod = new EasyDeviceMod(activity);
        if (!nearDiscovery.isDiscovering()) {
            nearDiscovery.makeDiscoverable(easyDeviceMod.getDeviceType(activity) + activity.getPackageName() + "_" + Build.MODEL);
            if (!nearConnect.isReceiving()) {
                nearConnect.startReceiving();
            }

            nearDiscovery.startDiscovery();
        }
    }

    public void stopDiscovery() {
        if (nearDiscovery.isDiscovering()) {
            nearDiscovery.makeNonDiscoverable();
            nearDiscovery.stopDiscovery();
        }
        if (nearConnect.isReceiving()) {
            nearConnect.stopReceiving(false);
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).setSearching(false);
            }
        }
    }

    public void send(Host host, byte[]  bytes) {
        nearConnect.send(bytes, host);
    }

    @Contract(value = " -> new", pure = true)
    @NonNull
    private NearDiscovery.Listener getNearDiscoveryListener() {
        return new NearDiscovery.Listener() {
            @Override
            public void onPeersUpdate(Set<Host> hosts) {
                for (Host host : hosts) {
                    if (!host.getName().contains(activity.getPackageName())) {
                        hosts.remove(host);
                    }
                }

                if (hosts.size() > 0) {
                    if (chooseDeviceFragment == null) {
                        chooseDeviceFragment = new ChooseDeviceFragment(hosts);
                    } else {
                        chooseDeviceFragment.setHosts(hosts);
                    }
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).switch2Fragment(chooseDeviceFragment);
                    }
                } else {
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).switch2Fragment(((MainActivity) activity).searchDeviceFragment);
                    }
                    if (nearDiscovery.isDiscovering()) {
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).setSearching(true);
                        }
                    } else {
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).setSearching(false);
                        }
                    }
                }
            }

            @Override
            public void onDiscoveryTimeout() {
                stopDiscovery();
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).setSearching(false);
                }
            }

            @Override
            public void onDiscoveryFailure(Throwable e) {
                stopDiscovery();
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).setSearching(false);
                }
            }

            @Override
            public void onDiscoverableTimeout() {
                stopDiscovery();
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).setSearching(false);
                }
            }
        };
    }

    @Contract(value = " -> new", pure = true)
    @NonNull
    private NearConnect.Listener getNearConnectListener() {
        return new NearConnect.Listener() {
            @Override
            public void onReceive(byte[] bytes, final Host sender) {
                if (bytes != null) {
                    switch (new String(bytes)) {
                        case MESSAGE_REQUEST_START_TRANSFER:
                        case MESSAGE_RESPONSE_ACCEPT_REQUEST:
                        case MESSAGE_RESPONSE_DECLINE_REQUEST:
                            startHostTransfer(sender, bytes);
                            break;

                        default:
                            stopDiscoveryAndStartTransfer(sender);
                            break;
                    }
                }
            }

            @Override
            public void onSendComplete(long jobId) {
                // jobId is the same as the return value of NearConnect.send(), an approximate epoch time of the send
            }

            @Override
            public void onSendFailure(Throwable e, long jobId) {
                // handle failed sends here
            }

            @Override
            public void onStartListenFailure(Throwable e) {
                // This tells that the NearConnect.startReceiving() didn't go through properly.
                // Common cause would be that another instance of NearConnect is already listening and it's NearConnect.stopReceiving() needs to be called first
            }
        };
    }

    private void startHostTransfer(@NotNull final Host sender, byte[] bytes) {
        String senderName = sender.getName().substring(sender.getName().indexOf(activity.getPackageName()) + activity.getPackageName().length() +1);

        switch (new String(bytes)) {
            case MESSAGE_REQUEST_START_TRANSFER:
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(senderName + activity.getString(R.string.want2connect))
                        .setPositiveButton(activity.getString(R.string.start), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nearConnect.send(MESSAGE_RESPONSE_ACCEPT_REQUEST.getBytes(), sender);
                                stopDiscoveryAndStartTransfer(sender);
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nearConnect.send(MESSAGE_RESPONSE_DECLINE_REQUEST.getBytes(), sender);
                            }
                        }).create().show();
                break;
            case MESSAGE_RESPONSE_DECLINE_REQUEST:
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(senderName + activity.getString(R.string.is_busy))
                        .setNeutralButton(activity.getString(R.string.okay), null).create().show();
                break;
            case MESSAGE_RESPONSE_ACCEPT_REQUEST:
                stopDiscoveryAndStartTransfer(sender);
                break;
        }
    }

    private void stopDiscoveryAndStartTransfer(Host host) {
        nearConnect.stopReceiving(false);
        nearDiscovery.stopDiscovery();
        transferFragment = new TransferFragment(activity, host);
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).switch2Fragment(transferFragment);
        }
    }
}
