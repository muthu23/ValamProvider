package com.delivery.provider.Helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;

import com.delivery.provider.R;

public class CustomDialog extends ProgressDialog {

    public CustomDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
    }

    public CustomDialog(Context context, String strMessage) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setIndeterminate(true);
        setMessage(strMessage);
    }
}
