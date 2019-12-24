package de.datlag.hotdrop.auth

import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import de.datlag.hotdrop.auth.InteraAuth.UserInfoCallback
import de.datlag.hotdrop.extend.AdvancedActivity
import de.interaapps.firebasemanager.auth.AnonymousAuth
import de.interaapps.firebasemanager.auth.EmailAuth
import de.interaapps.firebasemanager.auth.GoogleAuth
import de.interaapps.firebasemanager.auth.GoogleAuth.RevokeAccessCallbacks
import de.interaapps.firebasemanager.auth.OAuth
import de.interaapps.firebasemanager.core.FirebaseManager
import de.interaapps.firebasemanager.core.auth.Auth
import de.interaapps.firebasemanager.core.auth.ProviderEnum

class UserManager(private val activity: AdvancedActivity, private val firebaseManager: FirebaseManager) {
    private var interaAccount: InteraAccount? = null
    private val interaAuth = InteraAuth(activity)
    lateinit var firebaseUser: FirebaseUser

    fun getUserName(userNameCallback: UserNameCallback) {
        var firebaseAuth: FirebaseAuth? = null
        var authResult: AuthResult? = null
        val providerEnum: ProviderEnum

        for (auth in firebaseManager.authManager.login) {
            if (auth.authResult != null) {
                authResult = auth.authResult
            }
            if (auth.auth != null) {
                firebaseAuth = auth.auth
            }
            if (auth.user != null) {
                firebaseUser = auth.user
            }
        }

        providerEnum = authResult?.let {
            firebaseManager.authManager.getProvider(it)
        } ?: firebaseManager.authManager.getProvider(firebaseUser)!!

        if (providerEnum === ProviderEnum.EMAIL) {
            if(interaAccount != null) {
                userNameCallback.onSuccess(interaAccount!!.username!!)
            } else {
                interaAuth.getUserInfo(firebaseUser, object: UserInfoCallback {
                    override fun onUserInfoSuccess(response: String) {
                        interaAccount = InteraAccount(response)
                        interaAccount!!.username?.let { userNameCallback.onSuccess(it) }
                    }

                    override fun onUserInfoFailed(exception: Exception?) {
                        userNameCallback.onFailed(exception)
                    }
                })
            }
        } else if (providerEnum === ProviderEnum.GITHUB) {
            if (authResult != null) {
                userNameCallback.onSuccess(authResult.additionalUserInfo!!.username!!)
            } else {
                firebaseAuth!!.getAccessToken(true).addOnSuccessListener(activity) { getTokenResult: GetTokenResult ->
                    val queue = Volley.newRequestQueue(activity)
                    val stringRequest = StringRequest(Request.Method.GET,
                            "https://github.com/login/oauth/authorize?client_id=$getTokenResult&scope=user",
                            Response.Listener {
                                response: String? ->
                                Log.e("GITHUB", "Scope Success")
                                Log.e("GITHUB", response)
                            }, Response.ErrorListener { error: VolleyError ->
                                Log.e("GITHUB", "Scope failed")
                                Log.e("GITHUB", error.message)
                            })
                    queue.add(stringRequest)
                    userNameCallback.onSuccess("Github Account")
                }.addOnFailureListener(activity) { exception: Exception? -> userNameCallback.onFailed(exception) }
            }
        } else if (providerEnum === ProviderEnum.GOOGLE) {
            userNameCallback.onSuccess(firebaseUser.displayName!!)
        } else {
            userNameCallback.onSuccess("Anonymous")
        }
    }

    fun login(auth: Auth?, loginCallback: LoginCallback) {
        when (auth) {
            is GoogleAuth -> {
                auth.startLogin(object : GoogleAuth.LoginCallbacks {
                    override fun onLoginSuccessful(authResult: AuthResult?) {
                        loginCallback.onLoginSuccess(authResult!!)
                    }

                    override fun onLoginFailed(exception: Exception) {
                        loginCallback.onLoginFailed(exception)
                    }
                })
            }
            is EmailAuth -> {
                interaAuth.startLogin((auth as EmailAuth?)!!, object : InteraAuth.LoginCallback {
                    override fun onLoginSuccess(authResult: AuthResult) {
                        interaAuth.getUserInfo(authResult.user!!, object : UserInfoCallback {
                            override fun onUserInfoSuccess(response: String) {
                                interaAccount = InteraAccount(response)
                                loginCallback.onLoginSuccess(authResult)
                            }

                            override fun onUserInfoFailed(exception: Exception?) {
                                loginCallback.onLoginSuccess(authResult)
                            }
                        })
                    }

                    override fun onLoginFailed(exception: Exception?) {
                        loginCallback.onLoginFailed(exception)
                    }
                })
            }
            is OAuth -> {
                auth.startLogin(object : OAuth.LoginCallbacks {
                    override fun onLoginSuccessful(authResult: AuthResult) {
                        loginCallback.onLoginSuccess(authResult)
                    }

                    override fun onLoginFailed(exception: Exception) {
                        loginCallback.onLoginFailed(exception)
                    }
                })
            }
            is AnonymousAuth -> {
                auth.startLogin(object : AnonymousAuth.LoginCallbacks {
                    override fun onLoginSuccessful(authResult: AuthResult) {
                        loginCallback.onLoginSuccess(authResult)
                    }

                    override fun onLoginFailed(exception: Exception) {
                        loginCallback.onLoginFailed(exception)
                    }
                })
            }
        }
    }

    fun logout(logoutCallback: LogoutCallback) {
        for (auth in firebaseManager.authManager.login) {
                if (auth is GoogleAuth) {
                    auth.signOut(object : GoogleAuth.LogoutCallbacks {
                        override fun onLogoutSuccessful() {
                            auth.signOut()
                            auth.revokeAccess(object : RevokeAccessCallbacks {
                                override fun onRevokeAccessSuccessful(aVoid: Void) {}
                                override fun onRevokeAccessFailed(exception: Exception) {}
                            })
                            logoutCallback.onLogoutSuccess()
                        }

                        override fun onLogoutFailed(exception: Exception) {
                            logoutCallback.onLogoutFailed()
                        }
                    })
                }
        }
        firebaseManager.authManager.massLogout()
        logoutCallback.onLogoutSuccess()
    }

    interface LoginCallback {
        fun onLoginSuccess(authResult: AuthResult)
        fun onLoginFailed(exception: Exception?)
    }

    interface LogoutCallback {
        fun onLogoutSuccess()
        fun onLogoutFailed()
    }

    interface UserNameCallback {
        fun onSuccess(username: String)
        fun onFailed(exception: Exception?)
    }
}