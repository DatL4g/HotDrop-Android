package de.datlag.hotdrop.firebase;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.datlag.hotdrop.MainActivity;
import de.datlag.hotdrop.utils.FileUtil;
import de.interaapps.firebasemanager.core.FirebaseManager;
import de.interaapps.firebasemanager.core.auth.Auth;

/*
* TODO: Clean the fucking file.
* */

public class StorageManager {
    private Activity activity;
    private FirebaseManager firebaseManager;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();
    private DateChecker dateChecker;

    public StorageManager(Activity activity, FirebaseManager firebaseManager) {
        this.activity = activity;
        this.firebaseManager = firebaseManager;

        init();
    }

    private FirebaseUser getUser() {
        for (Auth auth : firebaseManager.getLogin()) {
            if (auth.getUser() != null) {
                return auth.getUser();
            }
        }

        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void init() {
        dateChecker = new DateChecker(activity);
    }

    public void startUploadFile(boolean isAnonym, FileUploadCallback fileUploadCallback) {
        FileFilter fileFilter = new FileFilter() {
            @Contract(pure = true)
            @Override
            public boolean accept(@NotNull File file) {
                return file.length() < 50000000;
            }
        };

        FileUtil.chooseFile(activity, fileFilter, new FileUtil.FileChooseCallback() {
            @Override
            public void onChosen(String path, File file) {
                startByDBEntry(file, isAnonym, fileUploadCallback);
            }
        });
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
                    .addOnSuccessListener(activity, new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            uploadFile(file, documentReference.getId(), true, fileUploadCallback);
                        }
                    }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    fileUploadCallback.onFailure(e);
                }
            });
        } else {
            firebaseFirestore.collection("uploadData/normal/" + getUser().getUid())
                    .add(upload)
                    .addOnSuccessListener(activity, new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            uploadFile(file, documentReference.getId(), false, fileUploadCallback);
                        }
                    }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    fileUploadCallback.onFailure(e);
                }
            });
        }
    }

    private void uploadFile(@NotNull File file, String docID, boolean isAnonym, FileUploadCallback fileUploadCallback) {
        StorageReference uploadRef;
        String randomId = docID+randomString(64)+file.getName();
        if (isAnonym) {
            uploadRef = storageReference.child("anonym/" + randomId);
        } else {
            uploadRef = storageReference.child("normal/" + getUser().getUid() + "/" + randomId);
        }

        UploadTask uploadTask = uploadRef.putFile(Uri.fromFile(file));
        uploadTask.addOnSuccessListener(activity, taskSnapshot -> updateDBEntry(uploadRef, docID, isAnonym, fileUploadCallback))
        .addOnFailureListener(activity, e -> {
            fileUploadCallback.onFailure(e);
        } );
    }

    private void updateDBEntry(@NotNull StorageReference storageReference, String docId, boolean isAnonym, FileUploadCallback fileUploadCallback) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("UploadStatus", true);
        updateData.put("FileUrl", storageReference.toString());

        if (isAnonym) {
            firebaseFirestore.collection("uploadData/anonym/anonymDocs")
            .document(docId).update(updateData).addOnSuccessListener(activity, aVoid -> {
                fileUploadCallback.onSuccess(storageReference.toString());
            }).addOnFailureListener(activity, e -> {
                    fileUploadCallback.onFailure(e);
                });
        } else {
            firebaseFirestore.collection("uploadData/normal/" + getUser().getUid())
                    .document(docId).update(updateData).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    fileUploadCallback.onSuccess(storageReference.toString());
                }
            })
            .addOnFailureListener(activity, (@NonNull Exception e) -> {
                    fileUploadCallback.onFailure(e);
            });
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

        Log.d("ROFL", "uploadData/normal/" + getUser().getUid());
        firebaseFirestore.collection("uploadData/normal/" + getUser().getUid()).document(docId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        FileUtil.chooseFolder(activity, null, (String path, File file) -> {
                                Map<String, Object> fileData = document.getData();
                                StorageReference storageReference = firebaseStorage.getReferenceFromUrl(fileData.get("FileUrl").toString());

                                File localFile = null;
                                try {
                                    localFile = File.createTempFile(path+"/"+fileData.get("FileName").toString().replaceAll("."+fileData.get("FileExtension").toString(), "") , fileData.get("FileExtension").toString() );
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Log.d("ASD", taskSnapshot.toString());
                                    }
                                });
                        });
                    } else
                        Log.d("TAG", "No such document");

                } else
                    Log.d("TAG", "get failed with ", task.getException());
            }
        });
    }
}
