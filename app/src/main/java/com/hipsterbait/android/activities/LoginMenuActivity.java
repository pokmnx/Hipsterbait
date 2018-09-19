package com.hipsterbait.android.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.hipsterbait.android.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;

public class LoginMenuActivity extends ImmersiveActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private final int RC_GOOG_SIGN_IN = 6969;
    private final int RC_FB_SIGN_IN = 64206;
    private final int RC_TWIT_SIGN_IN = 140;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private LoginManager mLoginManager;

    private TwitterLoginButton hiddenTwitterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Content View
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_menu);

        // Set widgets
        hiddenTwitterButton = (TwitterLoginButton) findViewById(R.id.login_menu_hidden_twitter_button);

        // Google Sign On
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Facebook Sign On
        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();
        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FB", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FB", "facebook:onCancel");
                Toast.makeText(LoginMenuActivity.this,
                        "FB Login Cancelled",
                        Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FB", "facebook:onError", error);
                Toast.makeText(LoginMenuActivity.this,
                        "FB Login error: " + error.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
                // ...
            }
        });

        // Twitter sign on
        Twitter.initialize(this);

        hiddenTwitterButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TWT", exception.getLocalizedMessage());
                // Do something on failure
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d("HB", "Logged in as " + currentUser.getDisplayName());
            mAuth.signOut();
        }
    }

    public void loginTapped(View loginButton) {
        Intent intent = new Intent(LoginMenuActivity.this, EmailLoginActivity.class);
        startActivity(intent);
    }

    public void facebookButtonTapped(View facebookButton) {
        mLoginManager.logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    public void twitterButtonTapped(View twitterButton) {
        hiddenTwitterButton.performClick();
    }

    public void googleButtonTapped(View googleButton) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOG_SIGN_IN);
    }

    public void createAccountTapped(View createAccountButton) {
        Intent intent = new Intent(LoginMenuActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    public void closeButtonTapped(View v) {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOG_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        if (requestCode == RC_FB_SIGN_IN) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == RC_TWIT_SIGN_IN) {
            hiddenTwitterButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("GSO", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Sign in success
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);

        } else {
            Toast.makeText(LoginMenuActivity.this, "Sign in error ." + result.getStatus().toString(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("FIR", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FIR", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                Log.d("LOG", user.getDisplayName());
                                finish();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIR", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginMenuActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FB", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FB", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                Log.d("LOG", user.getDisplayName());
                                finish();
                            } else {
                                Toast.makeText(LoginMenuActivity.this,
                                        "ERROR: Couldn't get Firebase user",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FB", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginMenuActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d("TWT", "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TWT", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                Log.d("LOG", user.getDisplayName());

                                Intent intent = new Intent(LoginMenuActivity.this, UpdateEmailActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TWT", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginMenuActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e("GSO", result.getErrorMessage());
    }
}
