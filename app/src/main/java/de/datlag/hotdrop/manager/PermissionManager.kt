package de.datlag.hotdrop.manager

import android.Manifest
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity

class PermissionManager(private val activity: AdvancedActivity) {
    fun check() {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (!report.areAllPermissionsGranted()) {
                            if (report.isAnyPermissionPermanentlyDenied) {
                                val message = StringBuilder()
                                val permissionInfo = activity.resources.getStringArray(R.array.permission_info)
                                val permissionNotGranted = activity.resources.getStringArray(R.array.permission_not_granted)
                                for (i in permissionNotGranted.indices) {
                                    message.append(permissionNotGranted[i] + if (i + 1 == permissionNotGranted.size) "\n\n\n" else "\n")
                                }
                                for (i in permissionInfo.indices) {
                                    message.append(permissionInfo[i] + if (i + 1 == permissionInfo.size) "" else "\n\n")
                                }
                                activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                                        .setTitle(activity.getString(R.string.not_granted))
                                        .setMessage(message.toString())
                                        .setPositiveButton(activity.getString(R.string.close)) { _: DialogInterface?, _: Int -> activity.finishAffinity() }
                                        .create()).show()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        val message = StringBuilder()
                        val permissionInfo = activity.resources.getStringArray(R.array.permission_info)
                        for (i in permissionInfo.indices) {
                            message.append(permissionInfo[i] + if (i + 1 == permissionInfo.size) "" else "\n\n")
                        }
                        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                                .setTitle(activity.getString(R.string.location_storage))
                                .setMessage(message.toString())
                                .setPositiveButton(activity.getString(R.string.okay)) { _: DialogInterface?, _: Int -> token.continuePermissionRequest() }
                                .create()).show()
                    }
                })
                .check()
    }

}