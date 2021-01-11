

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
import com.shubham.ondeamandservice.model.bookingList;
import com.shubham.ondeamandservice.model.workerList;

import java.util.ArrayList;

public class BookingHistoryAdapter extends ArrayAdapter<bookingList> {

    public BookingHistoryAdapter(@NonNull Context context, ArrayList<bookingList> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.single_booking_history_list, parent, false);
        }
        bookingList currentList = getItem(position);
        TextView carModelText = listItemView.findViewById(R.id.sbhl_car_model_tv);
        TextView carNumberText = listItemView.findViewById(R.id.sbhl_car_number_tv);
        TextView serviceText = listItemView.findViewById(R.id.sbhl_wash_tv);
        TextView dateTimeText = listItemView.findViewById(R.id.sbhl_date_tv);
        TextView priceText = listItemView.findViewById(R.id.sbhl_price_tv);
        ImageView carImage = listItemView.findViewById(R.id.sbhl_image);
        ImageView statusImage=listItemView.findViewById(R.id.sbhl_status_image);

        carModelText.setText(currentList.getModel());
        carNumberText.setText(currentList.getNumber());
        serviceText.setText(currentList.getService());
        dateTimeText.setText(currentList.getDateTime());
        priceText.setText("₹ " + currentList.getPrice());

        if (currentList.getCarType().equals("Mini"))
            carImage.setImageResource(R.drawable.car_mini);
        else if (currentList.getCarType().equals("Sedan"))
            carImage.setImageResource(R.drawable.car_sedan);
        else if (currentList.getCarType().equals("Suv"))
            carImage.setImageResource(R.drawable.car_suv);
        else
            carImage.setImageResource(R.drawable.car_premium);

        if(currentList.getStatus().equals("Ongoing"))
            statusImage.setImageResource(R.drawable.pending);
        else if(currentList.getStatus().equals("Cancel"))
            statusImage.setImageResource(R.drawable.cancel);
        else
            statusImage.setImageResource(R.drawable.complete);

        return listItemView;
    }
}
