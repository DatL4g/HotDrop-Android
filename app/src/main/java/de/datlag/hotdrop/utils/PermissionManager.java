package de.datlag.hotdrop.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import de.datlag.hotdrop.R;

public class PermissionManager {

    private Activity activity;

    public static final int LOCATION_PERMISSION_CODE = 420;
    public static final int STORAGE_READ_PERMISSION_CODE = 421;
    public static final int STORAGE_WRITE_PERMISSION_CODE = 422;

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public void permissionCheck(@NotNull String[] permissions, final int[] permissionCodes) {
        for (int i = 0; i < permissions.length; i++) {
            final String permission = permissions[i];
            final int permissionCode = permissionCodes[i];

            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);

                switch (permission) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        builder.setTitle(activity.getString(R.string.location));
                        builder.setMessage(activity.getString(R.string.location_needed));

                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        builder.setTitle("Read Storage");
                        builder.setMessage("This permission is needed to send and receive files and folders");
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        builder.setTitle("Write Storage");
                        builder.setMessage("This permission is needed to send and receive files and folders");
                        break;
                }

                builder.setPositiveButton(activity.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCode);
                    }
                });
                builder.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCodes[i]);
            }
        }
    }
}
