package com.delivery.provider.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.delivery.provider.Adapter.DocumentAdapter;
import com.delivery.provider.Bean.Document;
import com.delivery.provider.Helper.AppHelper;
import com.delivery.provider.Helper.ConnectionHelper;
import com.delivery.provider.Helper.CustomDialog;
import com.delivery.provider.Helper.SharedHelper;
import com.delivery.provider.Helper.URLHelper;
import com.delivery.provider.Helper.VolleyMultipartRequest;
import com.delivery.provider.MyApplication;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.delivery.provider.MyApplication.trimMessage;
public class DocumentActivity extends AppCompatActivity implements
        DocumentAdapter.ServiceClickListener {

    public static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 100;
    private RecyclerView rvDocuments;
    private ArrayList<Document> documentArrayList;
    private DocumentAdapter documentAdapter;
    private Document updatedDocument;
    ConnectionHelper helper;
    public Handler ha;
    private boolean isUpdateDocument = false;
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        isUpdateDocument = getIntent().getBooleanExtra("isUpdateDocument", false);
        helper = new ConnectionHelper(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        FloatingActionButton fabNext = findViewById(R.id.fabNext);
        rvDocuments = findViewById(R.id.rvDocuments);
        documentArrayList = new ArrayList<>();
        documentAdapter = new DocumentAdapter(documentArrayList, this);
        documentAdapter.setServiceClickListener(this);
        rvDocuments.setAdapter(documentAdapter);
        fabNext.setOnClickListener(v -> {
            if (documentArrayList.size() > 0) {
                for (int i = 0; i < documentArrayList.size(); i++) {
                    if (documentAdapter.getServiceListModel().get(i).getBitmap() == null &&
                            documentAdapter.getServiceListModel().get(i).getImg() == null) {
                        displayMessage(documentAdapter.getServiceListModel()
                                .get(i).getName() + " required");
                        return;
                    }
                }
                callUploadDocumentWebservice();
            }
        });
        getDocList();
        ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkStatus();
                ha.postDelayed(this, 3000);
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkStatus();
                ha.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void checkStatus() {
        try {
            if (helper.isConnectingToInternet()) {
                String url = URLHelper.base + "api/provider/trip";
                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                    Log.e("CheckStatus", "" + response.toString());
                    if (!isUpdateDocument && !response.optString("account_status").equals("document")) {
                        ha.removeMessages(0);
                        Intent mainIntent = new Intent(DocumentActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                        finish();
                    }
                }, error -> Utilities.print("Error", error.toString())) {
                    @Override
                    public java.util.Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("X-Requested-With", "XMLHttpRequest");
                        headers.put("Authorization", "Bearer " +
                                SharedHelper.getKey(DocumentActivity.this, "access_token"));
                        return headers;
                    }
                };
                MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
            } else {
                displayMessage(getString(R.string.oops_connect_your_internet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDocList() {
        if (helper.isConnectingToInternet()) {
            final CustomDialog customDialog = new CustomDialog(this);
            customDialog.setCancelable(false);
            customDialog.show();
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    URLHelper.GET_DOC, new JSONObject(), result -> {
                customDialog.dismiss();
                JSONArray response = result.optJSONArray("document");
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject doc = response.optJSONObject(i);
                        Document document = new Document();
                        document.setId(doc.optString("id"));
                        document.setName(doc.optString("name"));
                        document.setType(doc.optString("type"));
                        JSONObject docObj = doc.optJSONObject("providerdocuments");
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
                        documentAdapter = new DocumentAdapter(documentArrayList, this);
                        documentAdapter.setServiceClickListener(this);
                        rvDocuments.setAdapter(documentAdapter);
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
                        if (response.statusCode == 400 || response.statusCode == 405 ||
                                response.statusCode == 500) {
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
                    headers.put("Authorization", "Bearer " +
                            SharedHelper.getKey(DocumentActivity.this, "access_token"));
                    return headers;
                }
            };
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    @Override
    public void onDocImgClick(Document document, int position) {
        this.position = position;
        updatedDocument = document;
        if (hasPermission(Manifest.permission.CAMERA) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            pickImage();
        } else {
            requestPermissionsSafely(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onDocDateClick(Document document, int pos) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    public void pickImage() {
        EasyImage.openCameraForImage(this, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ASK_MULTIPLE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean permission1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean permission2 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permission1 && permission2) {
                    pickImage();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.please_give_permissions, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, DocumentActivity.this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                try {
                    File file = new Compressor(DocumentActivity.this).compressToFile(imageFiles.get(0));
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    if (bitmap != null) {
                        updatedDocument.setBitmap(bitmap);
                        documentArrayList.get(position).setBitmap(bitmap);
                        documentAdapter.setList(documentArrayList);
                        documentAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });
    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(this, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private void callUploadDocumentWebservice() {
        if (helper.isConnectingToInternet()) {
            CustomDialog customDialog = new CustomDialog(this);
            customDialog.setCancelable(false);
            customDialog.show();
            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST, URLHelper.upload_documents, response -> {
                if (customDialog.isShowing()) {
                    customDialog.dismiss();
                    Toast.makeText(this, "Document successfully uploaded.", Toast.LENGTH_SHORT).show();
                }
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
                        callUploadDocumentWebservice();
                    }
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> object = new HashMap<>();
                    try {
                        for (Document document : documentAdapter.getServiceListModel()) {
                            if (document.getBitmap() != null) {
                                object.put("id[" + document.getId() + "]", document.getId());
                            }
                        }
                        Utilities.print("InputToDocumentAPI", "" + object);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return object;
                }

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " +
                            SharedHelper.getKey(DocumentActivity.this, "access_token"));
                    return headers;
                }

                @Override
                protected Map<String, VolleyMultipartRequest.DataPart> getByteData() {
                    Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
                    List<Document> serviceListModel = documentAdapter.getServiceListModel();
                    for (int i = 0; i < serviceListModel.size(); i++) {
                        Document document = serviceListModel.get(i);
                        if (document.getBitmap() != null) {
                            params.put("document[" + serviceListModel.get(i).getId() + "]",
                                    new DataPart("doc[" + serviceListModel.get(i).getId() + "].jpg",
                                            AppHelper.getFileDataFromDrawable(document.getBitmap()), "image/jpeg"));
                        }
                    }
                    return params;
                }
            };
            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(100000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MyApplication.getInstance().addToRequestQueue(volleyMultipartRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(this, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(this, ActivityEmail.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isUpdateDocument) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.logout));
            builder.setMessage(getString(R.string.exit_confirm));
            builder.setPositiveButton(R.string.yes, (dialog, which) -> logout());
            builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
            builder.setCancelable(false);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(arg -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            });
            dialog.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ha != null) {
            ha.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroy() {
        ha.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void logout() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", SharedHelper.getKey(this, "id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                URLHelper.LOGOUT, object, response -> {
            SharedHelper.putKey(this, "current_status", "");
            SharedHelper.putKey(this, "loggedIn", getString(R.string.False));
            SharedHelper.putKey(this, "email", "");
            SharedHelper.clearSharedPreferences(this);
            Intent goToLogin = new Intent(this, SignIn.class);
            goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goToLogin);
            finish();
        }, error -> {
            String json;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {
                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));
                    if (response.statusCode == 400 || response.statusCode == 405 ||
                            response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.getString("message"));
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
                    logout();
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                Log.e("getHeaders: Token", SharedHelper.getKey(
                        DocumentActivity.this, "access_token")
                        + SharedHelper.getKey(DocumentActivity.this, "token_type"));
                headers.put("Authorization", "" + "Bearer" + " " + SharedHelper.getKey(
                        DocumentActivity.this, "access_token"));
                return headers;
            }
        };
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}