package de.datlag.hotdrop.util

import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.SparseArray
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class ReceiveFileUtil(private val activity: AdvancedActivity) {
    private val markwon: Markwon = Markwon.builder(activity)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(activity))
            .build()

    private val byteArrayList = SparseArray<ByteArray>()
    fun onReceive(hostName: String, bytes: ByteArray) {
        val jsonObject = FileUtil.jsonObjectFromBytes(bytes)
        val name = jsonObject[activity.getString(R.string.name)].asString
        val path = jsonObject[activity.getString(R.string.path)].asString
        val base64Result = jsonObject[activity.getString(R.string.base64)].asString
        val size = FileUtil.readableFileSize(jsonObject[activity.getString(R.string.size)].asLong)
        val part = jsonObject[activity.getString(R.string.part)].asInt
        val maxParts = jsonObject[activity.getString(R.string.maxParts)].asInt
        val extension = jsonObject[activity.getString(R.string.extension)].asString
        val mimeType = jsonObject[activity.getString(R.string.mime)].asString
        byteArrayList.put(part, FileUtil.base64ToBytes(base64Result))
        if (byteArrayList.size() == maxParts) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            for (i in 0 until byteArrayList.size()) {
                try {
                    byteArrayOutputStream.write(Objects.requireNonNull(byteArrayList[i]))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            showDialog(byteArrayOutputStream.toByteArray(), name, path, size, extension, mimeType)
            byteArrayList.clear()
            try {
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showDialog(bytes: ByteArray,
                           name: String,
                           path: String,
                           size: String,
                           extension: String,
                           mimeType: String) {
        val detectedMimeType = FileUtil.getMimeType(bytes)
        val realMimeType = if (detectedMimeType == null) "" else "**Real MimeType:** $detectedMimeType"
        var mimeAndExtSecure = activity.getString(R.string.mime_secure)
        if (mimeType == detectedMimeType && MimeTypes.getMimeType(extension) == mimeType && MimeTypes.getDefaultExt(mimeType) == extension) {
            mimeAndExtSecure = ""
        }
        val markdown = markwon.toMarkdown("**Path:**" + path.replace(name, "") + "<br>" +
                "**Size:** " + size + "<br>" +
                "**Extension:** " + extension + "<br>" +
                "**MimeType:** " + mimeType + "<br>" +
                realMimeType + mimeAndExtSecure)
        val alertDialog = activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle(name)
                .setMessage(markdown)
                .setPositiveButton(activity.getString(R.string.choose_destination)) { _: DialogInterface?, _: Int ->
                    FileUtil.chooseFolder(activity, null, object: FileUtil.FolderChooseCallback{
                        override fun onChosen(path: String?, file: File?) {
                            FileUtil.createFile(path!!, name)
                            FileUtil.writeBytesToFile(bytes, path + File.pathSeparator + name)
                        }

                    })
                }
                .setNegativeButton(activity.getString(R.string.decline), null)
                .create())
        if (MimeTypes.isImage(mimeType) && MimeTypes.isImage(detectedMimeType)) {
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Preview") { _: DialogInterface?, _: Int ->
                val appCompatImageView = AppCompatImageView(activity)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                appCompatImageView.setImageBitmap(bitmap)
                alertDialog.cancel()
                activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                        .setTitle("Preview")
                        .setPositiveButton(activity.getString(R.string.close)) { _: DialogInterface?, _: Int -> alertDialog.show() }
                        .setView(appCompatImageView)
                        .create()).show()
            }
        } else if (MimeTypes.isVideo(mimeType) || MimeTypes.isAudio(mimeType)) {
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Preview") { _: DialogInterface?, _: Int ->
                val playerView = PlayerView(activity)
                val player = ExoPlayerFactory.newSimpleInstance(activity)
                player.prepare(createMediaSourceFromByteArray(bytes))
                playerView.player = player
                player.playWhenReady = true
                alertDialog.cancel()
                activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                        .setTitle("Preview")
                        .setPositiveButton(activity.getString(R.string.close)) { _: DialogInterface?, _: Int ->
                            alertDialog.show()
                            player.release()
                            player.stop(true)
                        }
                        .setView(playerView)
                        .create()).show()
            }
        }
        alertDialog.show()
    }

    private fun createMediaSourceFromByteArray(data: ByteArray): MediaSource {
        val byteArrayDataSource = ByteArrayDataSource(data)
        val factory = DataSource.Factory { byteArrayDataSource }
        val mediaSource: MediaSource = ExtractorMediaSource.Factory(factory)
                .setExtractorsFactory(DefaultExtractorsFactory())
                .createMediaSource(Uri.EMPTY)
        return Objects.requireNonNull(mediaSource, "MediaSource cannot be null")
    }

}