

package com.shubham.ondeamandservice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.model.workerList;

import java.util.ArrayList;
import java.util.List;

public class WorkerAdapter extends ArrayAdapter<workerList> {

    public WorkerAdapter(@NonNull Context context, ArrayList<workerList> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.single_worker_list, parent, false);
        }
        workerList currentList = getItem(position);
        TextView nameText = listItemView.findViewById(R.id.swl_name_tv);
        TextView addressText = listItemView.findViewById(R.id.swl_address_tv);
        TextView distanceText = listItemView.findViewById(R.id.swl_distance_tv);
        RatingBar ratingBar = listItemView.findViewById(R.id.swl_ratingBar);

        nameText.setText(currentList.getName());
        addressText.setText(currentList.getAddress());
        distanceText.setText(currentList.getDistance() + " KM");
        ratingBar.setRating(Float.parseFloat(currentList.getRatings()));

        return listItemView;
    }
}
