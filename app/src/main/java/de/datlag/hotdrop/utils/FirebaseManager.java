package de.datlag.hotdrop.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PlayGamesAuthProvider;

import org.jetbrains.annotations.NotNull;

import de.datlag.hotdrop.R;

public class FirebaseManager {

    private Activity activity;
    private FirebaseManager.Callbacks callbacks;
    private static final int RC_SIGN_IN = 520;
    private static final int GAMES_SIGN_IN = 521;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;

    public FirebaseManager(Activity activity, FirebaseManager.Callbacks callbacks) {
        this.activity = activity;
        this.callbacks = callbacks;
        init();
    }

    private void init() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInOptions gsoGames = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestServerAuthCode(activity.getString(R.string.default_web_client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(activity, gso);
        auth = FirebaseAuth.getInstance();
    }

    public void setUser(FirebaseUser user) {
        this.user = user;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public void setAuth(FirebaseAuth auth) {
        this.auth = auth;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public void signOut() {
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(activity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callbacks.onSignOut();
                        }
                        if (user.isAnonymous()) {
                            user.delete();
                        }
                        user = null;
                    }
                });
    }

    public void signIn(int selected) {
        switch (selected) {
            case 0:
                Intent signInIntent = googleSignInClient.getSignInIntent();
                activity.startActivityForResult(signInIntent, RC_SIGN_IN);
                break;

            case 1:
                Intent signInGames = googleSignInClient.getSignInIntent();
                activity.startActivityForResult(signInGames, GAMES_SIGN_IN);
                break;

            case 2:
                auth.signInAnonymously()
                        .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    user = auth.getCurrentUser();
                                    callbacks.onAnonymouslyLoginSuccessful();
                                } else {
                                    callbacks.onAnonymouslyLoginFailed();
                                }
                            }
                        });
                break;
        }
    }

    public void onActivityResult(int requestCode, Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        switch (requestCode) {
            case RC_SIGN_IN:
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException ignored) {}
                break;

            case GAMES_SIGN_IN:
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithPlayGames(account);
                } catch (ApiException ignored) {}
                break;
        }
    }

    private void firebaseAuthWithGoogle(@NotNull GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();
                            callbacks.onGoogleLoginSuccessful(acct);
                        } else {
                            callbacks.onGoogleLoginFailed();
                        }
                    }
                });
    }

    private void firebaseAuthWithPlayGames(@NotNull GoogleSignInAccount acct) {
        AuthCredential credential = PlayGamesAuthProvider.getCredential(acct.getServerAuthCode());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();
                            callbacks.onPlayGamesLoginSuccessful(acct);
                        } else {
                            callbacks.onPlayGamesLoginFailed();
                        }
                    }
                });
    }

    public interface Callbacks {

        void onGoogleLoginSuccessful(GoogleSignInAccount account);

        void onGoogleLoginFailed();

        void onPlayGamesLoginSuccessful(GoogleSignInAccount account);

        void onPlayGamesLoginFailed();

        void onAnonymouslyLoginSuccessful();

        void onAnonymouslyLoginFailed();

        void onSignOut();
    }
}
