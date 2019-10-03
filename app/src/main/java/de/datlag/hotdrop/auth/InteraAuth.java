package de.datlag.hotdrop.auth;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import de.interaapps.firebasemanager.auth.EmailAuth;

public class InteraAuth {

    private Activity activity;

    public InteraAuth(Activity activity) {
        this.activity = activity;
    }

    public void getUserInfo(@NotNull FirebaseUser user, UserInfoCallback userInfoCallback) {
        final String mURL = activity.getString(R.string.intera_user_url) + user.getEmail();
        RequestQueue queue = Volley.newRequestQueue(activity);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, mURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userInfoCallback.onUserInfoSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                userInfoCallback.onUserInfoFailed(error);
            }
        });
        queue.add(stringRequest);
    }

    public void startLogin(EmailAuth auth, LoginCallback loginCallback) {
        WebView webView = new WebView(activity);
        clearCookies(activity);
        webView.loadUrl("https://accounts.interaapps.de/oauth/8");
        webView.setFocusable(true);
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(activity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setTitle(null)
                .setView(webView)
                .create();
        alertDialog.show();

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("iaauth://inteaapps/login")) {
                    String[] authToken = url.replace("iaauth://inteaapps/login?", "").split("&");
                    if (authToken[0] != null && authToken[1] != null) {
                        String mail = null;
                        String password = authToken[1].substring(authToken[1].indexOf("password="));
                        try {
                            mail = URLDecoder.decode(authToken[0].split("mail=")[1], StandardCharsets.UTF_8.name());
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

    private static void clearCookies(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
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
