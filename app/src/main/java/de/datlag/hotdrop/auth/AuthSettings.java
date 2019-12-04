package de.datlag.hotdrop.auth;

import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.view.helper.MaterialSnackbar;

public class AuthSettings {

    private AdvancedActivity activity;
    private String[] signInArray;

    public AuthSettings(AdvancedActivity activity) {
        this.activity = activity;

        init();
    }

    private void init() {
        signInArray = activity.getResources().getStringArray(R.array.sign_in_options);
    }

    public void providerDialog(ProviderCallback providerCallback) {
        activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                .setTitle("Provider")
                .setItems(signInArray, (DialogInterface dialogInterface, int i) -> {
                    providerCallback.onChosen(i);
                })
                .setPositiveButton(activity.getString(R.string.cancel), null)
                .create()).show();
    }

    public void infoDialog(@NotNull UserManager userManager) {
        userManager.getUserName(new UserManager.UserNameCallback() {
            @Override
            public void onSuccess(String username) {
                activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                        .setTitle("Account")
                        .setMessage("Logged in as "+username)
                        .setPositiveButton(activity.getString(R.string.okay), null)
                        .setNeutralButton("Logout", (DialogInterface dialogInterface, int i) -> {
                                userManager.logout(new UserManager.LogoutCallback() {
                                    public void onLogoutSuccess() {
                                        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "Logout successful", Snackbar.LENGTH_LONG);
                                        MaterialSnackbar.configSnackbar(activity, snackbar);
                                        snackbar.show();
                                    }

                                    public void onLogoutFailed() {
                                        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "Logout failed, try again", Snackbar.LENGTH_LONG);
                                        MaterialSnackbar.configSnackbar(activity, snackbar);
                                        snackbar.show();
                                    }
                                });
                        })
                        .create()).show();
            }

            @Override
            public void onFailed(Exception exception) {

            }
        });
    }

    public interface ProviderCallback {
        void onChosen(int i);
    }
}
