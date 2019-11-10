package de.datlag.hotdrop.auth;

import android.app.Activity;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import de.datlag.hotdrop.R;

public class AuthSettings {

    private Activity activity;
    private String[] signInArray;

    public AuthSettings(Activity activity) {
        this.activity = activity;

        init();
    }

    private void init() {
        signInArray = activity.getResources().getStringArray(R.array.sign_in_options);
    }

    public void providerDialog(ProviderCallback providerCallback) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Provider")
                .setItems(signInArray, (DialogInterface dialogInterface, int i) -> {
                    providerCallback.onChosen(i);
                })
                .setPositiveButton(activity.getString(R.string.cancel), null)
                .create().show();
    }

    public void infoDialog(@NotNull UserManager userManager) {
        userManager.getUserName(new UserManager.UserNameCallback() {
            @Override
            public void onSuccess(String username) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle("Account")
                        .setMessage("Logged in as "+username)
                        .setPositiveButton(activity.getString(R.string.okay), null)
                        .setNeutralButton("Logout", (DialogInterface dialogInterface, int i) -> {
                                userManager.logout(new UserManager.LogoutCallback() {
                                    public void onLogoutSuccess() {
                                    }

                                    public void onLogoutFailed() {
                                    }
                                });
                        })
                        .create().show();
            }

            public void onFailed(Exception exception) {

            }
        });
    }

    public interface ProviderCallback {
        void onChosen(int i);
    }
}
