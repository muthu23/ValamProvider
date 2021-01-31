package com.delivery.provider.Adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.delivery.provider.Activity.ShowInvoicePicture;
import com.delivery.provider.Bean.Document;
import com.delivery.provider.Helper.URLHelper;
import com.delivery.provider.Listeners.AdapterImageUpdateListener;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.Utilities;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by CSS on 27-11-2017.
 */
public class RegisterDocAdapter extends RecyclerView.Adapter<RegisterDocAdapter.ViewHolder> implements AdapterImageUpdateListener {

    private ArrayList<Document> listModels;
    private Context context;
    JSONArray jsonArraylist;
    private RadioButton lastChecked = null;
    BottomSheetBehavior behavior;
    String TAG = "ServiceListAdapter";
    private int pos;
    private ServiceClickListener serviceClickListener;
    Document serviceListModel;
    boolean[] selectedService;
    boolean select;
    ViewHolder viewHolder;
    ViewHolder selectedHolder;
    View clickedView;


    public RegisterDocAdapter(ArrayList<Document> listModel, Context context) {
        this.listModels = listModel;
        this.context = context;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    @Override
    public void onImageSelectedUpdate(Bitmap bitmap, int pos) {
        Log.e("update_img_listener", bitmap.toString() + "  Pos: " + pos);
        //notifyDataSetChanged();
        viewHolder.updateImageView(pos, bitmap);
    }

    public void setList(ArrayList<Document> list) {
        this.listModels = list;
    }

    public interface ServiceClickListener {
        void onDocImgClick(Document document, int pos);
    }

    public List<Document> getServiceListModel() {
        return listModels;
    }

    public void setServiceClickListener(ServiceClickListener serviceClickListener) {
        this.serviceClickListener = serviceClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reg_doc_list_item, parent, false);
        viewHolder = new ViewHolder(v);
        return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView docImg;
        TextView docName, expDate;
        DatePickerDialog datePickerDialog;
        ViewHolder viewsHolder;
        Utilities utils = new Utilities();
        String scheduledDate = "";

        ViewHolder(View itemView) {
            super(itemView);
            docImg = (ImageView) itemView.findViewById(R.id.doc_image);
            docName = (TextView) itemView.findViewById(R.id.doc_name);
            expDate = (TextView) itemView.findViewById(R.id.exp_date);

            docImg.setOnClickListener(this);
            expDate.setOnClickListener(this);
        }

        public void updateImageView(int position, Bitmap bitmap)
        {
            //docImg.getTag(clickedView.getId());

            docImg.setImageBitmap(bitmap);
            notifyItemChanged(position);
        }

        private void showDatePicker(final Document document)
        {
            final Document doc = document;

            // calender class's instance and get current date , month and year from calender
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR); // current year
            int mMonth = c.get(Calendar.MONTH); // current month
            int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
            // date picker dialog
            datePickerDialog = new DatePickerDialog(context,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            // set day of month , month and year value in the edit text
                            String choosedMonth = "";
                            String choosedDate = "";
                            String choosedDateFormat = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                            scheduledDate = choosedDateFormat;

                            if (dayOfMonth < 10) {
                                choosedDate = "0" + dayOfMonth;
                            } else {
                                choosedDate = "" + dayOfMonth;
                            }
                            //afterToday = utils.isAfterToday(year, monthOfYear, dayOfMonth);
                            expDate.setText(choosedDate + " " + choosedMonth + " " + year);
                            //if (doc != null)
                            doc.setExpdate(choosedDate + " " + choosedMonth + " " + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            //datePickerDialog.getDatePicker().setMaxDate((System.currentTimeMillis() - 1000) + (1000 * 60 * 60 * 24 * 7));
            datePickerDialog.show();
        }

        @Override
        public void onClick(View v) {

            int id = v.getId();
            Document document = (Document) v.getTag();

            if (id == R.id.doc_image)
            {
                pos = getPosition();
                clickedView = v;
                //Log.e("select_view_id", getPosition() + " ");
                if (document.getImg() != null && !document.getImg().equalsIgnoreCase("null") && document.getImg().length() > 0) {
                    showDialog(document);
                } else {
                    serviceClickListener.onDocImgClick(document, pos);
                }
            }
            else if ( id == R.id.exp_date )
            {
                //Document document = (Document) v.getTag();
                showDatePicker(document);
            }
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Document serviceListModel = listModels.get(position);

        String name = serviceListModel.getName();
        if (name != null && !name.equalsIgnoreCase("null") && name.length() > 0) {
            holder.docName.setText(name);
        }

        holder.expDate.setText("Choose Exp Date");

        Log.e(TAG, "onBindViewHolder: " + serviceListModel.getImg());
        Log.e(TAG, "onBindViewHolder: bitmap " + serviceListModel.getBitmap());


        if (serviceListModel.getImg() != null && !serviceListModel.getImg().equalsIgnoreCase("null") && serviceListModel.getImg().length() > 0) {
            Log.e(TAG, "onBindViewHolder: " + URLHelper.base + "storage/" + serviceListModel.getImg());
            Picasso.with(context).load(URLHelper.base + "storage/" + serviceListModel.getImg()).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.doc_placeholder).error(R.drawable.doc_placeholder).into(holder.docImg);
        }

        if ( serviceListModel.getBitmap() != null)
            holder.docImg.setImageBitmap(serviceListModel.getBitmap());

        if ( serviceListModel.getExpdate() != null && !serviceListModel.getExpdate().equals("") )
            holder.expDate.setText(serviceListModel.getExpdate());

        holder.docImg.setTag(serviceListModel);
        holder.expDate.setTag(serviceListModel);
    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }

    private void showDialog(final Document document) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = layoutInflater.inflate(R.layout.img_click_dialog, null);
        dialogBuilder.setView(dialogView);
        final TextView viewTxt = (TextView) dialogView.findViewById(R.id.view_txt);
        final TextView updateTxt = (TextView) dialogView.findViewById(R.id.update_txt);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        viewTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent=new Intent(context, ShowInvoicePicture.class);
                intent.putExtra("image",URLHelper.base + "storage/" + document.getImg());
                context.startActivity(intent);
            }
        });

        updateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceClickListener.onDocImgClick(document, pos);
                alertDialog.dismiss();
            }
        });
    }
}
