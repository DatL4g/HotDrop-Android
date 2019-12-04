package de.datlag.hotdrop.extend;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import de.datlag.hotdrop.R;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public abstract class AdvancedActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    protected void checkConfiguration() {
        onConfigurationChanged(this.getResources().getConfiguration());
    }

    public void switchFragment(Fragment fragment, int viewId) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(viewId, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (this.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, this.getResources().getDisplayMetrics());
        }

        return 112;
    }

    public AlertDialog applyDialogAnimation(@NotNull AlertDialog alertDialog) {
        Objects.requireNonNull(alertDialog.getWindow()).getAttributes().windowAnimations = R.style.MaterialDialogAnimation;
        return alertDialog;
    }

    public void browserIntent(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        this.startActivity(browserIntent);
    }

    public void copyText(String description, String text) {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(description, text);
        Objects.requireNonNull(clipboard).setPrimaryClip(clip);
    }

    public void createShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(this, "id1")
                    .setShortLabel("Donate")
                    .setLongLabel("Donate to help maintain the project")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_credit_card_black_24dp))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_link))))
                    .build();

            Objects.requireNonNull(shortcutManager).setDynamicShortcuts(Collections.singletonList(shortcutInfo));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onOrientationLandscape();
        } else {
            onOrientationPortrait();
        }
    }

    protected abstract void onOrientationPortrait();
    protected abstract void onOrientationLandscape();
}
