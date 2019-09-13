package de.datlag.hotdrop.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Looper;
import android.text.Spanned;
import android.util.Log;

import androidx.collection.ArraySet;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.model.Host;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.datlag.hotdrop.R;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class HostTransfer {

    private Activity activity;
    private Host host;
    private NearConnect nearConnect;
    private HostTransfer hostTransfer;
    private Markwon markwon;

    public HostTransfer(Activity activity) {
        this.activity = activity;
    }

    public HostTransfer(Activity activity, Host host) {
        this.activity = activity;
        this.host = host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public void init() {
        markwon = Markwon.builder(activity)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(activity))
                .build();
        hostTransfer = this;
        ArraySet<Host> peers = new ArraySet<>();
        peers.add(host);
        nearConnect = new NearConnect.Builder()
                .forPeers(peers)
                .setContext(activity)
                .setListener(getNearConnectListener(), Looper.getMainLooper()).build();
        nearConnect.startReceiving();
    }

    public void saveFile(byte[] bytes) {
        JsonObject jsonObject = FileUtil.jsonObjectFromBytes(bytes);
        String name = jsonObject.get("name").getAsString();
        String path = jsonObject.get("path").getAsString();
        String base64Result = jsonObject.get("base64").getAsString();
        String extension = jsonObject.get("extension").getAsString();
        String mimeType = jsonObject.get("mime").getAsString();
        String detectedMimeType = FileUtil.getMimeType(FileUtil.base64ToBytes(base64Result));
        String realMimeType = (detectedMimeType == null) ? "" : "**Real MimeType:** "+detectedMimeType;
        String mimeAndExtSecure = activity.getString(R.string.mime_secure);
        if (mimeType.equals(detectedMimeType) &&
                MimeTypes.getMimeType(extension).equals(mimeType) &&
                MimeTypes.getDefaultExt(mimeType).equals(extension)) {
            mimeAndExtSecure = "";
        }

        //final Spanned markdown = markwon.toMarkdown("**Hello there!**<br><a href=\"google.com\">Google</a>");
        final Spanned markdown = markwon.toMarkdown("**Path:**" + path.replace(name, "")+ "<br>" +
                "**Extension:** "+ extension+ "<br>" +
                "**MimeType:** "+ mimeType+ "<br>" +
                realMimeType + mimeAndExtSecure);

        new MaterialAlertDialogBuilder(activity)
                .setTitle(name)
                .setMessage(markdown)
                .setPositiveButton(activity.getString(R.string.choose_destination), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FileUtil.chooseFolder(activity, new FileUtil.FolderChooseCallback() {
                            @Override
                            public void onChosen(String path, File file) {
                                Log.e("Path", path);
                                Log.e("File", file.getName());
                                FileUtil.createFile(path, name);
                                FileUtil.writeBytesToFile(FileUtil.base64ToBytes(base64Result), path+File.separator+name);
                            }
                        });
                    }
                })
                .setNegativeButton(activity.getString(R.string.decline), null)
                .create().show();
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private NearConnect.Listener getNearConnectListener() {
        return new NearConnect.Listener() {
            @Override
            public void onReceive(byte[] bytes, final Host sender) {
                if (bytes != null) {
                    saveFile(bytes);
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
}
