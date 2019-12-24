package de.datlag.hotdrop.view.helper

import android.app.Activity
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import de.datlag.hotdrop.R

object MaterialSnackbar {
    private var margin = 24
    private var actionBarHeight = 112

    @JvmStatic
    fun configSnackbar(activity: Activity, snackbar: Snackbar) {
        getDimensions(activity)
        addMargins(snackbar)
        displayTop(snackbar)
        setCustomBackground(activity, snackbar)
        setCustomTextColor(activity, snackbar)
    }

    private fun getDimensions(activity: Activity) {
        margin = activity.resources.getDimension(R.dimen.snackbar_margin).toInt()
        val tv = TypedValue()
        if (activity.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, activity.resources.displayMetrics)
        }
    }

    private fun addMargins(snackbar: Snackbar) {
        val params = snackbar.view.layoutParams as MarginLayoutParams
        params.setMargins(margin, margin + actionBarHeight, margin, margin)
        snackbar.view.layoutParams = params
    }

    private fun setCustomBackground(activity: Activity, snackbar: Snackbar) {
        snackbar.view.background = ContextCompat.getDrawable(activity, R.drawable.snackbar_background)
    }

    private fun setCustomTextColor(activity: Activity, snackbar: Snackbar) {
        val appCompatTextView: AppCompatTextView = snackbar.view.findViewById(R.id.snackbar_text)
        appCompatTextView.setTextColor(ContextCompat.getColor(activity, R.color.snackbarText))
    }

    private fun displayTop(snackbar: Snackbar) {
        val params = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        snackbar.view.layoutParams = params
    }
}