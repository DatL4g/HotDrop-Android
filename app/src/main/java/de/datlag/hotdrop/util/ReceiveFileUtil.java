package de.datlag.hotdrop.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Spanned;
import android.util.SparseArray;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;

import com.adroitandroid.near.model.Host;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.ByteArrayDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;

public class ReceiveFileUtil {

    private AdvancedActivity activity;
    private Markwon markwon;
    private SparseArray<byte[]> byteArrayList = new SparseArray<>();

    public ReceiveFileUtil(AdvancedActivity activity) {
        this.activity = activity;
        markwon = Markwon.builder(activity)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(activity))
                .build();
    }

    public void onReceive(Host host, byte[] bytes) {
        JsonObject jsonObject = FileUtil.jsonObjectFromBytes(bytes);
        String name = jsonObject.get(activity.getString(R.string.name)).getAsString();
        String path = jsonObject.get(activity.getString(R.string.path)).getAsString();
        String base64Result = jsonObject.get(activity.getString(R.string.base64)).getAsString();
        String size = FileUtil.readableFileSize(jsonObject.get(activity.getString(R.string.size)).getAsLong());
        int part = jsonObject.get(activity.getString(R.string.part)).getAsInt();
        int maxParts = jsonObject.get(activity.getString(R.string.maxParts)).getAsInt();
        String extension = jsonObject.get(activity.getString(R.string.extension)).getAsString();
        String mimeType = jsonObject.get(activity.getString(R.string.mime)).getAsString();
        byteArrayList.put(part, FileUtil.base64ToBytes(base64Result));

        if (byteArrayList.size() == maxParts) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int i = 0; i < byteArrayList.size(); i++) {
                try {
                    byteArrayOutputStream.write(Objects.requireNonNull(byteArrayList.get(i)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            showDialog(byteArrayOutputStream.toByteArray(), name, path, size, extension, mimeType);
            byteArrayList.clear();
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void showDialog(byte[] bytes,
                            String name,
                            @NotNull String path,
                            String size,
                            String extension,
                            @NotNull String mimeType) {
        String detectedMimeType = FileUtil.getMimeType(bytes);
        String realMimeType = (detectedMimeType == null) ? "" : "**Real MimeType:** "+detectedMimeType;
        String mimeAndExtSecure = activity.getString(R.string.mime_secure);

        if (mimeType.equals(detectedMimeType) &&
                MimeTypes.getMimeType(extension).equals(mimeType) &&
                MimeTypes.getDefaultExt(mimeType).equals(extension)) {
            mimeAndExtSecure = "";
        }

        final Spanned markdown = markwon.toMarkdown("**Path:**" + path.replace(name, "")+ "<br>" +
                "**Size:** "+ size+ "<br>" +
                "**Extension:** "+ extension+ "<br>" +
                "**MimeType:** "+ mimeType+ "<br>" +
                realMimeType + mimeAndExtSecure);

        AlertDialog alertDialog = activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                .setTitle(name)
                .setMessage(markdown)
                .setPositiveButton(activity.getString(R.string.choose_destination), (dialogInterface, i) -> FileUtil.chooseFolder(activity, (path1, file) -> {
                    FileUtil.createFile(path1, name);
                    FileUtil.writeBytesToFile(bytes, path1 +File.separator+name);
                }))
                .setNegativeButton(activity.getString(R.string.decline), null)
                .create());

        if (MimeTypes.isImage(mimeType) && MimeTypes.isImage(detectedMimeType)) {
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Preview", (dialogInterface, i) -> {
                AppCompatImageView appCompatImageView = new AppCompatImageView(activity);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                appCompatImageView.setImageBitmap(bitmap);
                alertDialog.cancel();
                activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                        .setTitle("Preview")
                        .setPositiveButton(activity.getString(R.string.close), (dialogInterface1, i1) -> alertDialog.show())
                        .setView(appCompatImageView)
                        .create()).show();
            });
        } else if (MimeTypes.isVideo(mimeType) || MimeTypes.isAudio(mimeType)) {
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Preview", (dialogInterface, i) -> {
                PlayerView playerView = new PlayerView(activity);
                SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(activity);
                player.prepare(createMediaSourceFromByteArray(bytes));
                playerView.setPlayer(player);
                player.setPlayWhenReady(true);

                alertDialog.cancel();
                activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                        .setTitle("Preview")
                        .setPositiveButton(activity.getString(R.string.close), (dialogInterface12, i12) -> {
                            alertDialog.show();
                            player.release();
                            player.stop(true);
                        })
                        .setView(playerView)
                        .create()).show();
            });
        }

        alertDialog.show();
    }

    @NotNull
    private MediaSource createMediaSourceFromByteArray(byte[] data) {
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(data);
        DataSource.Factory factory = () -> byteArrayDataSource;
        MediaSource mediaSource = new ExtractorMediaSource.Factory(factory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(Uri.EMPTY);

        return Objects.requireNonNull(mediaSource, "MediaSource cannot be null");
    }
}
