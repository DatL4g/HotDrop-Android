package de.datlag.hotdrop.util;

import android.app.Activity;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;

public class DeviceUtil {

    public enum DeviceType {
        WATCH(0),
        PHONE(1),
        PHABLET(2),
        TABLET(3),
        TV(4);

        private int value;
        private DeviceType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static int getDeviceType(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        if (diagonalInches > 10.1) {
            return DeviceType.TV.getValue();
        } else if (diagonalInches <= 10.1 && diagonalInches > 7) {
            return DeviceType.TABLET.getValue();
        } else if (diagonalInches <= 7 && diagonalInches > 6.5) {
            return DeviceType.PHABLET.getValue();
        } else if (diagonalInches <= 6.5 && diagonalInches >= 2) {
            return DeviceType.PHONE.getValue();
        } else {
            return DeviceType.WATCH.getValue();
        }
    }

    public static String getDeviceName(Activity activity) {
        String deviceName = Build.MODEL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            String customName = Settings.Global.getString(activity.getContentResolver(), Settings.Global.DEVICE_NAME);
            if (customName.length() > 0) {
                deviceName = customName;
            }
        }
        return deviceName;
    }
}
