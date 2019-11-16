package de.datlag.hotdrop.firebase;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.datlag.hotdrop.util.DateChecker;
import de.datlag.hotdrop.util.FileUtil;

public class UploadManager {

    private Activity activity;
    private FirebaseUser firebaseUser;
    private DateChecker dateChecker;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    public UploadManager(Activity activity, FirebaseUser firebaseUser) {
        this.activity = activity;
        this.firebaseUser = firebaseUser;

        init();
    }

    private void init() {
        dateChecker = new DateChecker(activity);
    }

    public void startUploadFile(boolean isAnonym, FileUploadCallback fileUploadCallback) {
        FileFilter fileFilter = file -> file.length() < 50000000;

        FileUtil.chooseFile(activity, fileFilter, (path, file) -> startByDBEntry(file, isAnonym, fileUploadCallback));
    }

    private void startByDBEntry(File file, boolean isAnonym, FileUploadCallback fileUploadCallback) {
        dateChecker.getDate(new DateChecker.DateCheckerCallback() {
            @Override
            public void onSuccess(Timestamp timestamp) {
                createDBEntry(file, timestamp, true, isAnonym, fileUploadCallback);
            }

            @Override
            public void onFailure(Timestamp timestamp) {
                createDBEntry(file, timestamp, false, isAnonym, fileUploadCallback);
            }
        });
    }

    private void createDBEntry(@NotNull File file, Timestamp timestamp, boolean valid, boolean isAnonym, FileUploadCallback fileUploadCallback) {
        Map<String, Object> upload = new HashMap<>();
        upload.put("FileName", file.getName());
        upload.put("FileExtension", FileUtil.getFileExtension(file));
        upload.put("FileSize", file.length());
        upload.put("UploadDate", timestamp);
        upload.put("DateValid", valid);
        upload.put("UploadStatus", false);
        upload.put("FileUrl", "");

        if (isAnonym) {
            firebaseFirestore.collection("uploadData/anonym/anonymDocs")
                    .add(upload)
                    .addOnSuccessListener(activity, documentReference -> uploadFile(file, documentReference.getId(), true, fileUploadCallback))
                    .addOnFailureListener(activity, fileUploadCallback::onFailure);
        } else {
            firebaseFirestore.collection("uploadData/normal/" + firebaseUser.getUid())
                    .add(upload)
                    .addOnSuccessListener(activity, documentReference -> uploadFile(file, documentReference.getId(), false, fileUploadCallback))
                    .addOnFailureListener(activity, fileUploadCallback::onFailure);
        }
    }

    private void uploadFile(@NotNull File file, String docID, boolean isAnonym, FileUploadCallback fileUploadCallback) {
        StorageReference uploadRef;
        String randomId = docID+randomString(64)+file.getName();
        if (isAnonym) {
            uploadRef = storageReference.child("anonym/" + randomId);
        } else {
            uploadRef = storageReference.child("normal/" + firebaseUser.getUid() + "/" + randomId);
        }

        UploadTask uploadTask = uploadRef.putFile(Uri.fromFile(file));
        uploadTask.addOnSuccessListener(activity, taskSnapshot -> updateDBEntry(uploadRef, docID, isAnonym, fileUploadCallback))
                .addOnFailureListener(activity, fileUploadCallback::onFailure);
    }

    private void updateDBEntry(@NotNull StorageReference storageReference, String docId, boolean isAnonym, FileUploadCallback fileUploadCallback) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("UploadStatus", true);
        updateData.put("FileUrl", storageReference.toString());

        if (isAnonym) {
            firebaseFirestore.collection("uploadData/anonym/anonymDocs")
                    .document(docId).update(updateData).addOnSuccessListener(activity, aVoid -> {
                fileUploadCallback.onSuccess(storageReference.toString());
            }).addOnFailureListener(activity, fileUploadCallback::onFailure);
        } else {
            firebaseFirestore.collection("uploadData/normal/" + firebaseUser.getUid())
                    .document(docId).update(updateData).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    fileUploadCallback.onSuccess(storageReference.toString());
                }
            })
                    .addOnFailureListener(activity, fileUploadCallback::onFailure);
        }
    }

    @NotNull
    private String randomString(int sizeOfRandomString) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for(int i = 0; i<sizeOfRandomString; ++i)
            sb.append(allowedChars.charAt(random.nextInt(allowedChars.length())));
        return sb.toString();
    }

    public interface FileUploadCallback{
        void onSuccess(String url);
        void onFailure(Exception exception);
    }

    public void downloadFile(String docId){

        firebaseFirestore.collection("uploadData/normal/" + firebaseUser.getUid()).document(docId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (Objects.requireNonNull(document).exists()) {
                    FileUtil.chooseFolder(activity, null, (String path, File file) -> {
                        Map<String, Object> fileData = document.getData();
                        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(Objects.requireNonNull(fileData).get("FileUrl")).toString());

                        File localFile = null;
                        try {
                            localFile = File.createTempFile(path+"/"+fileData.get("FileName").toString().replaceAll("."+fileData.get("FileExtension").toString(), "") , fileData.get("FileExtension").toString() );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        storageReference.getFile(Objects.requireNonNull(localFile)).addOnSuccessListener(taskSnapshot -> {
                        });
                    });
                } else
                    Log.d("TAG", "No such document");

            } else
                Log.d("TAG", "get failed with ", task.getException());
        });
    }
}
