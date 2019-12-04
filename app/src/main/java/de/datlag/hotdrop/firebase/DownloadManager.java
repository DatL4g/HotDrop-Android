package de.datlag.hotdrop.firebase;

import android.util.Log;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.util.FileUtil;

public class DownloadManager {

    private AdvancedActivity activity;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public DownloadManager(AdvancedActivity activity, FirebaseUser firebaseUser) {
        this.activity = activity;
        this.firebaseUser = firebaseUser;
    }

    public void startDownload(String url, FileDownloadCallback fileDownloadCallback) {
        fileExists(url, new FileExistsCallback() {
            @Override
            public void onExists(StorageReference storageReference) {
                String docId = url.split("gs:(/)+hotdrop(-420)?.appspot.com/anonym/")[1];
                firebaseFirestore.collection("uploadData/anonym/anonymDocs").document(docId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> documentData = document.getData();
                            Timestamp timestamp = (Timestamp) documentData.get("UploadDate");
                            activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                                    .setTitle(documentData.get("FileName").toString())
                                    .setMessage("FileName: " + documentData.get("FileName") + "\n" +
                                            "FileSize: " + FileUtil.readableFileSize((long) documentData.get("FileSize")) + "\n" +
                                            "UploadDate: " + getDate(timestamp.getSeconds()))
                                    .create()).show();
                        } else {
                            fileDownloadCallback.onNotFound();
                        }
                    } else {
                        Log.e("Error", task.getException().getMessage());
                    }
                });
            }

            @Override
            public void onNotExists() {
                fileDownloadCallback.onNotFound();
            }
        });
    }

    public void startDownload(FileDownloadCallback fileDownloadCallback) {
        activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                .setTitle("Choose File")
                //.setItems()
                .create()).show();
    }

    private String getDate(long time) {
        Date date = new Date(time*1000L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return simpleDateFormat.format(date);
    }

    private void fileExists(String url, FileExistsCallback fileExistsCallback) {
        StorageReference storageReference = null;
        try {
            storageReference = firebaseStorage.getReferenceFromUrl(url);
        } catch (Exception ignored) {
            fileExistsCallback.onNotExists();
        }

        if (storageReference != null) {
            StorageReference finalStorageReference = storageReference;
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                fileExistsCallback.onExists(finalStorageReference);
            }).addOnFailureListener(e -> {
                fileExistsCallback.onNotExists();
            });
        }
    }

    interface FileExistsCallback {
        void onExists(StorageReference storageReference);
        void onNotExists();
    }

    public interface FileDownloadCallback {
        void onNotFound();
        void onSuccess();
        void onFailed(Exception exception);
    }
}
