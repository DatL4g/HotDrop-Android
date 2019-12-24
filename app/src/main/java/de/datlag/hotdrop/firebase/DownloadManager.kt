package de.datlag.hotdrop.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.util.FileUtil
import java.text.SimpleDateFormat
import java.util.*

class DownloadManager(private val activity: AdvancedActivity, private val firebaseUser: FirebaseUser) {
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()

    fun startDownload(url: String, fileDownloadCallback: FileDownloadCallback) {
        fileExists(url, object : FileExistsCallback {
            override fun onExists(storageReference: StorageReference?) {
                val docId = url.split("gs:(/)+hotdrop(-420)?.appspot.com/anonym/").toTypedArray()[1]
                firebaseFirestore.collection("uploadData/anonym/anonymDocs").document(docId).get().addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document!!.exists()) {
                            val documentData = document.data
                            val timestamp = documentData!!["UploadDate"] as Timestamp?
                            activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                                    .setTitle(documentData["FileName"].toString())
                                    .setMessage("FileName: " + documentData["FileName"] + "\n" +
                                            "FileSize: " + FileUtil.readableFileSize(documentData["FileSize"] as Long) + "\n" +
                                            "UploadDate: " + getDate(timestamp!!.seconds))
                                    .create()).show()
                        } else {
                            fileDownloadCallback.onNotFound()
                        }
                    } else {
                        Log.e("Error", task.exception!!.message)
                    }
                }
            }

            override fun onNotExists() {
                fileDownloadCallback.onNotFound()
            }
        })
    }

    fun startDownload(fileDownloadCallback: FileDownloadCallback?) {
        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle("Choose File") //.setItems()
                .create()).show()
    }

    private fun getDate(time: Long): String {
        val date = Date(time * 1000L)
        val simpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy", activity.localeList[0])
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(date)
    }

    private fun fileExists(url: String, fileExistsCallback: FileExistsCallback) {
        var storageReference: StorageReference? = null
        try {
            storageReference = firebaseStorage.getReferenceFromUrl(url)
        } catch (ignored: Exception) {
            fileExistsCallback.onNotExists()
        }
        if (storageReference != null) {
            val finalStorageReference: StorageReference = storageReference
            storageReference.downloadUrl.addOnSuccessListener {
                fileExistsCallback.onExists(finalStorageReference)
            }.addOnFailureListener {
                fileExistsCallback.onNotExists()
            }
        }
    }

    internal interface FileExistsCallback {
        fun onExists(storageReference: StorageReference?)
        fun onNotExists()
    }

    interface FileDownloadCallback {
        fun onNotFound()
        fun onSuccess()
        fun onFailed(exception: Exception?)
    }

}