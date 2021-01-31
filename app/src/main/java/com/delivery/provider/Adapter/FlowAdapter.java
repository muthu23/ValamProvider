package com.delivery.provider.Adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.delivery.provider.Bean.Flows;
import com.delivery.provider.Fragment.Map;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.MyBoldTextView;

import java.util.ArrayList;

public class FlowAdapter extends RecyclerView.Adapter<FlowAdapter.ViewHolder> {

    private static final String TAG = "FlowAdapter";
    private FlowadapterListener flowadapterListener;
    private ArrayList<Flows> listModels;
    private Activity activity;

    public FlowAdapter(ArrayList<Flows> listModel, Activity activity) {
        this.listModels = listModel;
        this.activity = activity;
    }

    public void setFlowadapterListener(FlowadapterListener flowadapterListener) {
        this.flowadapterListener = flowadapterListener;
    }

    @Override
    public FlowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flow_status, parent, false);
        return new FlowAdapter.ViewHolder(v);
    }

    public void setListModels(ArrayList<Flows> listModels) {
        this.listModels = listModels;
    }

    @Override
    public void onBindViewHolder(final FlowAdapter.ViewHolder holder, final int position) {
        Flows flows = listModels.get(position);
        if (flows.getdeliveryAddress() != null) {
            holder.delivery_address.setText(flows.getdeliveryAddress());
        } else {
            holder.delivery_address.setText("");
        }
        if (flows.getcomments() != null) {
            holder.comments.setText(flows.getcomments());
        } else {
            holder.comments.setText("");
        }
        if (flows.getStatus() != null) {
            Log.e(TAG, "flows_status: " + flows.getStatus());
            if (flows.getStatus().equalsIgnoreCase("SEARCHING")) {
                holder.status_btn.setText("Tap to Start");
            } else if (flows.getStatus().equalsIgnoreCase("STARTED")) {
                holder.status_btn.setText("Tap When Arrived");
            } else if (flows.getStatus().equalsIgnoreCase("DROPPED")) {
                holder.status_btn.setText("Tap When Delivered");
            } else if (flows.getStatus().equalsIgnoreCase("COMPLETED")) {
                holder.status_btn.setText("Delivered");
            }
        }
        if (Map.isFlowStarted) {
            if (!flows.getStatus().equalsIgnoreCase("SEARCHING") && !flows.getStatus()
                    .equalsIgnoreCase("COMPLETED")) {
                holder.rootLayout.setAlpha((float) 1);
                holder.navigation.setClickable(true);
                holder.status_btn.setClickable(true);
                holder.status_btn.setEnabled(true);
                holder.navigation.setEnabled(true);
            } else {
                holder.rootLayout.setAlpha((float) 0.5);
                holder.status_btn.setClickable(false);
                holder.navigation.setClickable(false);
                holder.status_btn.setEnabled(false);
                holder.navigation.setEnabled(false);
            }
        } else {
            if (!flows.getStatus().equalsIgnoreCase("COMPLETED")) {
                holder.rootLayout.setAlpha((float) 1);
                holder.status_btn.setClickable(true);
                holder.status_btn.setClickable(true);
                holder.status_btn.setEnabled(true);
                holder.navigation.setEnabled(true);
            } else {
                holder.rootLayout.setAlpha((float) 0.5);
                holder.status_btn.setClickable(false);
                holder.navigation.setClickable(false);
                holder.status_btn.setEnabled(false);
                holder.navigation.setEnabled(false);
            }
        }
        holder.delivery_address.setTag(flows);
        holder.comments.setTag(flows);
        holder.navigation.setTag(flows);
        holder.status_btn.setTag(flows);
        /*holder.imgGotoPhoto.setVisibility(flows.getAfterImage() != null &&
                !flows.getAfterImage().equals("") &&
                !flows.getAfterImage().equals("null") ? View.VISIBLE : View.GONE);
        holder.imgGotoPhoto.setOnClickListener(v -> {
            if (flows.getAfterImage() != null &&
                    !flows.getAfterImage().equals(""))
                openPhoto(flows.getAfterImage());
        });*/
    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }

    public interface FlowadapterListener {

        void onnavigationClick(Flows flows);

        void onstausClick(Flows flows, int pos, ArrayList<Flows> listModels);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView delivery_address, comments;
        MyBoldTextView order;
        ImageView navigation/*, imgGotoPhoto*/;
        Button status_btn;
        LinearLayout rootLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            order = itemView.findViewById(R.id.order);
            delivery_address = itemView.findViewById(R.id.delivery_address);
            comments = itemView.findViewById(R.id.comments);
            navigation = itemView.findViewById(R.id.navigation);
            status_btn = itemView.findViewById(R.id.status_btn);
            rootLayout = itemView.findViewById(R.id.root_layout);
//            imgGotoPhoto = itemView.findViewById(R.id.imgGotoPhoto);
            navigation.setOnClickListener(this);
            status_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Flows flows = (Flows) v.getTag();
            int position = listModels.indexOf(flows);
            if (flowadapterListener != null) {
                if (v == navigation) {
                    flowadapterListener.onnavigationClick(flows);
                } else if (v == status_btn) {
                    flowadapterListener.onstausClick(flows, position,listModels);
                }
            }
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