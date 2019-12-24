package de.datlag.hotdrop.util

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.net.ConnectivityManagerCompat
import java.util.*

object ConnectivityUtil {
    fun isMetered(activity: Activity): Boolean {
        return ConnectivityManagerCompat.isActiveNetworkMetered(activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    }
}