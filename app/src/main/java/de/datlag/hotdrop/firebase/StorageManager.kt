package de.datlag.hotdrop.firebase

import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.firebase.DownloadManager.FileDownloadCallback
import de.datlag.hotdrop.firebase.UploadManager.FileUploadCallback
import de.datlag.hotdrop.manager.SettingsManager
import de.datlag.hotdrop.view.helper.MaterialSnackbar.configSnackbar
import de.interaapps.firebasemanager.core.FirebaseManager

class StorageManager(private val activity: AdvancedActivity, private val firebaseManager: FirebaseManager, private val settingsManager: SettingsManager) {
    private var firebaseUser: FirebaseUser? = null
    private lateinit var uploadManager: UploadManager
    private lateinit var downloadManager: DownloadManager
    init {
        firebaseUser = user
        if(firebaseUser != null) {
            uploadManager = UploadManager(activity, firebaseUser!!)
            downloadManager = DownloadManager(activity, firebaseUser!!)
        }
    }

    private fun checkLoginValid(): Boolean {
        if (firebaseUser == null) {
            activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.account))
                    .setMessage(activity.getString(R.string.upload_info))
                    .setPositiveButton(activity.getString(R.string.okay)) { _: DialogInterface?, _: Int -> settingsManager.chooseSetting(0) }
                    .setNegativeButton(activity.getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
                    .create()).show()
            return false
        }
        return true
    }

    fun upload() {
        if (checkLoginValid()) {
            uploadManager.startUploadFile(firebaseUser!!.isAnonymous, object : FileUploadCallback {
                override fun onSuccess(downloadUri: String?) {
                    if (firebaseUser!!.isAnonymous) {
                        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                                .setTitle("File uploaded")
                                .setMessage("Please share this link, otherwise you cannot Download this file or have any access to it")
                                .setPositiveButton("Share") { _: DialogInterface?, _: Int ->
                                    val sendIntent = Intent(Intent.ACTION_SEND)
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Get this file from HotDrop!")
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, downloadUri)
                                    sendIntent.type = "text/plain"
                                    activity.startActivity(Intent.createChooser(sendIntent, "Share URL"))
                                }.setNeutralButton("Copy Link") {
                                    _: DialogInterface?, _: Int -> activity.copyText("HotDrop Download URL", downloadUri!!)
                                }
                                .create()).show()
                    } else {
                        val snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "File uploaded!", Snackbar.LENGTH_LONG)
                        configSnackbar(activity, snackbar)
                        snackbar.show()
                    }
                }

                override fun onFailure(exception: Exception?) {
                    val snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "Upload failed!", Snackbar.LENGTH_LONG)
                    configSnackbar(activity, snackbar)
                    snackbar.show()
                    Log.e("Upload", exception!!.message)
                }
            })
        }
    }

    fun download(url: String?) {
        if (checkLoginValid()) {
            if (firebaseUser!!.isAnonymous) {
                downloadManager.startDownload(url!!, object : FileDownloadCallback {
                    override fun onNotFound() {
                        Log.e("File", "Not found")
                    }

                    override fun onSuccess() {}
                    override fun onFailed(exception: Exception?) {}
                })
            } else {
                TODO()
            }
        }
    }

    private val user: FirebaseUser?
        get() {
            for (auth in firebaseManager.authManager.login) {
                if (auth.user != null) {
                    return auth.user
                }
            }
            return FirebaseAuth.getInstance().currentUser
        }
}