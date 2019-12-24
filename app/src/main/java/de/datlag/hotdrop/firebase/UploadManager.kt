package de.datlag.hotdrop.firebase

import android.app.Activity
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.datlag.hotdrop.util.DateChecker
import de.datlag.hotdrop.util.DateChecker.DateCheckerCallback
import de.datlag.hotdrop.util.FileUtil
import de.datlag.hotdrop.util.FileUtil.FileChooseCallback
import java.io.File
import java.io.FileFilter
import java.util.*

class UploadManager(private val activity: Activity, private val firebaseUser: FirebaseUser) {
    private var dateChecker: DateChecker = DateChecker(activity)
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val storageReference = firebaseStorage.reference

    fun startUploadFile(isAnonym: Boolean, fileUploadCallback: FileUploadCallback) {
        val fileFilter = FileFilter { file: File -> file.length() < 50000000 }
        FileUtil.chooseFile(activity, fileFilter, null, object : FileChooseCallback {
            override fun onChosen(path: String?, file: File?) {
                startByDBEntry(file!!, isAnonym, fileUploadCallback)
            }
        })
    }

    private fun startByDBEntry(file: File, isAnonym: Boolean, fileUploadCallback: FileUploadCallback) {
        dateChecker.getDate(object : DateCheckerCallback {
            override fun onSuccess(timestamp: Timestamp?) {
                createDBEntry(file, timestamp!!, true, isAnonym, fileUploadCallback)
            }

            override fun onFailure(timestamp: Timestamp?) {
                createDBEntry(file, timestamp!!, false, isAnonym, fileUploadCallback)
            }
        })
    }

    private fun createDBEntry(file: File, timestamp: Timestamp, valid: Boolean, isAnonym: Boolean, fileUploadCallback: FileUploadCallback) {
        val upload: MutableMap<String, Any> = HashMap()
        upload["FileName"] = file.name
        upload["FileExtension"] = FileUtil.getFileExtension(file)
        upload["FileSize"] = file.length()
        upload["UploadDate"] = timestamp
        upload["DateValid"] = valid
        upload["UploadStatus"] = false
        upload["FileUrl"] = ""
        if (isAnonym) {
            firebaseFirestore.collection("uploadData/anonym/anonymDocs")
                    .add(upload)
                    .addOnSuccessListener(activity) { documentReference: DocumentReference -> uploadFile(file, documentReference.id, true, fileUploadCallback) }
                    .addOnFailureListener(activity) { exception: Exception? -> fileUploadCallback.onFailure(exception) }
        } else {
            firebaseFirestore.collection("uploadData/normal/" + firebaseUser.uid)
                    .add(upload)
                    .addOnSuccessListener(activity) { documentReference: DocumentReference -> uploadFile(file, documentReference.id, false, fileUploadCallback) }
                    .addOnFailureListener(activity) { exception: Exception? -> fileUploadCallback.onFailure(exception) }
        }
    }

    private fun uploadFile(file: File, docID: String, isAnonym: Boolean, fileUploadCallback: FileUploadCallback) {
        val uploadRef: StorageReference = if (isAnonym) {
            storageReference.child("anonym/$docID")
        } else {
            storageReference.child("normal/" + firebaseUser.uid + "/" + docID)
        }
        val uploadTask = uploadRef.putFile(Uri.fromFile(file))
        uploadTask.addOnSuccessListener(activity) { updateDBEntry(uploadRef, docID, isAnonym, fileUploadCallback) }
                .addOnFailureListener(activity) { exception: Exception? -> fileUploadCallback.onFailure(exception) }
    }

    private fun updateDBEntry(storageReference: StorageReference, docId: String, isAnonym: Boolean, fileUploadCallback: FileUploadCallback) {
        val updateData: MutableMap<String, Any> = HashMap()
        updateData["UploadStatus"] = true
        updateData["FileUrl"] = storageReference.toString()
        if (isAnonym) {
            firebaseFirestore.collection("uploadData/anonym/anonymDocs")
                    .document(docId).update(updateData).addOnSuccessListener(activity) {
                        fileUploadCallback.onSuccess(storageReference.toString())
                    }.addOnFailureListener(activity) {
                        exception: Exception? -> fileUploadCallback.onFailure(exception)
                    }
        } else {
            firebaseFirestore.collection("uploadData/normal/" + firebaseUser.uid)
                    .document(docId).update(updateData).addOnSuccessListener(activity) { fileUploadCallback.onSuccess(storageReference.toString()) }
                    .addOnFailureListener(activity) { exception: Exception? -> fileUploadCallback.onFailure(exception) }
        }
    }

    interface FileUploadCallback {
        fun onSuccess(url: String?)
        fun onFailure(exception: Exception?)
    }
}