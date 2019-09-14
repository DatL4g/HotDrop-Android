package de.datlag.hotdrop.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import de.datlag.hotdrop.R;

public class FileUtil {

    public static void chooseFile(Activity activity, FileChooseCallback fileChooseCallback) {
        new ChooserDialog(activity, R.style.FileChooserStyle)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        fileChooseCallback.onChosen(path, pathFile);
                    }
                })
                // to handle the back key pressed or clicked outside the dialog:
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                })
                .build()
                .show();
    }

    public static void chooseFolder(Activity activity, FolderChooseCallback folderChooseCallback) {
        new ChooserDialog(activity, R.style.FileChooserStyle)
                .withFilter(true, false)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        folderChooseCallback.onChosen(path, pathFile);
                    }
                })
                // to handle the back key pressed or clicked outside the dialog:
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                })
                .build()
                .show();
    }

    public static JsonObject jsonObjectFromFile(@NotNull Context context, File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(context.getString(R.string.path), file.getAbsolutePath());
        jsonObject.addProperty(context.getString(R.string.name), file.getName());
        jsonObject.addProperty(context.getString(R.string.mime), MimeTypes.getMimeType(extension));
        jsonObject.addProperty(context.getString(R.string.extension), extension);
        jsonObject.addProperty(context.getString(R.string.base64), FileUtil.toBase64String(file));
        return jsonObject;
    }

    public static void writeBytesToFile(byte[] bytes, String file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFile(String path, String filename) {
        makeDir(path);
        File file = new File(path+File.separator+filename);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getMimeType(byte[] bytes) {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(bytes));
        String mimeType = null;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mimeType;
    }

    public static String getMimeType(Activity activity, @NotNull Uri uri) {
        String mimeType = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Path path = Paths.get(uri.toString());
            try {
                mimeType = Files.probeContentType(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
                ContentResolver cr = activity.getContentResolver();
                mimeType = cr.getType(uri);
            } else {
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
        }
        return mimeType;
    }

    public static String toBase64String(@NotNull File file) {
        byte[] bytes = new byte[(int) file.length()];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String toBase64String(@NotNull byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] base64ToBytes(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    @NotNull
    public static byte[] jsonObjectToBytes(@NotNull JsonObject jsonObject) {
        return jsonObject.toString().getBytes();
    }

    public static JsonObject jsonObjectFromBytes(byte[] bytes) {
        return new Gson().fromJson(new String(bytes), JsonObject.class);
    }

    public interface FileChooseCallback {
        void onChosen(String path, File file);
    }

    public interface FolderChooseCallback {
        void onChosen(String path, File file);
    }

    public static void makeDir(String path) {
        if(!existsFile(path)) {
            File file = new File(path);
            file.mkdirs();
        }
    }

    public static boolean existsFile(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean isDirectory(String path) {
        if (!existsFile(path)) {
            return false;
        }

        return new File(path).isDirectory();
    }

    public static boolean isFile(String path) {
        if (!existsFile(path)) {
            return false;
        }

        return new File(path).isFile();
    }
}