package com.delivery.provider.Adapter;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.delivery.provider.Bean.Flows;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.MyBoldTextView;
import com.delivery.provider.Utilities.MyTextView;

import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private ArrayList<Flows> listModels;
    private Activity activity;

    public TripAdapter(ArrayList<Flows> listModel, Activity activity) {
        this.listModels = listModel;
        this.activity = activity;
    }

    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
        return new TripAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Flows status = listModels.get(position);
        if (status.getdeliveryAddress() != null) {
            holder.destination_address.setText(status.getdeliveryAddress());
        } else {
            holder.destination_address.setText("");
        }
        if (status.getcomments() != null) {
            holder.comments.setText(status.getcomments());
        } else {
            holder.comments.setText("");
        }
        holder.destination_address.setTag(status);
        holder.trip.setText(activity.getResources().getString(R.string.trip) + (position + 1));
        /*holder.imgGotoPhoto.setVisibility(status.getAfterImage() != null &&
                !status.getAfterImage().equals("") &&
                !status.getAfterImage().equals("null") ? View.VISIBLE : View.GONE);
        holder.imgGotoPhoto.setOnClickListener(v -> {
            if (status.getAfterImage() != null &&
                    !status.getAfterImage().equals(""))
                openPhoto(status.getAfterImage());
        });*/
    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        MyBoldTextView trip;
        MyTextView destination_address, comments;
//        ImageView imgGotoPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            destination_address = itemView.findViewById(R.id.destination_address);
            comments = itemView.findViewById(R.id.comments);
//            imgGotoPhoto = itemView.findViewById(R.id.imgGotoPhoto);
            trip = itemView.findViewById(R.id.trip);
        }
    }

    /*private void openPhoto(String url) {
        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_open_photo, null);
        dialogBuilder.setView(dialogView);
        final ImageView ivPhoto = dialogView.findViewById(R.id.ivPhoto);
        final TextView tvClose = dialogView.findViewById(R.id.tvClose);
        android.support.v7.app.AlertDialog alertDialog = dialogBuilder.create();
        Glide.with(activity).load(url)
                .apply(new RequestOptions().placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)).into(ivPhoto);
        tvClose.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }*/
}