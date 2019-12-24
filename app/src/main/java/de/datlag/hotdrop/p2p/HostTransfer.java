package de.datlag.hotdrop.p2p;

import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.collection.ArraySet;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.model.Host;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import de.datlag.hotdrop.MainActivity;
import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.util.FileUtil;
import de.datlag.hotdrop.util.ReceiveFileUtil;


public class HostTransfer {

    private AdvancedActivity activity;
    private Host host;
    private NearConnect nearConnect;
    private HostTransfer hostTransfer;
    private ReceiveFileUtil receiveFileUtil;
    public static final String HOST_DISCONNECTED = "HOST_DISCONNECTED";
    private AlertDialog alertDialog;

    public HostTransfer(AdvancedActivity activity) {
        this.activity = activity;
    }

    public HostTransfer(AdvancedActivity activity, Host host) {
        this.activity = activity;
        this.host = host;

        init();
    }

    public void setHost(Host host) {
        this.host = host;
        init();
    }

    private void init() {
        hostTransfer = this;
        ArraySet<Host> peers = new ArraySet<>();
        peers.add(host);
        nearConnect = new NearConnect.Builder()
                .forPeers(peers)
                .setContext(activity)
                .setListener(getNearConnectListener(), Looper.getMainLooper()).build();
        nearConnect.startReceiving();
        receiveFileUtil = new ReceiveFileUtil(activity);
    }

    public void send(Host host, byte[] bytes) {
        nearConnect.send(bytes, host);
    }

    public void startTransfer(File file) {
        alertDialog = new MaterialAlertDialogBuilder(activity)
                .setView(R.layout.progress_dialog)
                .setCancelable(false)
                .create();
        activity.applyDialogAnimation(alertDialog).show();
        ArrayList<byte[]> bytes = FileUtil.byteArraysFromFile(file);
        for (int i = 0; i < bytes.size(); i++) {
            hostTransfer.send(host, FileUtil.jsonObjectToBytes(FileUtil.jsonObjectFromFile(activity, file, bytes, i)));
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private NearConnect.Listener getNearConnectListener() {
        return new NearConnect.Listener() {
            @Override
            public void onReceive(byte[] bytes, final Host sender) {
                if (bytes != null) {
                    if (new String(bytes).equals(HOST_DISCONNECTED)) {
                        if (activity instanceof MainActivity) {
                            activity.switchFragment(((MainActivity) activity).getSearchDeviceFragment(), R.id.fragment_view);
                        }
                    } else {
                        receiveFileUtil.onReceive(host, bytes);
                    }
                }
            }
            @Override
            public void onSendComplete(long jobId) {
                alertDialog.cancel();
            }
            @Override
            public void onSendFailure(Throwable e, long jobId) {

            }

            @Override
            public void onStartListenFailure(Throwable e) {
                // This tells that the NearConnect.startReceiving() didn't go through properly.
                // Common cause would be that another instance of NearConnect is already listening and it's NearConnect.stopReceiving() needs to be called first
            }
        };
    }

    public void stopTransferAndDisconnect() {
        nearConnect.stopReceiving(true);
        send(host, HOST_DISCONNECTED.getBytes());
    }
}
