package de.datlag.hotdrop.extend

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.util.TypedValue
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import de.datlag.hotdrop.R
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.util.*

abstract class AdvancedActivity : AppCompatActivity() {
    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    fun <ViewT: View> Activity.bindView(@IdRes idRes: Int): Lazy<ViewT> {
        return lazy(LazyThreadSafetyMode.NONE) {
            findViewById<ViewT>(idRes)
        }
    }

    protected fun checkConfiguration() {
        onConfigurationChanged(this.resources.configuration)
    }

    fun switchFragment(fragment: Fragment, viewId: Int) {
        val fragmentManager = this.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        fragmentTransaction.replace(viewId, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    val localeList: LocaleListCompat
    get() {
        return LocaleListCompat.getDefault()
    }

    val actionBarHeight: Int
        get() {
            val tv = TypedValue()
            return if (this.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                TypedValue.complexToDimensionPixelSize(tv.data, this.resources.displayMetrics)
            } else 112
        }

    fun applyDialogAnimation(alertDialog: AlertDialog): AlertDialog {
        alertDialog.window!!.attributes.windowAnimations = R.style.MaterialDialogAnimation
        return alertDialog
    }

    fun browserIntent(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        this.startActivity(browserIntent)
    }

    fun copyText(description: String?, text: String) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(description, text)
        Objects.requireNonNull(clipboard).setPrimaryClip(clip)
    }

    fun createShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            val shortcutInfo = ShortcutInfo.Builder(this, "id1")
                    .setShortLabel("Donate")
                    .setLongLabel("Donate to help maintain the project")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_credit_card_black_24dp))
                    .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_link))))
                    .build()
            shortcutManager.dynamicShortcuts = listOf(shortcutInfo)
        }
    }
}