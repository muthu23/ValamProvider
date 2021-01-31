package com.delivery.provider.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.delivery.provider.Activity.ShowInvoicePicture;
import com.delivery.provider.Bean.Document;
import com.delivery.provider.Helper.URLHelper;
import com.delivery.provider.Listeners.AdapterImageUpdateListener;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.Utilities;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

/**
 * Created by CSS on 27-11-2017.
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder>
        implements AdapterImageUpdateListener {

    private ArrayList<Document> listModels;
    private Context context;
    String TAG = "ServiceListAdapter";
    private int pos;
    private ServiceClickListener serviceClickListener;
    boolean select;
    ViewHolder viewHolder;
    Utilities utils;


    public DocumentAdapter(ArrayList<Document> listModel, Context context) {
        this.listModels = listModel;
        this.context = context;
        utils=new Utilities();
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
        void onDocDateClick(Document document, int pos);
    }

    public List<Document> getServiceListModel() {
        return listModels;
    }

    public void setServiceClickListener(ServiceClickListener serviceClickListener) {
        this.serviceClickListener = serviceClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_list_item, parent, false);
        viewHolder = new ViewHolder(v);
        return new ViewHolder(v);
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView docImg;
        TextView docName, expDate;
        ViewHolder viewsHolder;

        ViewHolder(View itemView) {
            super(itemView);
            docImg = (ImageView) itemView.findViewById(R.id.doc_image);
            docName = (TextView) itemView.findViewById(R.id.doc_name);
            expDate = (TextView) itemView.findViewById(R.id.exp_date);

            docImg.setOnClickListener(this);
            expDate.setOnClickListener(this);
        }

        public void updateImageView(int position, Bitmap bitmap) {
            //docImg.getTag(clickedView.getId());

            docImg.setImageBitmap(bitmap);
            notifyItemChanged(position);
        }

        @Override
        public void onClick(View v) {
            if (v == docImg) {
                pos = getPosition();

                Document document = (Document) v.getTag();
                if (document.getImg() != null && !document.getImg().equalsIgnoreCase("null") && document.getImg().length() > 0) {
                    showDialog(document);
                } else {
                    serviceClickListener.onDocImgClick(document, pos);
                }
            } else if (v == expDate) {
                Document document = (Document) v.getTag();
                if(document.getImg()!=null){
                    serviceClickListener.onDocDateClick(document,pos);
                }else{
                    Toast.makeText(context,
                            context.getResources().getString(R.string.upload_your_img),
                            Toast.LENGTH_SHORT)
                            .show();
                }
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


        if (serviceListModel.getImg() != null && !serviceListModel.getImg().equalsIgnoreCase("null") && serviceListModel.getImg().length() > 0) {
            Log.e(TAG, "onBindViewHolder: " + URLHelper.base + "storage/" + serviceListModel.getImg());
            Picasso.with(context).load(URLHelper.base + "storage/" + serviceListModel.getImg()).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.doc_placeholder).error(R.drawable.doc_placeholder).into(holder.docImg);
        }

        if (serviceListModel.getBitmap() != null)
            holder.docImg.setImageBitmap(serviceListModel.getBitmap());
        holder.expDate.setText("Choose Exp Date");
        if(serviceListModel.getExpdate()!=null &&
                !serviceListModel.getExpdate().equalsIgnoreCase("null") &&
                serviceListModel.getExpdate().length()>1){
            holder.expDate.setText(serviceListModel.getExpdate());
        }

        holder.docImg.setTag(serviceListModel);
        holder.expDate.setTag(serviceListModel);
        holder.expDate.setVisibility(GONE);

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
                Intent intent = new Intent(context, ShowInvoicePicture.class);
                intent.putExtra("image", URLHelper.base + "storage/" + document.getImg());
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
