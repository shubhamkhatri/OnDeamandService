package com.shubham.ondeamandservice.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shubham.ondeamandservice.R;

public class WorkerInfoActivity extends AppCompatActivity {

    private TextView name, age, gender, address, distance, phone, email, servicesDone;
    private Button book;
    private RatingBar rating;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String PhoneNumber;
    private double latitude, longitude;
    private double latitude2, longitude2;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_info);

        Intent i = getIntent();
        PhoneNumber = i.getStringExtra("Phone");
        latitude = i.getDoubleExtra("Latitude", 0);
        longitude = i.getDoubleExtra("Longitude", 0);
        setDefault();
        progressDialog = new ProgressDialog(WorkerInfoActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        setText();

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkerInfoActivity.this, BookingFormActivity.class);
                intent.putExtra("Phone", PhoneNumber);
                startActivity(intent);
            }
        });

    }

    private void setDefault() {
        name = (TextView) findViewById(R.id.worker_info_name_tv);
        age = (TextView) findViewById(R.id.worker_info_age_tv);
        gender = (TextView) findViewById(R.id.worker_info_gender_tv);
        address = (TextView) findViewById(R.id.worker_info_address_tv);
        servicesDone = (TextView) findViewById(R.id.worker_info_service_done_tv);
        distance = (TextView) findViewById(R.id.worker_info_distance_tv);
        phone = (TextView) findViewById(R.id.worker_info_phone_tv);
        email = (TextView) findViewById(R.id.worker_info_email_tv);
        book = (Button) findViewById(R.id.worker_info_book_button);
        rating = (RatingBar) findViewById(R.id.worker_info_ratingBar);
    }

    private void setText() {
        db.collection("workers").document(PhoneNumber).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String fName = documentSnapshot.getString("First Name");
                    String lName = documentSnapshot.getString("Last Name");
                    String Age = documentSnapshot.getString("Age");
                    String Gender = documentSnapshot.getString("Gender");
                    String Address = documentSnapshot.getString("Address");
                    String Rating = documentSnapshot.getString("Rating");
                    String ServicesDone = documentSnapshot.getString("Services Done");
                    String Phone = documentSnapshot.getString("Phone");
                    String Email = documentSnapshot.getString("Email");
                    String latt = documentSnapshot.getString("Latitude");
                    String longg = documentSnapshot.getString("Longitude");
                    latitude2 = Double.parseDouble(latt);
                    longitude2 = Double.parseDouble(longg);
                    String Distance = String.valueOf(calculateDistance(latitude2, longitude2));
                    Distance = Distance + " KM";
                    String namee = fName + " " + lName;

                    name.setText(namee);
                    gender.setText(Gender);
                    age.setText(Age);
                    address.setText(Address);
                    distance.setText(Distance);
                    rating.setRating(Float.parseFloat(Rating));
                    servicesDone.setText(ServicesDone);
                    phone.setText(Phone);
                    email.setText(Email);
                    progressDialog.dismiss();
                }
            }
        });
    }

    public double calculateDistance(double lat1, double lon1) {
        final double lat = latitude;
        final double lon = longitude;
        double lon2, lat2;

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double r = 6371;
        return Math.round((c * r) * 100.0) / 100.0;
    }
}