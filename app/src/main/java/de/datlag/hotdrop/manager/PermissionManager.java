package de.datlag.hotdrop.manager;

import android.Manifest;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;

public class PermissionManager {

    private AdvancedActivity activity;

    public PermissionManager(AdvancedActivity activity) {
        this.activity = activity;
    }

    public void check() {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                StringBuilder message = new StringBuilder();
                                String[] permissionInfo = activity.getResources().getStringArray(R.array.permission_info);
                                String[] permissionNotGranted = activity.getResources().getStringArray(R.array.permission_not_granted);

                                for (int i = 0; i < permissionNotGranted.length; i++) {
                                    message.append(permissionNotGranted[i].concat((i + 1 == permissionNotGranted.length) ? "\n\n\n" : "\n"));
                                }

                                for (int i = 0; i < permissionInfo.length; i++) {
                                    message.append(permissionInfo[i].concat((i + 1 == permissionInfo.length) ? "" : "\n\n"));
                                }


                                activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                                        .setTitle(activity.getString(R.string.not_granted))
                                        .setMessage(message.toString())
                                        .setPositiveButton(activity.getString(R.string.close), (dialogInterface, i) -> activity.finishAffinity())
                                        .create()).show();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        StringBuilder message = new StringBuilder();
                        String[] permissionInfo = activity.getResources().getStringArray(R.array.permission_info);
                        for (int i = 0; i < permissionInfo.length; i++) {
                            message.append(permissionInfo[i].concat((i + 1 == permissionInfo.length) ? "" : "\n\n"));
                        }

                        activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                                .setTitle(activity.getString(R.string.location_storage))
                                .setMessage(message.toString())
                                .setPositiveButton(activity.getString(R.string.okay), (DialogInterface dialogInterface, int i) -> {
                                    token.continuePermissionRequest();
                                })
                                .create()).show();
                    }
                })
                .check();
    }

}
