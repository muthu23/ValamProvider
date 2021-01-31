package com.delivery.provider.Adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.delivery.provider.Bean.ServiceTypes;
import com.delivery.provider.R;

import java.util.ArrayList;

/**
 * Created by CSS on 27-11-2017.
 */

public class ServiceTypeAdapter extends ArrayAdapter<ServiceTypes> implements View.OnClickListener
{
    Context context;
    private ArrayList<ServiceTypes> serviceList;

    public ServiceTypeAdapter(ArrayList<ServiceTypes> serviceList, Context context)
    {
        super(context, R.layout.service_list_item, serviceList);
        this.context = context;
        this.serviceList = serviceList;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        ServiceTypes serviceTypes = (ServiceTypes) object;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ServiceTypes service = getItem(position);

        ViewHolder viewHolder;

        final View result;

        if ( convertView != null )
        {
            viewHolder = new ServiceTypeAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.service_list_item, parent, false);
            viewHolder.service_id = (TextView) convertView.findViewById(R.id.service_id);
            viewHolder.service_name = (TextView) convertView.findViewById(R.id.service_name);

            //result = convertView;

            viewHolder.service_id.setText(service.getId());
            viewHolder.service_name.setText(service.getName());

            convertView.setTag(viewHolder);
        }
        /*else {
            viewHolder = (ServiceTypeAdapter.ViewHolder) convertView.getTag();
            //result = convertView;
        }*/



        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView service_id;
        TextView service_name;
    }
}
