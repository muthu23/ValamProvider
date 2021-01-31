package com.delivery.provider.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.delivery.provider.Helper.SharedHelper;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jayakumar on 31/01/17.
 */

public class ActivityEmail extends AppCompatActivity {

    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email;
    TextView register, forgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        email = findViewById(R.id.email);
        nextICON = findViewById(R.id.nextIcon);
        backArrow = findViewById(R.id.backArrow);
        register = findViewById(R.id.register);
        forgetPassword = findViewById(R.id.forgetPassword);
        nextICON.setOnClickListener(view -> {
            if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                displayMessage(getString(R.string.email_validation));
            } else {
                if ((!isValidEmail(email.getText().toString()))) {
                    displayMessage(getString(R.string.email_validation));
                } else {
                    Utilities.hideKeyboard(ActivityEmail.this);
                    SharedHelper.putKey(ActivityEmail.this, "email", email.getText().toString());
                    Intent mainIntent = new Intent(ActivityEmail.this, ActivityPassword.class);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });
        backArrow.setOnClickListener(view -> {
            SharedHelper.putKey(ActivityEmail.this, "email", "");
            Intent mainIntent = new Intent(ActivityEmail.this, SignIn.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            ActivityEmail.this.finish();
        });
        register.setOnClickListener(view -> {
            Utilities.hideKeyboard(ActivityEmail.this);
            SharedHelper.putKey(ActivityEmail.this, "password", "");
            Intent mainIntent = new Intent(ActivityEmail.this, RegisterActivity.class);
            mainIntent.putExtra("isFromMailActivity", true);
            startActivity(mainIntent);
        });
        forgetPassword.setOnClickListener(view -> {
            Utilities.hideKeyboard(ActivityEmail.this);
            SharedHelper.putKey(ActivityEmail.this, "password", "");
            Intent mainIntent = new Intent(ActivityEmail.this, ForgetPassword.class);
            mainIntent.putExtra("isFromMailActivity", true);
            startActivity(mainIntent);
        });
    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        SharedHelper.putKey(ActivityEmail.this, "email", "");
        Intent mainIntent = new Intent(ActivityEmail.this, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        ActivityEmail.this.finish();
    }
}