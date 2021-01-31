package com.delivery.provider.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.delivery.provider.Helper.CustomDialog;
import com.delivery.provider.Helper.SharedHelper;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.LocaleHelper;

public class ActivitySettings extends AppCompatActivity {

    private RadioButton radioEnglish, radioSpanish;

    private LinearLayout lnrEnglish, lnrSpanish;

    private CustomDialog customDialog, customDialogNew;

    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
        init();
    }

    private void init() {

        radioEnglish = (RadioButton) findViewById(R.id.radioEnglish);
        radioSpanish = (RadioButton) findViewById(R.id.radioSpanish);

        lnrEnglish = (LinearLayout) findViewById(R.id.lnrEnglish);
        lnrSpanish = (LinearLayout) findViewById(R.id.lnrSpanish);

        backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        if (SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("en")){
            radioEnglish.setChecked(true);
        }else if (SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("es")){
            radioSpanish.setChecked(true);
        }else{
            radioSpanish.setChecked(true);
        }

        lnrEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioSpanish.setChecked(false);
                radioEnglish.setChecked(true);
            }
        });

        lnrSpanish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEnglish.setChecked(false);
                radioSpanish.setChecked(true);
            }
        });

        radioSpanish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    radioEnglish.setChecked(false);
                    SharedHelper.putKey(ActivitySettings.this, "language", "es");
                    setLanguage();
                    GoToMainActivity();
                }
            }
        });

        radioEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    radioSpanish.setChecked(false);
                    SharedHelper.putKey(ActivitySettings.this, "language", "en");
                    setLanguage();
//                    recreate();
                    GoToMainActivity();
                }
            }
        });
    }

    public void GoToMainActivity(){
        customDialogNew = new CustomDialog(this, getResources().getString(R.string.language_update));
        if (customDialogNew != null)
            customDialogNew.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                customDialogNew.dismiss();
                Intent mainIntent = new Intent(ActivitySettings.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
            }
        }, 3000);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void setLanguage() {
        String languageCode = SharedHelper.getKey(ActivitySettings.this, "language");
        LocaleHelper.setLocale(this, languageCode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

}
