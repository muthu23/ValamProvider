package com.delivery.provider.Adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.delivery.provider.Bean.Locations;
import com.delivery.provider.R;
import com.delivery.provider.Utilities.MyTextView;

import java.util.ArrayList;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

    private static final String TAG = "LocationsAdapter";

    private ArrayList<Locations> listModels;
    private Context context;
    boolean[] selectedService;

    private LocationsListener locationsListener;

    public LocationsAdapter(ArrayList<Locations> listModel, Context context) {
        this.listModels = listModel;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.flow_location, parent, false);
        return new ViewHolder(v);
    }



    public interface LocationsListener {
        void onCloseClick(Locations locations);

        void onSrcClick(Locations locations);

        void onDestClick(Locations locations);

        void onGoodsClick(Locations locations);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        MyTextView srcTxt, destTxt, goodTxt;
        ImageView closeImg;

        public ViewHolder(View itemView) {
            super(itemView);
            srcTxt = itemView.findViewById(R.id.src_txt);
            destTxt = itemView.findViewById(R.id.dest_txt);
            goodTxt = itemView.findViewById(R.id.good_txt);
            //closeImg = itemView.findViewById(R.id.close_img);

//            srcTxt.setOnClickListener(this);
//            destTxt.setOnClickListener(this);
//            goodTxt.setOnClickListener(this);
//            closeImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            if (locationsListener != null) {
//                Locations locations = (Locations) v.getTag();
//                if (v == closeImg) {
//                    locationsListener.onCloseClick(locations);
//                } else if (v == srcTxt) {
//                    locationsListener.onSrcClick(locations);
//                } else if (v == destTxt) {
//                    locationsListener.onDestClick(locations);
//                } else if (v == goodTxt) {
//                    showGoodsDialog(locations);
//                }
//            }
//        }
        }


    }
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Locations locations = listModels.get(position);

            if (locations.getsAddress() != null) {
                holder.srcTxt.setText(locations.getsAddress());
            } else {
                holder.srcTxt.setText("");
            }
            if (locations.getdAddress() != null) {
                holder.destTxt.setText(locations.getdAddress());
            } else {
                holder.destTxt.setText("");
            }
            if (locations.getGoods() != null) {
                holder.goodTxt.setText(locations.getGoods());
            } else {
                holder.goodTxt.setText("");
            }

            holder.destTxt.setTag(locations);
            holder.srcTxt.setTag(locations);
            holder.goodTxt.setTag(locations);

        }


        @Override
        public int getItemCount() {
            return listModels.size();
        }

    }

