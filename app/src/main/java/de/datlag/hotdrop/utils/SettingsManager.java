package de.datlag.hotdrop.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;

import de.datlag.hotdrop.R;
import de.interaapps.firebasemanager.auth.AnonymousAuth;
import de.interaapps.firebasemanager.auth.GoogleAuth;
import de.interaapps.firebasemanager.auth.PlayGamesAuth;
import de.interaapps.firebasemanager.core.FirebaseManager;
import de.interaapps.firebasemanager.core.auth.Auth;

public class SettingsManager {

    private Activity activity;
    private FirebaseManager firebaseManager;
    private PermissionManager permissionManager;
    private LoginCallbacks loginCallbacks;

    private String[] settingsArray;
    private String[] signInArray;

    public SettingsManager(Activity activity, FirebaseManager firebaseManager, PermissionManager permissionManager, LoginCallbacks loginCallbacks) {
        this.activity = activity;
        this.firebaseManager = firebaseManager;
        this.permissionManager = permissionManager;
        this.loginCallbacks = loginCallbacks;


        init();
    }

    private void init() {
        settingsArray = activity.getResources().getStringArray(R.array.available_settings);
        signInArray = activity.getResources().getStringArray(R.array.sign_in_options);
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

    private void switchSettings(int selected) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(settingsArray[selected]);

        switch (selected) {
            case 0:
                for (Auth auth : firebaseManager.getLogin().toArray(new Auth[0])) {
                    if (auth.getUser() != null) {
                        builder.setTitle("Logged in");
                        String userName = null;
                        if (auth.getUser() != null) {
                            userName = (auth.getUser().isAnonymous()) ? "Guest" : auth.getUser().getDisplayName();
                        }
                        builder.setMessage("Welcome, " + userName);
                        builder.setPositiveButton(activity.getString(R.string.close), null);
                        builder.setNeutralButton("Log out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (Auth auth : firebaseManager.getLogin().toArray(new Auth[0])) {
                                    if (auth instanceof GoogleAuth) {
                                        ((GoogleAuth) auth).signOut(new GoogleAuth.LogoutCallbacks() {
                                            @Override
                                            public void onLogoutSuccessful() {
                                                auth.signOut();
                                                ((GoogleAuth) auth).revokeAccess(new GoogleAuth.RevokeAccessCallbacks() {
                                                    @Override
                                                    public void onRevokeAccessSuccessful() {

                                                    }

                                                    @Override
                                                    public void onRevokeAccessFailed(Exception exception) {

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onLogoutFailed(Exception exception) {

                                            }
                                        });
                                    } else if (auth instanceof PlayGamesAuth) {
                                        ((PlayGamesAuth) auth).signOut(new PlayGamesAuth.LogoutCallbacks() {
                                            @Override
                                            public void onLogoutSuccessful() {

                                            }

                                            @Override
                                            public void onLogoutFailed(Exception exception) {

                                            }
                                        });
                                    } else {
                                        auth.signOut();
                                    }
                                }
                            }
                        });
                        break;
                    }
                }
                builder.setItems(signInArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Auth auth = firebaseManager.getLogin().get(which);
                        if (auth instanceof GoogleAuth) {
                            ((GoogleAuth) auth).startLogin(new GoogleAuth.LoginCallbacks() {
                                @Override
                                public void onLoginSuccessful(@Nullable AuthResult authResult) {
                                    loginCallbacks.onLoginSuccessful(authResult);
                                }

                                @Override
                                public void onLoginFailed(Exception exception) {
                                    loginCallbacks.onLoginFailed();
                                }
                            });
                        } else if (auth instanceof PlayGamesAuth) {
                            ((PlayGamesAuth) auth).startLogin(new PlayGamesAuth.LoginCallbacks() {
                                @Override
                                public void onLoginSuccessful(@Nullable AuthResult authResult) {
                                    loginCallbacks.onLoginSuccessful(authResult);
                                }

                                @Override
                                public void onLoginFailed(Exception exception) {
                                    loginCallbacks.onLoginFailed();
                                }
                            });
                        } else if (auth instanceof AnonymousAuth) {
                            ((AnonymousAuth) auth).startLogin(new AnonymousAuth.LoginCallbacks() {
                                @Override
                                public void onLoginSuccessful(AuthResult authResult) {
                                    loginCallbacks.onLoginSuccessful(authResult);
                                }

                                @Override
                                public void onLoginFailed(Exception exception) {
                                    loginCallbacks.onLoginFailed();
                                }
                            });
                        }
                    }
                });
                builder.setPositiveButton(activity.getString(R.string.close), null);
                break;
            case 1:
                builder.setMessage("Test: selected " + selected);
                break;
            case 2:
                ArrayList<String> statusList = new ArrayList<>();

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

                String[] status = statusList.toArray(new String[0]);

                builder.setItems(status, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                permissionManager.permissionCheck(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new int[]{PermissionManager.LOCATION_PERMISSION_CODE});
                                break;
                            case 1:
                                permissionManager.permissionCheck(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new int[]{PermissionManager.STORAGE_READ_PERMISSION_CODE});
                                break;
                            case 2:
                                permissionManager.permissionCheck(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new int[]{PermissionManager.STORAGE_WRITE_PERMISSION_CODE});
                                break;
                        }
                    }
                });
                builder.setPositiveButton(activity.getString(R.string.close), null);
                break;
        }

        builder.create().show();
    }

    public interface LoginCallbacks {
        void onLoginSuccessful(AuthResult authResult);

        void onLoginFailed();
    }
}
