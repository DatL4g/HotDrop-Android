package de.datlag.hotdrop.auth;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.interaapps.firebasemanager.auth.EmailAuth;

public class InteraAuth {

    private AdvancedActivity activity;

    public InteraAuth(AdvancedActivity activity) {
        this.activity = activity;
    }

    public void getUserInfo(@NotNull FirebaseUser user, @NotNull UserInfoCallback userInfoCallback) {
        final String mURL = activity.getString(R.string.intera_user_url) + user.getEmail();
        RequestQueue queue = Volley.newRequestQueue(activity);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, mURL,
                userInfoCallback::onUserInfoSuccess, userInfoCallback::onUserInfoFailed);
        queue.add(stringRequest);
    }

    public void startLogin(EmailAuth auth, LoginCallback loginCallback) {
        WebView webView = new WebView(activity);
        clearCookies(activity);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(activity.getString(R.string.intera_auth_url));
        webView.setFocusable(true);
        AlertDialog alertDialog = activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                .setTitle(null)
                .setView(webView)
                .create());
        alertDialog.show();

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains(activity.getString(R.string.intera_request_url))) {
                    String[] authToken = url.replace(activity.getString(R.string.intera_request_url), "").split("&");
                    if (authToken[0] != null && authToken[1] != null) {
                        String mail = null;
                        String password = authToken[1].substring(authToken[1].indexOf(activity.getString(R.string.intera_split_password)));
                        try {
                            mail = URLDecoder.decode(authToken[0].split(activity.getString(R.string.intera_split_mail))[1], StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return;
                        }

                        String finalMail = mail;
                        auth.createAccount(mail, password, new EmailAuth.CreateEmailCallbacks() {
                            @Override
                            public void onEmailCreatedSuccessful(AuthResult authResult) {
                                loginCallback.onLoginSuccess(authResult);
                            }

                            @Override
                            public void onEmailCreatedFailed(Exception exception) {
                                if (exception instanceof FirebaseAuthUserCollisionException) {
                                    auth.loginAccount(finalMail, password, new EmailAuth.LoginEmailCallbacks() {
                                        @Override
                                        public void onEmailLoginSuccessful(AuthResult authResult) {
                                            loginCallback.onLoginSuccess(authResult);
                                        }

                                        @Override
                                        public void onEmailLoginFailed(Exception exception) {
                                            loginCallback.onLoginFailed(exception);
                                        }
                                    });
                                } else {
                                    loginCallback.onLoginFailed(exception);
                                }
                            }
                        });
                        alertDialog.cancel();
                    }
                }
            }
        });

        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        webView.requestFocus();
    }

    private static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    public interface LoginCallback {
        void onLoginSuccess(AuthResult authResult);

        void onLoginFailed(Exception exception);
    }

    public interface UserInfoCallback {
        void onUserInfoSuccess(String response);

        void onUserInfoFailed(Exception exception);
    }
}
