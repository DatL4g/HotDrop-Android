package de.datlag.hotdrop.auth

import android.annotation.SuppressLint
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.interaapps.firebasemanager.auth.EmailAuth
import de.interaapps.firebasemanager.auth.EmailAuth.CreateEmailCallbacks
import de.interaapps.firebasemanager.auth.EmailAuth.LoginEmailCallbacks
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

class InteraAuth(private val activity: AdvancedActivity) {
    fun getUserInfo(user: FirebaseUser, userInfoCallback: UserInfoCallback) {
        val mURL = activity.getString(R.string.intera_user_url) + user.email
        val queue = Volley.newRequestQueue(activity)
        val stringRequest = StringRequest(Request.Method.GET, mURL, Response.Listener {
            response: String? -> userInfoCallback.onUserInfoSuccess(response!!)
        }, Response.ErrorListener {
            exception: VolleyError? -> userInfoCallback.onUserInfoFailed(exception)
        })
        queue.add(stringRequest)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun startLogin(auth: EmailAuth, loginCallback: LoginCallback) {
        val webView = WebView(activity)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.loadUrl(activity.getString(R.string.intera_auth_url))
        webView.isFocusable = true
        val alertDialog = activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle(null)
                .setView(webView)
                .create())
        alertDialog.show()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (url.contains(activity.getString(R.string.intera_request_url))) {
                    val authToken: Array<String?> = url.replace(activity.getString(R.string.intera_request_url), "").split("&").toTypedArray()
                    if (authToken[0] != null && authToken[1] != null) {
                        val password = authToken[1]!!.substring(authToken[1]!!.indexOf(activity.getString(R.string.intera_split_password)))

                        val mail: String? = try {
                            URLDecoder.decode(authToken[0]!!.split(activity.getString(R.string.intera_split_mail)).toTypedArray()[1], StandardCharsets.UTF_8.name())
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                            return
                        }

                        auth.createAccount(mail, password, object : CreateEmailCallbacks {
                            override fun onEmailCreatedSuccessful(authResult: AuthResult) {
                                loginCallback.onLoginSuccess(authResult)
                            }

                            override fun onEmailCreatedFailed(exception: Exception) {
                                if (exception is FirebaseAuthUserCollisionException) {
                                    auth.loginAccount(mail, password, object : LoginEmailCallbacks {
                                        override fun onEmailLoginSuccessful(authResult: AuthResult) {
                                            loginCallback.onLoginSuccess(authResult)
                                        }

                                        override fun onEmailLoginFailed(exception: Exception) {
                                            loginCallback.onLoginFailed(exception)
                                        }
                                    })
                                } else {
                                    loginCallback.onLoginFailed(exception)
                                }
                            }
                        })
                        alertDialog.cancel()
                    }
                }
            }
        }
        alertDialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        alertDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        webView.requestFocus()
    }

    interface LoginCallback {
        fun onLoginSuccess(authResult: AuthResult)
        fun onLoginFailed(exception: Exception?)
    }

    interface UserInfoCallback {
        fun onUserInfoSuccess(response: String)
        fun onUserInfoFailed(exception: Exception?)
    }

}