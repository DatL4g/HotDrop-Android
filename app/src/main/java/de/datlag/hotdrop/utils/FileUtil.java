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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.zeroturnaround.zip.ZipUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import de.datlag.hotdrop.R;

public class FileUtil {

    public static final int MIN_LARGE_HEAP_SIZE = 11000000;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

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

    public static void chooseFile(Activity activity, FileFilter fileFilter, FileChooseCallback fileChooseCallback) {
        new ChooserDialog(activity, R.style.FileChooserStyle)
                .withFilter(fileFilter)
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

    public static void chooseFile(Activity activity, String startingPath, FileChooseCallback fileChooseCallback) {
        new ChooserDialog(activity, R.style.FileChooserStyle)
                .withStartFile(startingPath)
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

    public static void chooseFolder(Activity activity, String startingPath, FolderChooseCallback folderChooseCallback) {
        new ChooserDialog(activity, R.style.FileChooserStyle)
                .withFilter(true, false)
                .withStartFile(startingPath)
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

    public static void chooseAny(Activity activity, AnyChooseCallback anyChooseCallback) {
        new ChooserDialog(activity, R.style.FileChooserStyle)
                .withFilter(false, true)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String dir, File dirFile) {
                        if (isFile(dir)) {
                            anyChooseCallback.onChosenFile(dir, dirFile);
                        } else if (isDirectory(dir)) {
                            anyChooseCallback.onChosenFolder(dir, dirFile);
                        }
                    }
                })
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.cancel();
                    }
                })
                .withNegativeButton("Choose Folder", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String dir, File dirFile) {
                                if (isFile(dir)) {
                                    anyChooseCallback.onChosenFile(dir, dirFile);
                                } else if (isDirectory(dir)) {
                                    anyChooseCallback.onChosenFolder(dir, dirFile);
                                }
                            }
                        };
                    }
                })
                .build().show();
    }

    public static JsonObject jsonObjectFromFile(@NotNull Context context, File file, @NotNull ArrayList<byte[]> bytes, int pos) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        JsonObject jsonObject = new JsonObject();
        ArrayList<String> base64List = new ArrayList<>();
        for (byte[] byteArray : bytes) {
            base64List.add(FileUtil.toBase64String(byteArray));
        }

        jsonObject.addProperty(context.getString(R.string.path), file.getAbsolutePath());
        jsonObject.addProperty(context.getString(R.string.name), file.getName());
        jsonObject.addProperty(context.getString(R.string.mime), MimeTypes.getMimeType(extension));
        jsonObject.addProperty(context.getString(R.string.extension), extension);
        jsonObject.addProperty(context.getString(R.string.base64), FileUtil.toBase64String(bytes.get(pos)));
        jsonObject.addProperty(context.getString(R.string.size), file.length());
        jsonObject.addProperty(context.getString(R.string.part), pos);
        jsonObject.addProperty(context.getString(R.string.maxParts), bytes.size());
        return jsonObject;
    }

    @NotNull
    public static byte[] jsonObjectToBytes(@NotNull JsonObject jsonObject) {
        return jsonObject.toString().getBytes();
    }

    private static ArrayList<byte[]> splitFileToBytes(File file){
        ArrayList<byte[]> returnValue = new ArrayList<>();
        int chunkSize = MIN_LARGE_HEAP_SIZE;
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        long fileSize = file.length();

        int subfile;
        for (subfile = 0; subfile < fileSize / chunkSize; subfile++)
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (int currentByte = 0; currentByte < chunkSize; currentByte++)
            {
                try {
                    outputStream.write(in.read());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            returnValue.add(outputStream.toByteArray());
        }

        if (fileSize != chunkSize * (subfile - 1))
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int b = 0;
            while (true) {
                try {
                    if (!((b = in.read()) != -1)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream.write(b);
            }

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            returnValue.add(outputStream.toByteArray());
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    public static ArrayList<byte[]> byteArraysFromFile(File file) {
        ArrayList<byte[]> byteArrayList = new ArrayList<>();
        if (isParted(file)) {
            byteArrayList = splitFileToBytes(file);
        } else {
            byteArrayList.add(readFileToByteArray(file));
        }
        return byteArrayList;
    }

    @NotNull
    @Contract("_ -> new")
    public static FileInputStream openInputStream(@NotNull File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    @NotNull
    public static byte[] readFileToByteArray(File file) {
        InputStream in = null;
        try {
            try {
                in = openInputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toByteArray(in);
        } finally {
            closeQuietly(in);
        }
    }

    @NotNull
    public static byte[] toByteArray(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(@NotNull InputStream input, OutputStream output) {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (true) {
            try {
                if (!(-1 != (n = input.read(buffer)))) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.write(buffer, 0, n);
            } catch (IOException e) {
                e.printStackTrace();
            }
            count += n;
        }
        return count;
    }

    public static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @NotNull
    public static String readableFileSize(long size) {
        if(size <= 0){
            return "0";
        }
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean isParted(@NotNull File file) {
        if (file.length() > MIN_LARGE_HEAP_SIZE) {
            return true;
        }
        return false;
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

    public static String toBase64String(@NotNull byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] base64ToBytes(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    public static JsonObject jsonObjectFromBytes(byte[] bytes) {
        return new Gson().fromJson(new String(bytes), JsonObject.class);
    }

    public interface FileChooseCallback {
        void onChosen(String path, File file);
    }

    public interface FolderChooseCallback {
        void onChosen(String path, File file) throws IOException;
    }

    public interface AnyChooseCallback {
        void onChosenFolder(String path, File file);

        void onChosenFile(String path, File file);
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

    public static boolean existsFile(@NotNull File file) {
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

    public static boolean isDirectory(File file) {
        if (!existsFile(file)) {
            return false;
        }

        return file.isDirectory();
    }

    public static boolean isFile(File file) {
        if (!existsFile(file)) {
            return false;
        }

        return file.isFile();
    }

    @NotNull
    public static File folderToFile(@NotNull Context context, @NotNull File file) {
        File tempDir = context.getCacheDir();
        File tempFile = null;
        try {
            tempFile = File.createTempFile(file.getName(), ".zip", tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZipUtil.pack(file, tempFile);
        Objects.requireNonNull(tempFile).deleteOnExit();

        return tempFile;
    }

    @NotNull
    public static File folderToFile(@NotNull Context context, String path) {
        File file = new File(path);
        File tempDir = context.getCacheDir();
        File tempFile = null;
        try {
            tempFile = File.createTempFile(file.getName(), ".zip", tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZipUtil.pack(file, tempFile);
        Objects.requireNonNull(tempFile).deleteOnExit();

        return tempFile;
    }

    public static String getFileExtension(File file) {
        return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
    }
}