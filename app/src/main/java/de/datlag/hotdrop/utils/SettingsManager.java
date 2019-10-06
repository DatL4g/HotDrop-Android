package de.datlag.hotdrop.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import de.datlag.hotdrop.MainActivity;
import de.datlag.hotdrop.R;
import de.datlag.hotdrop.auth.AuthSettings;
import de.datlag.hotdrop.auth.UserManager;
import de.interaapps.firebasemanager.core.FirebaseManager;

public class SettingsManager {

    private Activity activity;
    private FirebaseManager firebaseManager;
    private String[] settingsArray;
    private UserManager userManager;
    private AuthSettings authSettings;

    public SettingsManager(Activity activity, FirebaseManager firebaseManager) {
        this.activity = activity;
        this.firebaseManager = firebaseManager;

        init();
    }

    private void init() {
        settingsArray = activity.getResources().getStringArray(R.array.available_settings);
        userManager = new UserManager(activity, firebaseManager);
        authSettings = new AuthSettings(activity);
    }

    public void open() {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.settings))
                .setItems(settingsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switchSettings(which);
                    }
                })
                .setPositiveButton(activity.getString(R.string.close), null)
                .show();
    }

    public void switchSettings(int selected) {
        switch (selected) {
            case 0:
                if (userManager.isLoggedIn()) {
                    authSettings.infoDialog(userManager);
                } else {
                    authSettings.providerDialog(new AuthSettings.ProviderCallback() {
                        @Override
                        public void onChosen(int i) {
                            userManager.login(firebaseManager.getLogin().get(i), new UserManager.LoginCallback() {
                                @Override
                                public void onLoginSuccess(AuthResult authResult) {
                                    if (activity instanceof MainActivity) {
                                        userManager.getUserName(new UserManager.UserNameCallback() {
                                            @Override
                                            public void onSuccess(String username) {
                                                Snackbar snackbar = Snackbar.make(((MainActivity) activity).getCoordinatorLayout(), "Welcome "+username, Snackbar.LENGTH_LONG);
                                                ((MainActivity) activity).showSnackbar(snackbar);
                                            }

                                            @Override
                                            public void onFailed(Exception exception) {
                                                Snackbar snackbar = Snackbar.make(((MainActivity) activity).getCoordinatorLayout(), "Login Successful", Snackbar.LENGTH_LONG);
                                                ((MainActivity) activity).showSnackbar(snackbar);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onLoginFailed(Exception exception) {
                                    if (activity instanceof MainActivity) {
                                        if (exception instanceof FirebaseAuthUserCollisionException) {
                                            Snackbar snackbar = Snackbar.make(((MainActivity) activity).getCoordinatorLayout(), "Email Address allready in use", Snackbar.LENGTH_LONG);
                                            ((MainActivity) activity).showSnackbar(snackbar);
                                        } else {
                                            Snackbar snackbar = Snackbar.make(((MainActivity) activity).getCoordinatorLayout(), "Login Failure", Snackbar.LENGTH_LONG);
                                            ((MainActivity) activity).showSnackbar(snackbar);
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
                break;
            case 1:
                break;
            case 2:
                List<String> statusList = new ArrayList<>();
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    statusList.add("Location: Granted");
                } else {
                    statusList.add("Location: Denied");
                }

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    statusList.add("Read Storage: Granted");
                } else {
                    statusList.add("Read Storage: Denied");
                }

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    statusList.add("Write Storage: Granted");
                } else {
                    statusList.add("Write Storage: Denied");
                }

                new MaterialAlertDialogBuilder(activity)
                        .setTitle("Permission")
                        .setItems(statusList.toArray(new String[0]), null)
                        .setPositiveButton(activity.getString(R.string.okay), null)
                        .create().show();

                break;
        }
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
