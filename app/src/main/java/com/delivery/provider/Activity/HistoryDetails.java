package com.delivery.provider.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.delivery.provider.Adapter.TripAdapter;
import com.delivery.provider.Bean.Flows;
import com.delivery.provider.Helper.ConnectionHelper;
import com.delivery.provider.Helper.CustomDialog;
import com.delivery.provider.Helper.SharedHelper;
import com.delivery.provider.Helper.URLHelper;
import com.delivery.provider.Helper.User;
import com.delivery.provider.MyApplication;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.delivery.provider.MyApplication.trimMessage;

public class HistoryDetails extends AppCompatActivity {
    public JSONObject jsonObject;
    Activity activity;
    Context context;
    Boolean isInternet;
    ConnectionHelper helper;
    CustomDialog customDialog;
    TextView tripAmount;
    TextView tripDate;
    TextView paymentType;
    TextView tripComments;
    TextView tripProviderName;
    TextView tripSource;
    TextView lblTitle;
    TextView tripId;
    TextView invoice_txt;
    TextView txt04Total;
    TextView txt04AmountToPaid;
    ImageView tripImg, tripProviderImg, paymentTypeImg;
    RatingBar tripProviderRating;
    View viewLayout;
    ImageView backArrow;
    LinearLayout parentLayout, lnrComments, lnrInvoiceSub, lnrInvoice;
    String tag = "";
    Button btnCancelRide, btnClose, btnViewInvoice;
    Utilities utils = new Utilities();
    TextView lblBookingID, lblDistanceCovered, lblTimeTaken, lblBasePrice, lblDistancePrice, lblDiscountPrice, lblTaxPrice;
    LinearLayout lnrBookingID, lnrDistanceTravelled, lnrTimeTaken, lnrBaseFare, lnrDistanceFare, lnrTax;
    RecyclerView tripRv;
    TripAdapter tripAdapter;
    ArrayList<Flows> tripArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);
        findViewByIdAndInitialize();
        try {
            Intent intent = getIntent();
            String post_details = intent.getExtras().getString("post_value");
            tag = intent.getExtras().getString("tag");
            jsonObject = new JSONObject(post_details);
        } catch (Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        //setup recycler view
        tripArrayList = new ArrayList<>();
        tripRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        tripAdapter = new TripAdapter(tripArrayList, activity);
        tripRv.setAdapter(tripAdapter);

        if (jsonObject != null) {

            if (tag.equalsIgnoreCase("past_trips")) {
                btnCancelRide.setVisibility(View.GONE);
                lnrComments.setVisibility(View.VISIBLE);
                getRequestDetails();
                lblTitle.setText("Past Trips");
                btnViewInvoice.setVisibility(View.VISIBLE);
            } else {
                btnCancelRide.setVisibility(View.VISIBLE);
                lnrComments.setVisibility(View.GONE);
                getUpcomingDetails();
                lblTitle.setText("Upcoming Trips");
                btnViewInvoice.setVisibility(View.GONE);
            }
        }
        backArrow.setOnClickListener(view -> onBackPressed());
    }

    public void findViewByIdAndInitialize() {
        activity = HistoryDetails.this;
        context = HistoryDetails.this;
        helper = new ConnectionHelper(activity);
        isInternet = helper.isConnectingToInternet();
        parentLayout = findViewById(R.id.parentLayout);
        parentLayout.setVisibility(View.GONE);
        tripAmount = findViewById(R.id.tripAmount);
        tripDate = findViewById(R.id.tripDate);
        paymentType = findViewById(R.id.paymentType);
        paymentTypeImg = findViewById(R.id.paymentTypeImg);
        tripProviderImg = findViewById(R.id.tripProviderImg);
        tripRv = findViewById(R.id.trip_rv);
        tripImg = findViewById(R.id.tripImg);
        tripComments = findViewById(R.id.tripComments);
        tripProviderName = findViewById(R.id.tripProviderName);
        tripProviderRating = findViewById(R.id.tripProviderRating);
        tripSource = findViewById(R.id.tripSource);
        invoice_txt = findViewById(R.id.invoice_txt);
        txt04Total = findViewById(R.id.txt04Total);
        txt04AmountToPaid = findViewById(R.id.txt04AmountToPaid);
        lblTitle = findViewById(R.id.lblTitle);
        tripId = findViewById(R.id.trip_id);
        viewLayout = findViewById(R.id.ViewLayout);
        btnCancelRide = findViewById(R.id.btnCancelRide);
        btnClose = findViewById(R.id.btnClose);
        btnViewInvoice = findViewById(R.id.btnViewInvoice);
        lnrComments = findViewById(R.id.lnrComments);
        lnrInvoice = findViewById(R.id.lnrInvoice);
        lnrInvoiceSub = findViewById(R.id.lnrInvoiceSub);
        backArrow = findViewById(R.id.backArrow);

        lnrBookingID = findViewById(R.id.lnrBookingID);
        lnrDistanceTravelled = findViewById(R.id.lnrDistanceTravelled);
        lnrTimeTaken = findViewById(R.id.lnrTimeTaken);
        lnrBaseFare = findViewById(R.id.lnrBaseFare);
        lnrDistanceFare = findViewById(R.id.lnrDistanceFare);
        lnrTax = findViewById(R.id.lnrTax);

        lblBookingID = findViewById(R.id.lblBookingID);
        lblDistanceCovered = findViewById(R.id.lblDistanceCovered);
        lblTimeTaken = findViewById(R.id.lblTimeTaken);
        lblBasePrice = findViewById(R.id.lblBasePrice);
        lblTaxPrice = findViewById(R.id.lblTaxPrice);
        lblDiscountPrice = findViewById(R.id.lblDiscountPrice);
        lblDistancePrice = findViewById(R.id.lblDistancePrice);

        LayerDrawable drawable = (LayerDrawable) tripProviderRating.getProgressDrawable();
        drawable.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);

        btnClose.setOnClickListener(view -> lnrInvoice.setVisibility(View.GONE));

        btnViewInvoice.setOnClickListener(v -> lnrInvoice.setVisibility(View.VISIBLE));

        lnrInvoice.setOnClickListener(view -> lnrInvoice.setVisibility(View.GONE));

        lnrInvoiceSub.setOnClickListener(view -> {

        });

        btnCancelRide.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(getString(R.string.cencel_request))
                    .setCancelable(false)
                    .setPositiveButton("YES", (dialog, id) -> {
                        dialog.dismiss();
                        cancelRequest();
                    })
                    .setNegativeButton("NO", (dialog, id) -> dialog.dismiss());
            AlertDialog alert = builder.create();
            alert.show();
        });
    }


    private void setDetails(JSONArray response) {
        if (response != null && response.length() > 0) {
            Glide.with(activity).load(response.optJSONObject(0).optString("static_map")).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(tripImg);
            if (!response.optJSONObject(0).optString("payment").equalsIgnoreCase("null")) {
                Log.e("History Details", "onResponse: Currency" + SharedHelper.getKey(context, "currency"));
                //tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optString("total"));
            } else {
                //tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + "0");
            }
            String form;
            if (tag.equalsIgnoreCase("past_trips")) {
                form = response.optJSONObject(0).optString("assigned_at");
            } else {
                form = response.optJSONObject(0).optString("schedule_at");
            }
            try {
                tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + "\n" + getTime(form));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                JSONArray userdrop = response.optJSONObject(0).getJSONArray("userdrop");
                if (userdrop != null) {
                    tripArrayList.clear();
                    for (int i = 0; i < userdrop.length(); i++) {
                        Flows flows = new Flows();
                        flows.setdeliveryAddress(userdrop.getJSONObject(i).optString("d_address"));
                        flows.setcomments(userdrop.getJSONObject(i).optString("service_items"));
                        tripArrayList.add(flows);
                    }
                    tripAdapter.notifyDataSetChanged();


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tripId.setText(response.optJSONObject(0).optString("booking_id"));
            paymentType.setText(response.optJSONObject(0).optString("payment_mode"));
            if (response.optJSONObject(0).optString("payment_mode").equalsIgnoreCase("CASH")) {
                paymentTypeImg.setImageResource(R.drawable.money1);
            } else {
                paymentTypeImg.setImageResource(R.drawable.visa_icon);
            }
            if (response.optJSONObject(0).optJSONObject("user") != null) {
                if (response.optJSONObject(0).optJSONObject("user").optString("picture").startsWith("http"))
                    Picasso.with(activity).load(response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
                else
                    Picasso.with(activity).load(URLHelper.base + "storage/" + response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
            }
            final JSONArray res = response;
            tripProviderImg.setOnClickListener(v -> {
                if (res.optJSONObject(0).optJSONObject("user") != null) {
                    JSONObject jsonObject = res.optJSONObject(0).optJSONObject("user");
                }
                User user = new User();
                user.setFirstName(jsonObject.optString("first_name"));
                user.setLastName(jsonObject.optString("last_name"));
                user.setEmail(jsonObject.optString("email"));
                if (jsonObject.optString("picture").startsWith("http"))
                    user.setImg(jsonObject.optString("picture"));
                else
                    user.setImg(URLHelper.base + "storage/" + jsonObject.optString("picture"));
                user.setRating(jsonObject.optString("rating"));
                user.setMobile(jsonObject.optString("mobile"));
                Intent intent = new Intent(context, ShowProfile.class);
                intent.putExtra("user", user);
                startActivity(intent);
            });
            if (response.optJSONObject(0).optJSONObject("user") != null) {
                if (response.optJSONObject(0).optJSONObject("user").optString("rating") != null &&
                        !response.optJSONObject(0).optJSONObject("user").optString("rating").equalsIgnoreCase(""))
                    tripProviderRating.setRating(Float.parseFloat(response.optJSONObject(0).optJSONObject("user").optString("rating")));
                else {
                    tripProviderRating.setRating(0);
                }
            }
            /*if (!response.optJSONObject(0).optString("rating").equalsIgnoreCase("null") &&
                    !response.optJSONObject(0).optJSONObject("rating").optString("user_comment").equalsIgnoreCase("")) {
                tripComments.setText(response.optJSONObject(0).optJSONObject("rating").optString("user_comment"));
            } else {
                tripComments.setText(getString(R.string.no_comments));
            }*/
            if (response.optJSONObject(0).optJSONObject("user") != null) {
                tripProviderName.setText(response.optJSONObject(0).optJSONObject("user").optString("first_name") + " " + response.optJSONObject(0).optJSONObject("user").optString("last_name"));
                if (response.optJSONObject(0).optString("s_address") == null || response.optJSONObject(0).optString("d_address") == null || response.optJSONObject(0).optString("d_address").equals("") || response.optJSONObject(0).optString("s_address").equals("")) {
                    viewLayout.setVisibility(View.GONE);
                } else {
                    tripSource.setText(response.optJSONObject(0).optString("s_address"));
                }
            }
            parentLayout.setVisibility(View.VISIBLE);
        }
    }

    public void getRequestDetails() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.GET_HISTORY_DETAILS_API + "?request_id=" + jsonObject.optString("id"), response -> {

            utils.print("Get Trip details", response.toString());
            if (response != null && response.length() > 0) {
                Glide.with(activity).load(response.optJSONObject(0).optString("static_map")).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(tripImg);
                if (!response.optJSONObject(0).optString("payment").equalsIgnoreCase("null")) {
                    Log.e("History Details", "onResponse: Currency" + SharedHelper.getKey(context, "currency"));
                    tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optString("total"));
                } else {
                    tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + "0");
                }

                lblBasePrice.setText(SharedHelper.getKey(context, "currency") + response.optJSONObject(0).optJSONObject("payment").optInt("fixed"));
                lblDistancePrice.setText(SharedHelper.getKey(context, "currency") + response.optJSONObject(0).optJSONObject("payment").optInt("distance"));
                lblDiscountPrice.setText(SharedHelper.getKey(context, "currency") + response.optJSONObject(0).optJSONObject("payment").optInt("discount"));
                lblTaxPrice.setText(SharedHelper.getKey(context, "currency") + response.optJSONObject(0).optJSONObject("payment").optInt("tax"));
                lblBookingID.setText("" + response.optJSONObject(0).optString("booking_id"));
                lblDistanceCovered.setText(response.optJSONObject(0).optInt("distance") + " KM");
                if (response.optJSONObject(0).optString("travel_time") != null &&
                        !response.optJSONObject(0).optString("travel_time").equalsIgnoreCase("")) {
                    lblTimeTaken.setText(response.optJSONObject(0).optString("travel_time") + " mins");
                }
                try {
                    JSONArray userdrop = response.optJSONObject(0).getJSONArray("userdrop");
                    if (userdrop != null) {
                        tripArrayList.clear();
                        for (int i = 0; i < userdrop.length(); i++) {
                            Flows flows = new Flows();
                            flows.setdeliveryAddress(userdrop.getJSONObject(i).optString("d_address"));
                            flows.setcomments(userdrop.getJSONObject(i).optString("service_items"));
                            flows.setAfterImage(userdrop.getJSONObject(i).optString("after_image"));
                            tripArrayList.add(flows);
                        }
                        tripAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                txt04Total.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("total"));
                txt04AmountToPaid.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("payable"));
                String form;
                if (tag.equalsIgnoreCase("past_trips")) {
                    form = response.optJSONObject(0).optString("assigned_at");
                } else {
                    form = response.optJSONObject(0).optString("schedule_at");
                }
                try {
                    tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + "\n" + getTime(form));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                paymentType.setText(response.optJSONObject(0).optString("payment_mode"));
                if (response.optJSONObject(0).optString("payment_mode").equalsIgnoreCase("CASH")) {
                    paymentTypeImg.setImageResource(R.drawable.money1);
                } else {
                    paymentTypeImg.setImageResource(R.drawable.visa_icon);
                }
                if (response.optJSONObject(0).optJSONObject("user") != null) {
                    if (response.optJSONObject(0).optJSONObject("user").optString("picture").startsWith("http"))
                        Picasso.with(activity).load(response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
                    else
                        Picasso.with(activity).load(URLHelper.base + "storage/" + response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);

                }
                final JSONArray res = response;
                tripProviderImg.setOnClickListener(v -> {
                    JSONObject jsonObject = res.optJSONObject(0).optJSONObject("user");

                    User user = new User();
                    user.setFirstName(jsonObject.optString("first_name"));
                    user.setLastName(jsonObject.optString("last_name"));
                    user.setEmail(jsonObject.optString("email"));
                    if (jsonObject.optString("picture").startsWith("http"))
                        user.setImg(jsonObject.optString("picture"));
                    else
                        user.setImg(URLHelper.base + "storage/" + jsonObject.optString("picture"));
                    user.setRating(jsonObject.optString("rating"));
                    user.setMobile(jsonObject.optString("mobile"));
                    Intent intent = new Intent(context, ShowProfile.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                });

                tripId.setText(response.optJSONObject(0).optString("booking_id"));
                if (response.optJSONObject(0).optJSONObject("rating") != null) {
                    if (response.optJSONObject(0).optJSONObject("rating").optString("user_rating") != null &&
                            !response.optJSONObject(0).optJSONObject("rating").optString("user_rating").equalsIgnoreCase(""))
                        tripProviderRating.setRating(Float.parseFloat(response.optJSONObject(0).optJSONObject("rating").optString("user_rating")));
                    else {
                        tripProviderRating.setRating(0);
                    }

                }

                if (!response.optJSONObject(0).optString("rating").equalsIgnoreCase("null") &&
                        !response.optJSONObject(0).optJSONObject("rating").optString("user_comment").equalsIgnoreCase("")) {
                    tripComments.setText(response.optJSONObject(0).optJSONObject("rating").optString("user_comment"));
                } else {
                    tripComments.setText(getString(R.string.no_comments));
                }
                if (response.optJSONObject(0).optJSONObject("user") != null) {
                    tripProviderName.setText(response.optJSONObject(0).optJSONObject("user").optString("first_name") + " " + response.optJSONObject(0).optJSONObject("user").optString("last_name"));
                    if (response.optJSONObject(0).optString("s_address") == null || response.optJSONObject(0).optString("d_address") == null || response.optJSONObject(0).optString("d_address").equals("") || response.optJSONObject(0).optString("s_address").equals("")) {
                        viewLayout.setVisibility(View.GONE);
                    } else {
                        tripSource.setText(response.optJSONObject(0).optString("s_address"));
                    }
                }
                parentLayout.setVisibility(View.VISIBLE);
            }
            customDialog.dismiss();

        }, error -> {
            customDialog.dismiss();
            String json = null;
            String Message;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {

                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));

                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.optString("message"));
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                            e.printStackTrace();
                        }

                    } else if (response.statusCode == 401) {
                        GoToBeginActivity();
                    } else if (response.statusCode == 422) {

                        json = trimMessage(new String(response.data));
                        if (json != "" && json != null) {
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
                    e.printStackTrace();
                }

            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    getRequestDetails();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                utils.print("Token", "" + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    public void cancelRequest() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("id", jsonObject.optString("id"));
            utils.print("", "request_id" + jsonObject.optString("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CANCEL_REQUEST_API, object, response -> {
            utils.print("CancelRequestResponse", response.toString());
            customDialog.dismiss();
            finish();
        }, error -> {
            customDialog.dismiss();
            String json = null;
            String Message;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {

                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));

                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.optString("message"));
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                            e.printStackTrace();
                        }
                    } else if (response.statusCode == 401) {
                        GoToBeginActivity();
                    } else if (response.statusCode == 422) {

                        json = trimMessage(new String(response.data));
                        if (json != "" && json != null) {
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
                    e.printStackTrace();
                }

            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    cancelRequest();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                utils.print("", "Access_Token" + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }


    public void displayMessage(String toastString) {
        Snackbar.make(findViewById(R.id.parentLayout), toastString, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void getUpcomingDetails() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.UPCOMING_TRIP_DETAILS + "?request_id=" + jsonObject.optString("id"), response -> {
            setDetails(response);
            utils.print("Get Upcoming Details", response.toString());
            customDialog.dismiss();
            parentLayout.setVisibility(View.VISIBLE);

        }, error -> {
            customDialog.dismiss();
            String json = null;
            String Message;
            NetworkResponse response = error.networkResponse;
            if (response != null && response.data != null) {

                try {
                    JSONObject errorObj = new JSONObject(new String(response.data));

                    if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                        try {
                            displayMessage(errorObj.optString("message"));
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                            e.printStackTrace();
                        }

                    } else if (response.statusCode == 401) {
                        GoToBeginActivity();
                    } else if (response.statusCode == 422) {

                        json = trimMessage(new String(response.data));
                        if (json != "" && json != null) {
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
                    e.printStackTrace();
                }

            } else {
                if (error instanceof NoConnectionError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof NetworkError) {
                    displayMessage(getString(R.string.oops_connect_your_internet));
                } else if (error instanceof TimeoutError) {
                    getUpcomingDetails();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        MyApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    private String getMonth(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String monthName = new SimpleDateFormat("MMM").format(cal.getTime());
        return monthName;
    }

    private String getDate(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String dateName = new SimpleDateFormat("dd").format(cal.getTime());
        return dateName;
    }

    private String getYear(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String yearName = new SimpleDateFormat("yyyy").format(cal.getTime());
        return yearName;
    }

    private String getTime(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String timeName = new SimpleDateFormat("hh:mm a").format(cal.getTime());
        return timeName;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
