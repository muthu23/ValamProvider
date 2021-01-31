package com.delivery.provider.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.delivery.provider.Adapter.RegisterDocAdapter;
import com.delivery.provider.Bean.Document;
import com.delivery.provider.Bean.ServiceTypes;
import com.delivery.provider.Helper.AppHelper;
import com.delivery.provider.Helper.ConnectionHelper;
import com.delivery.provider.Helper.CustomDialog;
import com.delivery.provider.Helper.SharedHelper;
import com.delivery.provider.Helper.URLHelper;
import com.delivery.provider.Helper.VolleyMultipartRequest;
import com.delivery.provider.Listeners.AdapterImageUpdateListener;
import com.delivery.provider.MyApplication;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.Utilities;
import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.ion.Ion;
import com.rilixtech.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.delivery.provider.MyApplication.trimMessage;

public class RegisterActivity extends AppCompatActivity implements RegisterDocAdapter.ServiceClickListener {

    public Context context = RegisterActivity.this;
    public Activity activity = RegisterActivity.this;
    String TAG = "RegisterActivity";
    String device_token, device_UDID;
    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email, first_name, last_name, mobile_no, password;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    Boolean fromActivity = false;
    String strViewPager = "";
    Spinner service_type;
    public static final String TAGG = "DocumentActivity";
    public static int APP_REQUEST_CODE = 99;
    CountryCodePicker ccp;
    RecyclerView recyclerView;
    ImageView uploadImg;
    ArrayList<Document> documentArrayList;
    ArrayList<ServiceTypes> serviceTypeArrayList;
    ArrayList<String> serviceList;
    RegisterDocAdapter documentAdapter;
    ArrayAdapter<String> serviceAdapter;
    Boolean isPermissionGivenAlready = false;
    private static final int SELECT_PHOTO = 100;
    public static int deviceHeight;
    public static int deviceWidth;
    Document updatedDocument;
    int position = -1;
    ServiceTypes serviceTypes;
    AdapterImageUpdateListener imageUpdateListener;
    Spinner serviceSpinner;

    private String blockCharacterSet = "~#^|$%&*!()_-*.,@/";

    private InputFilter filter = (source, start, end, dest, dstart, dend) -> {
        if (source != null && blockCharacterSet.contains(("" + source))) {
            return "";
        }
        return null;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        try {
            Intent intent = getIntent();
            if (intent != null) {
//                if (getIntent().getExtras().containsKey("viewpager")) {
//                    strViewPager = getIntent().getExtras().getString("viewpager");
//                }
//                if (getIntent().getExtras().getBoolean("isFromMailActivity")) {
//                    fromActivity = true;
//                } else if (!getIntent().getExtras().getBoolean("isFromMailActivity")) {
//                    fromActivity = false;
//                } else {
//                    fromActivity = false;
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fromActivity = false;
        }
        findViewById();
        GetToken();
        setupRecyclerView();
        setupServiceSpinner();
        getServiceTypes();
        getDocList();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        nextICON.setOnClickListener(view -> {
            Pattern ps = Pattern.compile(".*[0-9].*");
            Matcher firstName = ps.matcher(first_name.getText().toString());
            Matcher lastName = ps.matcher(last_name.getText().toString());
            if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                displayMessage(getString(R.string.email_validation));
            } else if (!Utilities.isValidEmail(email.getText().toString())) {
                displayMessage(getString(R.string.not_valid_email));
            } else if (first_name.getText().toString().equals("") || first_name.getText().toString().equalsIgnoreCase(getString(R.string.first_name))) {
                displayMessage(getString(R.string.first_name_empty));
            } else if (firstName.matches()) {
                displayMessage(getString(R.string.first_name_no_number));
            } else if (last_name.getText().toString().equals("") || last_name.getText().toString().equalsIgnoreCase(getString(R.string.last_name))) {
                displayMessage(getString(R.string.last_name_empty));
            } else if (lastName.matches()) {
                displayMessage(getString(R.string.last_name_no_number));
            } else if (mobile_no.getText().toString().equals("") || mobile_no.getText().toString().equalsIgnoreCase(getString(R.string.mobile_no))) {
                displayMessage(getString(R.string.mobile_number_empty));
            } else if (password.getText().toString().equals("") || password.getText().toString().equalsIgnoreCase(getString(R.string.password_txt))) {
                displayMessage(getString(R.string.password_validation));
            } else if (password.length() < 6) {
                displayMessage(getString(R.string.password_validation1));
            } else {
                if (documentArrayList.size() > 0) {
                    boolean ischeck = true;
                    for (int i = 0; i < documentArrayList.size(); i++) {
                        if (documentAdapter.getServiceListModel().get(i).getBitmap() == null) {
                            ischeck = false;
                            displayMessage(documentAdapter.getServiceListModel().get(i).getName() + "required");
                            return;
                        }
                    }

                    if (ischeck) {
                        if (isInternet) {
                            checkMailAlreadyExit();
                        } else {
                            displayMessage(getString(R.string.something_went_wrong_net));
                        }
                    }
                }
            }
        });
        backArrow.setOnClickListener(view -> {
           /* Intent mainIntent = new Intent(RegisterActivity.this, ActivityPassword.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            RegisterActivity.this.finish();*/
            onBackPressed();
        });
    }

