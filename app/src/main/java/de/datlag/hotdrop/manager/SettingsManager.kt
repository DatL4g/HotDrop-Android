package de.datlag.hotdrop.manager

import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity

class SettingsManager(private val activity: AdvancedActivity) {
    private var settingsOptions: Array<String> = activity.resources.getStringArray(R.array.available_settings)

    fun open() {
        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.settings))
                .setItems(settingsOptions) { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                    chooseSetting(which)
                }
                .setPositiveButton(activity.getString(R.string.close), null).create()).show()
    }

    private fun chooseSetting(which: Int) {
        when (which) {
            0 -> {
            }
            1 -> activity.browserIntent(activity.getString(R.string.donate_link))
            else -> open()
        }
    }
}