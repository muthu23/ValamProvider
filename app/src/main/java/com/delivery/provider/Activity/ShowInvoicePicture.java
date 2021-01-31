package com.delivery.provider.Activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.delivery.provider.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by CSS on 27-11-2017.
 */

public class ShowInvoicePicture extends AppCompatActivity implements View.OnClickListener {

    ImageView imgZoomService;
    Activity activity;
    ImageView backArrow;
    String image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        setContentView(R.layout.show_invoice_pic);
        findviewById();
        setOnClickListener();

    }

    private void findviewById() {
        imgZoomService = (ImageView) findViewById(R.id.imgZoomService);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        image = getIntent().getExtras().getString("image");
        Picasso.with(activity).load(image).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.doc_placeholder).error(R.drawable.doc_placeholder).into(imgZoomService);
    }

    private void setOnClickListener() {
        backArrow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == backArrow) {
            finish();
        }
    }

}