    public void findViewById() {
        email = findViewById(R.id.email);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        mobile_no = findViewById(R.id.mobile_no);
        password = findViewById(R.id.password);
        nextICON = findViewById(R.id.nextIcon);
        backArrow = findViewById(R.id.backArrow);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        email.setText(SharedHelper.getKey(context, "email"));
        first_name.setFilters(new InputFilter[]{filter});
        last_name.setFilters(new InputFilter[]{filter});
        ccp = findViewById(R.id.ccp);
        recyclerView = findViewById(R.id.recyclerView);
        uploadImg = findViewById(R.id.upload_img);
        service_type = findViewById(R.id.service_type);
        serviceList = new ArrayList<>();
        ccp.registerPhoneNumberTextView(mobile_no);
    }

    public void checkMailAlreadyExit() {
        customDialog = new CustomDialog(RegisterActivity.this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("email", email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CHECK_MAIL_ALREADY_REGISTERED, object, response -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            sendOTP();
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                Utilities.print("MyTest", "" + error);
                Utilities.print("MyTestError", "" + error.networkResponse);
                Utilities.print("MyTestError1", "" + response.statusCode);
                try {
                    if (response.statusCode == 422) {
                        json = trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            if (json.startsWith("The email has already been taken")) {
                                displayMessage(getString(R.string.email_exist));
                            } else {
                                displayMessage(getString(R.string.mobile_exist));
                            }
                            //displayMessage(json);
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
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
                    checkMailAlreadyExit();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void sendOTP() {
        customDialog = new CustomDialog(this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("mobile", mobile_no.getText().toString());
            object.put("phoneonly", mobile_no.getText().toString());
            object.put("country_code", ccp.getSelectedCountryCodeWithPlus());
            Utilities.print("InputToOTPAPI", "" + object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.otp, object, response -> {
            if ((customDialog != null) && (customDialog.isShowing()))
                customDialog.dismiss();
            if (response.opt("otp") != null) {
                Intent mainIntent = new Intent(this, OtpActivity.class);
                mainIntent.putExtra("mobile", mobile_no.getText().toString());
                mainIntent.putExtra("country_code", ccp.getSelectedCountryCodeWithPlus());
                mainIntent.putExtra("otp", String.valueOf(response.opt("otp")));
                startActivityForResult(mainIntent, APP_REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            } else {
                displayMessage(String.valueOf(response.opt("status")));
            }
            Utilities.print("OTPResponse", response.toString());
        }, error -> {
            if ((customDialog != null) && (customDialog.isShowing()))
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
                    } else if (response.statusCode == 401) {
                        try {
                            if (!errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                displayMessage(errorObj.optString("message"));
                            }
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }
                    } else if (response.statusCode == 422) {
                        json = trimMessage(new String(response.data));
                        if (json != null && !json.equals("")) {
                            if (json.startsWith("The email has already been taken")) {
                                displayMessage(getString(R.string.email_exist));
                            } else {
                                displayMessage(getString(R.string.mobile_exist));
                            }
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
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
                    sendOTP();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void signIn() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            try {
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                object.put("email", SharedHelper.getKey(RegisterActivity.this, "email"));
                object.put("password", SharedHelper.getKey(RegisterActivity.this, "password"));
                Utilities.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, response -> {
                if (customDialog != null && customDialog.isShowing())
                    customDialog.dismiss();
                Utilities.print("SignUpResponse", response.toString());
                SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                    SharedHelper.putKey(context, "currency", response.optString("currency"));
                else
                    SharedHelper.putKey(context, "currency", "R");
                getProfile();
            }, error -> {
                if (customDialog != null && customDialog.isShowing())
                    customDialog.dismiss();
                String json;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));
                        Utilities.print("ErrorInLoginAPI", "" + errorObj.toString());
                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500 || response.statusCode == 401) {
                            displayMessage(getString(R.string.something_went_wrong));
                        } else if (response.statusCode == 422) {
                            json = trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
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
                        signIn();
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void getProfile() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, response -> {
                if (customDialog != null && customDialog.isShowing())
                    customDialog.dismiss();
                Utilities.print("GetProfile", response.toString());
                SharedHelper.putKey(RegisterActivity.this, "id", response.optString("id"));
                SharedHelper.putKey(RegisterActivity.this, "first_name", response.optString("first_name"));
                SharedHelper.putKey(RegisterActivity.this, "last_name", response.optString("last_name"));
                SharedHelper.putKey(RegisterActivity.this, "email", response.optString("email"));
                if (response.optString("avatar").startsWith("http"))
                    SharedHelper.putKey(context, "picture", response.optString("avatar"));
                else
                    SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));
                SharedHelper.putKey(RegisterActivity.this, "gender", "" + response.optString("gender"));
                SharedHelper.putKey(RegisterActivity.this, "mobile", response.optString("mobile"));
                SharedHelper.putKey(context, "approval_status", response.optString("status"));
                if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                    SharedHelper.putKey(context, "currency", response.optString("currency"));
                else
                    SharedHelper.putKey(context, "currency", "R");
                SharedHelper.putKey(context, "sos", response.optString("sos"));
                SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.True));
                if (response.optJSONObject("service") != null) {
                    JSONObject service = response.optJSONObject("service");
                    JSONObject serviceType = service.optJSONObject("service_type");
                    SharedHelper.putKey(context, "service", serviceType.optString("name"));
                }
                SharedHelper.putKey(RegisterActivity.this, "login_by", "manual");
                GoToMainActivity();
            }, error -> {
                if (customDialog != null && customDialog.isShowing())
                    customDialog.dismiss();
                String json;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    try {
                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            displayMessage(getString(R.string.something_went_wrong));
                        } else if (response.statusCode == 401) {
                            SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {
                            json = trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
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
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(RegisterActivity.this, "access_token"));
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            if (data != null) {
                signupFinal();
            }
        }
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                //bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                Bitmap resizeImg = getBitmapFromUri(this, uri);
                if (resizeImg != null && AppHelper.getPath(this, uri) != null) {
                    Bitmap reRotateImg = AppHelper.modifyOrientation(resizeImg, AppHelper.getPath(this, uri));
                    uploadImg.setImageBitmap(reRotateImg);
                    updatedDocument.setBitmap(reRotateImg);
                    /*imageUpdateListener = (AdapterImageUpdateListener) documentAdapter;
                    imageUpdateListener.onImageSelectedUpdate(reRotateImg, position);*/
                    documentAdapter.setList(documentArrayList);
                    documentAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void signupFinal() {
        if (helper.isConnectingToInternet()) {
            final CustomDialog customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            customDialog.show();
            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URLHelper.register, response -> {
                customDialog.dismiss();
                Utilities.print("SignInResponse", response.toString());
                SharedHelper.putKey(RegisterActivity.this, "email", email.getText().toString());
                SharedHelper.putKey(RegisterActivity.this, "password", password.getText().toString());
                signIn();
            }, error -> {
                if (customDialog.isShowing())
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
                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
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
                        signupFinal();
                    }
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> object = new HashMap<>();
                    try {
                        object.put("device_type", "android");
                        object.put("device_id", device_UDID);
                        object.put("device_token", device_token);
                        object.put("login_by", "manual");
                        object.put("first_name", first_name.getText().toString());
                        object.put("last_name", last_name.getText().toString());
                        object.put("email", email.getText().toString());
                        object.put("password", password.getText().toString());
                        object.put("password_confirmation", password.getText().toString());
                        object.put("mobile", SharedHelper.getKey(context, "mobile"));
                        //Car Details
                        String car_type = service_type.getAdapter().getItem(service_type.getSelectedItemPosition()).toString();
                        Log.e("car_type_selected", car_type);
                        if (!car_type.equalsIgnoreCase("")) {
                            for (ServiceTypes type : serviceTypeArrayList) {
                                if (type.getName().equals(car_type)) {
                                    object.put("service_type", String.valueOf(type.getId()));
                                }
                            }
                        }
                        Log.e(TAG, "signupFinal: " +
                                documentAdapter.getServiceListModel().toString());
                        for (Document document : documentAdapter.getServiceListModel()) {
                            if (document.getBitmap() != null) {
                                String key = "id[" + document.getId() + "]";
                                object.put(key, document.getId());
                            }
                        }
                        Utilities.print("InputToRegisterAPI", "" + object);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return object;
                }

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    //headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }

                @Override
                protected Map<String, VolleyMultipartRequest.DataPart> getByteData() {
                    Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
                    for (Document document : documentAdapter.getServiceListModel()) {
                        if (document.getBitmap() != null) {
                            String photo = "photos[" + document.getId() + "]";
                            params.put(photo, new VolleyMultipartRequest.DataPart("doc.jpg", AppHelper.getFileDataFromDrawable(document.getBitmap()), "image/jpeg"));
                        }
                    }
                    return params;
                }
            };
            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MyApplication.getInstance().addToRequestQueue(volleyMultipartRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
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

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
    }

    public void displayMessage(String toastString) {
        Utilities.print("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, ActivityEmail.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupRecyclerView() {
        documentArrayList = new ArrayList<>();
        documentAdapter = new RegisterDocAdapter(documentArrayList, context);
        documentAdapter.setServiceClickListener(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        RegisterActivity.ItemOffsetDecoration itemDecoration = new RegisterActivity.ItemOffsetDecoration(context, R.dimen._5sdp);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(documentAdapter);
    }

    private void setupServiceSpinner() {
        try {
            ServiceTypes types = new ServiceTypes();
            /*types.setId(0);
            types.setName("Select Service");*/
            serviceList.add("Select Service");
            serviceAdapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_spinner_item, serviceList);
            serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            service_type.setAdapter(serviceAdapter);
        } catch (Exception setupServiceException) {
            setupServiceException.printStackTrace();
        }
    }

    @Override
    public void onDocImgClick(Document document, int position) {
        updatedDocument = document;
        this.position = position;
        if (checkStoragePermission())
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        else
            goToImageIntent();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGivenAlready) {
                        goToImageIntent();
                    }
                }
            }
        }
    }

    public void goToImageIntent() {
        isPermissionGivenAlready = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);
    }

    private static Bitmap getBitmapFromUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        Log.e(TAGG, "getBitmapFromUri: Resize uri" + uri);
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        Log.e(TAGG, "getBitmapFromUri: Height" + deviceHeight);
        Log.e(TAGG, "getBitmapFromUri: width" + deviceWidth);
        int maxSize = Math.min(deviceHeight, deviceWidth);
        if (image != null) {
            Log.e(TAGG, "getBitmapFromUri: Width" + image.getWidth());
            Log.e(TAGG, "getBitmapFromUri: Height" + image.getHeight());
            int inWidth = image.getWidth();
            int inHeight = image.getHeight();
            int outWidth;
            int outHeight;
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            return Bitmap.createScaledBitmap(image, outWidth, outHeight, false);
        } else {
            Toast.makeText(context, context.getString(R.string.valid_image), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

    private void getServiceTypes() {
        try {
            Ion.with(this)
                    .load(URLHelper.GET_SERVICE_TYPE)
                    .asJsonObject()
                    .setCallback((e, result) -> {
                        // do stuff with the result or error
                        if (result == null) {
                            Toast.makeText(RegisterActivity.this, "Please try again", Toast.LENGTH_LONG).show();
                            //  finish();
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject.optJSONArray("service_type") != null) {
                                Log.e("get_service_types", jsonObject.optJSONArray("service_type").toString());
                                serviceTypeArrayList = new ArrayList<>();
                                //serviceList = new ArrayList<String>();
                                JSONArray serviceTypeArray = jsonObject.optJSONArray("service_type");
                                for (int i = 0; i < serviceTypeArray.length(); i++) {
                                    serviceTypes = new ServiceTypes();
                                    JSONObject serviceObject = serviceTypeArray.getJSONObject(i);
                                    if (serviceObject != null) {
                                        serviceList.add(serviceObject.optString("name"));
                                        serviceTypes.setId(serviceObject.optInt("id"));
                                        serviceTypes.setName(serviceObject.optString("name"));
                                        serviceTypeArrayList.add(serviceTypes);
                                    }
                                }
                                if (serviceList.size() > 0) {
                                    serviceAdapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_spinner_item, serviceList);
                                    serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    service_type.setAdapter(serviceAdapter);
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    });
        } catch (Exception serviceTypeException) {
            serviceTypeException.printStackTrace();
        }
    }

    private void getDocList() {
        if (helper.isConnectingToInternet()) {
            final CustomDialog customDialog = new CustomDialog(activity);
            customDialog.setCancelable(false);
            customDialog.show();
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.GET_DOC, new JSONObject(), result -> {
                customDialog.dismiss();
                JSONArray response = result.optJSONArray("document");
                Log.e(TAG, "onResponse: " + response.toString());
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject doc = response.optJSONObject(i);
                        Document document = new Document();
                        document.setId(doc.optString("id"));
                        document.setName(doc.optString("name"));
                        document.setType(doc.optString("type"));
                        JSONObject docObj = doc.optJSONObject("document");
                        try {
                            if (docObj != null) {
                                document.setImg(docObj.optString("url"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        documentArrayList.add(document);
                    }
                    if (documentArrayList.size() > 0) {
                        documentAdapter = new RegisterDocAdapter(documentArrayList, context);
                        documentAdapter.setServiceClickListener(RegisterActivity.this);
                        recyclerView.setAdapter(documentAdapter);
                    }
                }
            }, error -> {
                customDialog.dismiss();
                String json;
                NetworkResponse response = error.networkResponse;
                Utilities.print("MyTest", "" + error);
                Utilities.print("MyTestError", "" + error.networkResponse);
                if (response != null && response.data != null) {
                    Utilities.print("MyTestError1", "" + response.statusCode);
                    try {
                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            displayMessage(getString(R.string.something_went_wrong));
                        } else if (response.statusCode == 401) {
                            displayMessage(getString(R.string.invalid_credentials));
                        } else if (response.statusCode == 422) {
                            json = trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
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
                        getDocList();
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }
}