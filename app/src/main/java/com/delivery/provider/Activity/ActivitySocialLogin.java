package com.delivery.provider.Activity;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.delivery.provider.Helper.ConnectionHelper;
import com.delivery.provider.Helper.CustomDialog;
import com.delivery.provider.Helper.SharedHelper;
import com.delivery.provider.Helper.URLHelper;
import com.delivery.provider.MyApplication;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.delivery.provider.MyApplication.trimMessage;

public class ActivitySocialLogin extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int REQ_SIGN_IN_REQUIRED = 100;
    private static final int RC_SIGN_IN = 100;
    public static int APP_REQUEST_CODE = 99;
    public Context context = ActivitySocialLogin.this;
    /*----------Facebook Login---------------*/
    CallbackManager callbackManager;
    ImageView backArrow;
    AccessTokenTracker accessTokenTracker;
    JSONObject json;
    ConnectionHelper helper;
    Boolean isInternet;
    LinearLayout facebook_layout;
    LinearLayout google_layout;
    CustomDialog customDialog;
    String TAG = "ActivitySocialLogin";
    String device_token, device_UDID;
    /*----------Google Login---------------*/
    GoogleApiClient mGoogleApiClient;
    String accessToken = "";
    String loginBy = "";
    String mobileNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_social);
        helper = new ConnectionHelper(ActivitySocialLogin.this);
        isInternet = helper.isConnectingToInternet();
        GetToken();
        facebook_layout = findViewById(R.id.facebook_layout);
        google_layout = findViewById(R.id.google_layout);
        backArrow = findViewById(R.id.backArrow);
        /*----------Google Login---------------*/
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //taken from google api console (Web api client id)
                .requestIdToken(getString(R.string.google_login_key))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        google_layout.setOnClickListener(v -> signIn());
        backArrow.setOnClickListener(view -> onBackPressed());
        /*----------Facebook Login---------------*/
        callbackManager = CallbackManager.Factory.create();
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    Build.ID,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        facebook_layout.setOnClickListener(v -> {
            if (isInternet) {
                LoginManager.getInstance().logInWithReadPermissions(ActivitySocialLogin.this,
                        Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {

                            public void onSuccess(LoginResult loginResult) {
                                if (AccessToken.getCurrentAccessToken() != null) {
                                    Log.i("loginresult", "" + loginResult.getAccessToken().getToken());
                                    SharedHelper.putKey(ActivitySocialLogin.this, "accessToken", loginResult.getAccessToken().getToken());
                                    accessToken = loginResult.getAccessToken().getToken();
                                    loginBy = "facebook";
//                                    phoneLogin();
                                    login(accessToken, URLHelper.FACEBOOK_LOGIN, "facebook");
                                } else {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                            }

                            @Override
                            public void onCancel() {
                                // App code
                                displayMessage(getString(R.string.fb_cancel));
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                // App code
                                displayMessage(getString(R.string.fb_error));
                            }
                        });
            } else {
                //mProgressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySocialLogin.this);
                builder.setMessage("Check your Internet").setCancelable(false);
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.setPositiveButton("Setting", (dialog, which) -> {
                    Intent NetworkAction = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(NetworkAction);
                });
                builder.show();
            }
        });
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }
            if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
                mobileNumber = data.getStringExtra("mobile");
                if (loginBy.equalsIgnoreCase("facebook")) {
                    login(accessToken, URLHelper.FACEBOOK_LOGIN, "facebook");
                } else {
                    login(accessToken, URLHelper.GOOGLE_LOGIN, "google");
                }
            }
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("Beginscreen", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("Google", "display_name:" + acct.getDisplayName());
            Log.d("Google", "mail:" + acct.getEmail());
            Log.d("Google", "photo:" + acct.getPhotoUrl());
            String accessToken = acct.getIdToken();
            Log.d("Google", "id_tokem:" + accessToken);
//            login(accessToken, URLHelper.GOOGLE_LOGIN, "google");
            new RetrieveTokenTask().execute(acct.getEmail());
        } else {
            displayMessage(getString(R.string.google_login));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void phoneLogin() {
        Intent intent = new Intent(this, PhoneActivity.class);
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    public void login(final String accesstoken, final String URL, final String Loginby) {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();
        final JsonObject json = new JsonObject();
        json.addProperty("device_type", "android");
        json.addProperty("device_token", device_token);
        json.addProperty("accessToken", accesstoken);
        json.addProperty("device_id", device_UDID);
        json.addProperty("login_by", Loginby);
        json.addProperty("mobile", mobileNumber);
        Log.e(TAG, "login: Facebook" + json);
        Ion.with(ActivitySocialLogin.this)
                .load(URL)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback((e, result) -> {
                    // do stuff with the result or error
                    customDialog.dismiss();
                    if (e != null) {
                        if (e instanceof NetworkErrorException) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (e instanceof TimeoutException) {
                            login(accesstoken, URL, Loginby);
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
                        return;
                    }
                    if (result != null) {
                        Log.v(Loginby + "_Response", result.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            String status = jsonObject.optString("status");
                            if (status.equalsIgnoreCase("true")) {
                                SharedHelper.putKey(context, "token_type", jsonObject.optString("token_type"));
                                SharedHelper.putKey(context, "access_token", jsonObject.optString("access_token"));
                                if (Loginby.equalsIgnoreCase("facebook"))
                                    SharedHelper.putKey(ActivitySocialLogin.this, "login_by", "facebook");
                                if (Loginby.equalsIgnoreCase("google"))
                                    SharedHelper.putKey(ActivitySocialLogin.this, "login_by", "google");
                                if (!jsonObject.optString("currency").equalsIgnoreCase("") && jsonObject.optString("currency") != null)
                                    SharedHelper.putKey(context, "currency", jsonObject.optString("currency"));
                                else
                                    SharedHelper.putKey(context, "currency", "R");
                                //phoneLogin();
                                getProfile();
                            } else {
                                JSONObject errorObject = new JSONObject(result.toString());
                                String strMessage = errorObject.optString("message");
                                if (strMessage.equalsIgnoreCase("Mobile not found"))
                                    phoneLogin();
                                else {
                                    displayMessage(strMessage);
                                    GoToBeginActivity();
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        displayMessage(getString(R.string.please_try_again));
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void GetToken() {
        try {
            if (SharedHelper.getKey(context, "device_token") != null
                    && SharedHelper.getKey(context, "device_token").length() > 0
                    && !SharedHelper.getKey(context, "device_token").equalsIgnoreCase("")) {
                device_token = SharedHelper.getKey(context, "device_token");
                Utilities.print(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                Utilities.print(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            Utilities.print(TAG, "Failed to complete token refresh");
        }
        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            Utilities.print(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Utilities.print(TAG, "Failed to complete device UDID");
        }
    }

    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(context, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void displayMessage(String toastString) {
        Utilities.print("displayMessage", "" + toastString);
        Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void getProfile() {
        if (isInternet) {
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, response -> {
                customDialog.dismiss();
                Log.v("GetProfile", response.toString());
                SharedHelper.putKey(context, "id", response.optString("id"));
                SharedHelper.putKey(context, "login_by", "facebook");
                SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                SharedHelper.putKey(context, "email", response.optString("email"));
                if (response.optString("avatar").startsWith("http"))
                    SharedHelper.putKey(context, "picture", response.optString("avatar"));
                else
                    SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));
                SharedHelper.putKey(context, "gender", response.optString("gender"));
                SharedHelper.putKey(context, "mobile", response.optString("mobile"));
                SharedHelper.putKey(context, "refer_code", response.optString("refer_code"));
                SharedHelper.putKey(context, "wallet_balance", response.optString("wallet_balance"));
                SharedHelper.putKey(context, "payment_mode", response.optString("payment_mode"));
                SharedHelper.putKey(context, "currency", response.optString("currency"));
                SharedHelper.putKey(context, "sos", response.optString("sos"));
                SharedHelper.putKey(context, "loggedIn", getString(R.string.True));
                if (response.optJSONObject("service") != null) {
                    try {
                        JSONObject service = response.optJSONObject("service");
                        if (service.optJSONObject("service_type") != null) {
                            JSONObject serviceType = service.optJSONObject("service_type");
                            SharedHelper.putKey(context, "service", serviceType.optString("name"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                GoToMainActivity();
            }, error -> {
                customDialog.dismiss();
                String json;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));
                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 422) {
                            json = trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                    }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getProfile();
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String GoogleaccessToken) {
            super.onPostExecute(GoogleaccessToken);
            Log.e("Token", GoogleaccessToken);
            accessToken = GoogleaccessToken;
            loginBy = "google";
//            phoneLogin();
            login(accessToken, URLHelper.GOOGLE_LOGIN, "google");
        }
    }
}