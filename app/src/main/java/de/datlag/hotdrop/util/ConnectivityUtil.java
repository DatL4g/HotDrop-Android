package de.datlag.hotdrop.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

import androidx.core.net.ConnectivityManagerCompat;

import java.util.Objects;

public class ConnectivityUtil {

    public static boolean isMetered(Activity activity) {
        return ConnectivityManagerCompat.isActiveNetworkMetered((ConnectivityManager) Objects.requireNonNull(activity.getSystemService(Context.CONNECTIVITY_SERVICE)));
    }
}
