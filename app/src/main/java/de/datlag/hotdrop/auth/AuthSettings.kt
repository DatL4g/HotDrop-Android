package de.datlag.hotdrop.auth

import android.content.DialogInterface
import androidx.core.os.LocaleListCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.datlag.hotdrop.R
import de.datlag.hotdrop.auth.UserManager.LogoutCallback
import de.datlag.hotdrop.auth.UserManager.UserNameCallback
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.view.helper.MaterialSnackbar.configSnackbar
import java.text.SimpleDateFormat
import java.util.*

class AuthSettings(private val activity: AdvancedActivity) {
    private var signInArray: Array<String> = activity.resources.getStringArray(R.array.sign_in_options)

    fun providerDialog(providerCallback: ProviderCallback) {
        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle("Provider")
                .setItems(signInArray) { _: DialogInterface?, i: Int -> providerCallback.onChosen(i) }
                .setPositiveButton(activity.getString(R.string.cancel), null)
                .create()).show()
    }

    fun infoDialog(userManager: UserManager) {
        userManager.getUserName(object : UserNameCallback {
            override fun onSuccess(username: String) {
                activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                        .setTitle("Account")
                        .setMessage("Logged in as " + username
                                + "\nCreated at: " + getDate(userManager.firebaseUser.metadata!!.creationTimestamp)
                                + "\nLast login: " + getDate(userManager.firebaseUser.metadata!!.lastSignInTimestamp))
                        .setPositiveButton(activity.getString(R.string.okay), null)
                        .setNeutralButton("Logout") { _: DialogInterface?, _: Int ->
                            userManager.logout(object : LogoutCallback {
                                override fun onLogoutSuccess() {
                                    val snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "Logout successful", Snackbar.LENGTH_LONG)
                                    configSnackbar(activity, snackbar)
                                    snackbar.show()
                                }

                                override fun onLogoutFailed() {
                                    val snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "Logout failed, try again", Snackbar.LENGTH_LONG)
                                    configSnackbar(activity, snackbar)
                                    snackbar.show()
                                }
                            })
                        }
                        .create()).show()
            }

            override fun onFailed(ignored: Exception?) {}
        })
    }

    private fun getDate(schlong: Long): String {
        val sfd = SimpleDateFormat("dd.MM.yyyy", LocaleListCompat.getDefault()[0])
        return sfd.format(Date(schlong))
    }

    interface ProviderCallback {
        fun onChosen(i: Int)
    }
}