package de.datlag.hotdrop.utils;

import android.app.Activity;
import android.content.ContentResolver;

import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileUtil {

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
}