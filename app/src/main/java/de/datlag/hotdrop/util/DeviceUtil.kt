package de.datlag.hotdrop.util

import android.app.Activity
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import kotlin.math.sqrt

object DeviceUtil {
    fun getDeviceType(activity: Activity): Int {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val yInches = metrics.heightPixels / metrics.ydpi
        val xInches = metrics.widthPixels / metrics.xdpi
        val diagonalInches = sqrt(xInches * xInches + yInches * yInches.toDouble())
        return if (diagonalInches > 10.1) {
            DeviceType.TV.value
        } else if (diagonalInches <= 10.1 && diagonalInches > 7) {
            DeviceType.TABLET.value
        } else if (diagonalInches <= 7 && diagonalInches > 6.5) {
            DeviceType.PHABLET.value
        } else if (diagonalInches in 2.0..6.5) {
            DeviceType.PHONE.value
        } else {
            DeviceType.WATCH.value
        }
    }

    fun getDeviceName(activity: Activity): String {
        var deviceName = Build.MODEL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val customName = Settings.Global.getString(activity.contentResolver, Settings.Global.DEVICE_NAME)
            if (customName.isNotEmpty()) {
                deviceName = customName
            }
        }
        return deviceName
    }

    enum class DeviceType(val value: Int) {
        WATCH(0), PHONE(1), PHABLET(2), TABLET(3), TV(4);

    }
}