package de.datlag.hotdrop.manager

import android.content.DialogInterface
import android.util.Log
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import de.datlag.hotdrop.R
import de.datlag.hotdrop.auth.AuthSettings
import de.datlag.hotdrop.auth.UserManager
import de.datlag.hotdrop.auth.UserManager.UserNameCallback
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.view.helper.MaterialSnackbar.configSnackbar
import de.interaapps.firebasemanager.core.FirebaseManager

class SettingsManager(private val activity: AdvancedActivity, private val firebaseManager: FirebaseManager) {
    private var settingsOptions: Array<String> = activity.resources.getStringArray(R.array.available_settings)
    private var userManager: UserManager = UserManager(activity, firebaseManager)
    private var authSettings: AuthSettings = AuthSettings(activity)

    fun open() {
        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.settings))
                .setItems(settingsOptions) { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                    chooseSetting(which)
                }
                .setPositiveButton(activity.getString(R.string.close), null).create()).show()
    }

    fun chooseSetting(which: Int) {
        when (which) {
            0 -> if (firebaseManager.authManager.isLoggedIn) {
                authSettings.infoDialog(userManager)
            } else {
                authSettings.providerDialog(object: AuthSettings.ProviderCallback {
                    override fun onChosen(i: Int) {
                        val coordinatorLayout = activity.findViewById<CoordinatorLayout>(R.id.coordinator)
                        userManager.login(firebaseManager.authManager.login[i], object : UserManager.LoginCallback {
                            override fun onLoginSuccess(authResult: AuthResult) {
                                userManager.getUserName(object : UserNameCallback {
                                    override fun onSuccess(username: String) {
                                        val snackbar = Snackbar.make(coordinatorLayout, "Welcome $username", Snackbar.LENGTH_LONG)
                                        configSnackbar(activity, snackbar)
                                        snackbar.show()
                                    }

                                    override fun onFailed(exception: Exception?) {
                                        val snackbar = Snackbar.make(coordinatorLayout, "Login Successful", Snackbar.LENGTH_LONG)
                                        configSnackbar(activity, snackbar)
                                        snackbar.show()
                                    }
                                })
                            }

                            override fun onLoginFailed(exception: Exception?) {
                                if (exception is FirebaseAuthUserCollisionException) {
                                    val snackbar = Snackbar.make(coordinatorLayout, "Email Address already in use", Snackbar.LENGTH_LONG)
                                    configSnackbar(activity, snackbar)
                                    snackbar.show()
                                } else {
                                    val snackbar = Snackbar.make(coordinatorLayout, "Login Failure", Snackbar.LENGTH_LONG)
                                    configSnackbar(activity, snackbar)
                                    snackbar.show()
                                    Log.e("Exception", exception.toString())
                                }
                            }
                        })
                    }
                })
            }
            1 -> {
            }
            2 -> activity.browserIntent(activity.getString(R.string.donate_link))
            else -> open()
        }
    }
}