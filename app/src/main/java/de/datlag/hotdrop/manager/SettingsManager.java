package de.datlag.hotdrop.manager;

import android.app.Activity;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.auth.AuthSettings;
import de.datlag.hotdrop.auth.UserManager;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.view.helper.MaterialSnackbar;
import de.interaapps.firebasemanager.core.FirebaseManager;

public class SettingsManager {

    private AdvancedActivity activity;
    private FirebaseManager firebaseManager;
    private String[] settingsOptions;
    private UserManager userManager;
    private AuthSettings authSettings;

    public SettingsManager(AdvancedActivity activity, FirebaseManager firebaseManager) {
        this.activity = activity;
        this.firebaseManager = firebaseManager;

        init();
    }

    private void init() {
        settingsOptions = activity.getResources().getStringArray(R.array.available_settings);
        userManager = new UserManager(activity, firebaseManager);
        authSettings = new AuthSettings(activity);
    }

    public void open() {
        activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.settings))
                .setItems(settingsOptions, (dialog, which) -> {
                    dialog.dismiss();
                    chooseSetting(which);
                })
                .setPositiveButton(activity.getString(R.string.close), null).create()).show();
    }

    public void chooseSetting(int which) {
        switch (which) {
            case 0:
                if(userManager.isLoggedIn()) {
                    authSettings.infoDialog(userManager);
                } else {
                    authSettings.providerDialog(i -> {
                        CoordinatorLayout coordinatorLayout = activity.findViewById(R.id.coordinator);

                        userManager.login(firebaseManager.getLogin().get(i), new UserManager.LoginCallback() {
                            @Override
                            public void onLoginSuccess(AuthResult authResult) {
                                    userManager.getUserName(new UserManager.UserNameCallback() {
                                        @Override
                                        public void onSuccess(String username) {
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Welcome "+username, Snackbar.LENGTH_LONG);
                                            MaterialSnackbar.configSnackbar(activity, snackbar);
                                            snackbar.show();
                                        }

                                        @Override
                                        public void onFailed(Exception exception) {
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Login Successful", Snackbar.LENGTH_LONG);
                                            MaterialSnackbar.configSnackbar(activity, snackbar);
                                            snackbar.show();
                                        }
                                    });
                            }

                            @Override
                            public void onLoginFailed(Exception exception) {
                                if (exception instanceof FirebaseAuthUserCollisionException) {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Email Address already in use", Snackbar.LENGTH_LONG);
                                    MaterialSnackbar.configSnackbar(activity, snackbar);
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Login Failure", Snackbar.LENGTH_LONG);
                                    MaterialSnackbar.configSnackbar(activity, snackbar);
                                    snackbar.show();
                                    Log.e("Exception", exception.toString());
                                }
                            }
                        });
                    });
                }
                break;
            case 1:
                //Connectivity
                break;
            case 2:
                //Billing
                break;

                default:
                    open();
                    break;
        }
    }
}
