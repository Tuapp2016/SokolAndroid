package sokol.sokolandroid.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import sokol.sokolandroid.R;
import sokol.sokolandroid.models.User;
import sokol.sokolandroid.util.Util;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    /* *************************************
     *              GENERAL                *
     ***************************************/

    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;

    /* Data from the authenticated user */
    private FirebaseAuth mAuth;

    /* Listener for Firebase session changes */
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    /* *************************************
     *              FACEBOOK               *
     ***************************************/
    /* The login button for Facebook */
    private LoginButton mFaceLoginButton;
    private Button mFacebookLoginButton;
    private LoginManager mFacebookLoginManager;
    /* The callback manager for Facebook */
    private CallbackManager mFacebookCallbackManager;


    /* *************************************
     *              GOOGLE                 *
     ***************************************/
    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_GOOGLE_LOGIN = 1;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* The login button for Google */
    private SignInButton mGoogleLoginButton;

    /* *************************************
     *              TWITTER                *
     ***************************************/
    public static final int RC_TWITTER_LOGIN = 140;

    private TwitterLoginButton mTwitterLoginButton;

    /* *************************************
     *              PASSWORD               *
     ***************************************/
    private Button mPasswordSignInButton;
    private Button mPasswordSignUpButton;
    private EditText mPasswordLoginEmail;
    private EditText mPasswordLoginPassword;
    private Button mPasswordLoginForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure Twitter SDK
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));
        Fabric.with(this, new Twitter(authConfig));

        /* Load the view and display it */
        setContentView(R.layout.activity_main);

        /* *************************************
         *              FACEBOOK               *
         ***************************************/
        /* Load the Facebook login button and set up the tracker to monitor access token changes */
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFaceLoginButton = (LoginButton) findViewById(R.id.login_with_face);
        mFacebookLoginButton = (Button) findViewById(R.id.login_with_facebook);
        mFacebookLoginButton.setOnClickListener(this);
        mFacebookLoginManager = LoginManager.getInstance();
        //mFaceLoginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
        mFacebookLoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                setAuthenticatedUser(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                setAuthenticatedUser(null);
            }
        });

        /* *************************************
         *               GOOGLE                *
         ***************************************/
        /* Load the Google login button */
        mGoogleLoginButton = (SignInButton) findViewById(R.id.login_with_google);
        mGoogleLoginButton.setOnClickListener(this);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        /* Setup the Google API object to allow Google+ logins */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        /* *************************************
         *                TWITTER              *
         ***************************************/

        mTwitterLoginButton = (TwitterLoginButton) findViewById(R.id.login_with_twitter);
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                setAuthenticatedUser(null);
            }
        });

        /* *************************************
         *               PASSWORD              *
         ***************************************/
        mPasswordSignInButton = (Button) findViewById(R.id.login_sign_in_with_password);
        mPasswordSignInButton.setOnClickListener(this);
        mPasswordSignUpButton = (Button) findViewById(R.id.login_sign_up);
        mPasswordSignUpButton.setOnClickListener(this);
        mPasswordLoginEmail = (EditText) findViewById(R.id.login_email);
        mPasswordLoginPassword = (EditText) findViewById(R.id.login_password);
        mPasswordLoginForgotPassword = (Button) findViewById(R.id.login_forgot_password);
        mPasswordLoginForgotPassword.setOnClickListener(this);

        /* *************************************
         *               GENERAL               *
         ***************************************/

        /* Create the Firebase Auth that is used for all authentication with Firebase */
        mAuth = FirebaseAuth.getInstance();

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.login_loading));
        mAuthProgressDialog.setMessage(getString(R.string.login_authenticating));
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mAuthProgressDialog.hide();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                setAuthenticatedUser(user);
                if(user != null){
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                }else{
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        final String PREFERENCE_LOGOUT = getString(R.string.preference_logout);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean(PREFERENCE_LOGOUT, false)){
            sharedPreferences.edit().remove(PREFERENCE_LOGOUT).commit();
            logout();
        }
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * This method fires when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                handleGoogleAccessToken(account);
            } else {
                // Google Sign In failed, update UI appropriately
                setAuthenticatedUser(null);
            }
        } else if (requestCode == RC_TWITTER_LOGIN) {
            mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
        } else {
            /* Otherwise, it's probably the request by the Facebook login button, keep track of the session */
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* If a user is currently authenticated, display a logout menu */
        if (this.mAuth.getCurrentUser() != null) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Unauthenticate from Firebase and from providers where necessary.
     */
    private void logout() {
        if (this.mAuth.getCurrentUser() != null) {
            FirebaseUser user = this.mAuth.getCurrentUser();
            /* logout of Firebase */
            mAuth.signOut();
            /* Logout of any of the Frameworks. This step is optional, but ensures the user is not logged into
             * Facebook/Google+ after logging out of Firebase. */
            String provider = Util.getProvider(user);
            if (provider.equals(getString(R.string.provider_facebook))) {
                /* Logout from Facebook */
                LoginManager.getInstance().logOut();
            } else if (provider.equals(getString(R.string.provider_google))) {
                if(mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    setAuthenticatedUser(null);
                                }
                            });
                    Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    setAuthenticatedUser(null);
                                }
                            });
                }
            }else if(provider.equals(getString(R.string.provider_twitter)))
                Twitter.logOut();
            /* Update authenticated user and show login buttons */
            setAuthenticatedUser(null);
        }
    }

    /**
     * Once a user is logged in, take the mAuth provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            /* show a provider specific status text */
            UserInfo userInfo = firebaseUser.getProviderData().get(1);
            String provider = userInfo.getProviderId();
            if (provider.equals(getString(R.string.provider_facebook))
                    || provider.equals(getString(R.string.provider_google))
                    || provider.equals(getString(R.string.provider_twitter))) {
                String uid = firebaseUser.getUid();
                String name = userInfo.getDisplayName().toString();
                String email = userInfo.getEmail() != null ? firebaseUser.getEmail() : "";
                Uri profileURL = userInfo.getPhotoUrl();
                User user = new User(uid, name, email, profileURL.toString(), provider);
                createUserDB(user);
                Toast.makeText(getApplicationContext(), "Logged in " + provider + " as " + name, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Opening base activity");
                Intent intentBaseActivity = new Intent(this, BaseActivity.class);
                startActivity(intentBaseActivity);
            } else if(provider.equals(getString(R.string.provider_sokol))){
                String email = firebaseUser.getEmail();
                Toast.makeText(getApplicationContext(), "Logged in " + provider + " as " + email, Toast.LENGTH_SHORT).show();
                Intent intentBaseActivity = new Intent(this, BaseActivity.class);
                startActivity(intentBaseActivity);
            } else {
                Log.e(TAG, "Invalid provider: " + provider);
            }
        }
        /* invalidate options menu to hide/show the logout button */
        supportInvalidateOptionsMenu();
    }

    /**
     * Show successful to users
     */
    private void showSuccesfulDialog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Show errors to users
     */
    private void showErrorDialog(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    /* ************************************
     *             GENERAL                *
     **************************************
     */
    private void handleFirebasekAccessToken(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        mAuthProgressDialog.hide();
                    }
                });
    }

    /* ************************************
     *             FACEBOOK               *
     **************************************
     */
    public void loginWithFacebook(){
        mFacebookLoginManager.logOut();
        //mFaceLoginButton.clearPermissions();
        mFacebookLoginManager.logInWithReadPermissions(this, Arrays.asList("email","user_friends","public_profile"));
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        mAuthProgressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        handleFirebasekAccessToken(credential);
    }

    /* ************************************
     *              GOOGLE                *
     **************************************
     */
    public void loginWithGoogle(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
    }

    private void handleGoogleAccessToken(GoogleSignInAccount acct) {
        Log.d(TAG, "handleGoogleAccessToken:" + acct.getId());
        mAuthProgressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        handleFirebasekAccessToken(credential);
    }

    /* ************************************
     *               TWITTER              *
     **************************************
     */
    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        mAuthProgressDialog.show();

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        handleFirebasekAccessToken(credential);
    }

    /* ************************************
     *              PASSWORD              *
     **************************************
     */
    public void loginWithPassword() {
        String email = mPasswordLoginEmail.getText().toString();
        String password = mPasswordLoginPassword.getText().toString();
        if(!Util.validateEmail(email))
            showErrorDialog(getString(R.string.login_error_email));
        else if(!Util.validatePassword(password))
            showErrorDialog(getString(R.string.login_error_password));
        else {
            mAuthProgressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    String provider = getString(R.string.provider_sokol);
                    if(task.isSuccessful()) {
                        Log.i(TAG, provider + " auth successful");
                        setAuthenticatedUser(mAuth.getCurrentUser());
                    }else{
                        Log.i(TAG, provider + " auth unsuccessful");
                    }
                }
            });
        }
    }

    private void createUserDB(User user){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        final String users = getString(R.string.db_users_users);
        if(databaseReference.child(users).child(user.getUid()) != null)
            return;
        //databaseReference.child(getString(R.string.db_users_users));
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/"+users+"/" + user.getUid(), user.toMap());
        databaseReference.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG, "User created successfully");
                }else{
                    Log.i(TAG, "User not created");
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // ignore
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
    }


    public void loginForgotPassword(){
        String email = mPasswordLoginEmail.getText().toString();
        if (!Util.validateEmail(email)) {
            String message = getString(R.string.login_error_email);
            showErrorDialog(message);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.login_password_reset_title);
            builder.setMessage(getString(R.string.login_password_reset_message, email));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String email = mPasswordLoginEmail.getText().toString();
                    if (!Util.validateEmail(email)) {
                        String message = getString(R.string.login_error_email);
                        showErrorDialog(message);
                    } else
                        mAuth.sendPasswordResetEmail(email);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void signUp()
    {
        Intent intentSignUp = new Intent(MainActivity.this, SignUp.class);
        startActivity(intentSignUp);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_sign_in_with_password:
                loginWithPassword();
                break;
            case R.id.login_forgot_password:
                loginForgotPassword();
                break;
            case R.id.login_sign_up:
                signUp();
                break;
            case R.id.login_with_google:
                loginWithGoogle();
                break;
            case R.id.login_with_facebook:
                loginWithFacebook();
                break;
        }
    }

}
