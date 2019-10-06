package de.datlag.hotdrop.utils;

import android.app.Activity;
import android.os.Looper;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.model.Host;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import androidx.collection.ArraySet;


public class HostTransfer {

    private Activity activity;
    private Host host;
    private NearConnect nearConnect;
    private HostTransfer hostTransfer;
    private ReceiveFileUtil receiveFileUtil;

    public HostTransfer(Activity activity) {
        this.activity = activity;
    }

    public HostTransfer(Activity activity, Host host) {
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
        //TODO: progressDialog
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
                    receiveFileUtil.onReceive(host, bytes);
                }
            }

            @Override
            public void onSendComplete(long jobId) {

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
}
