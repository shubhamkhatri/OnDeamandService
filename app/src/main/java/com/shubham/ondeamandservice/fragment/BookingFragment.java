package com.shubham.ondeamandservice.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.activity.BookingHistoryInfoActivity;
import com.shubham.ondeamandservice.activity.WorkerInfoActivity;
import com.shubham.ondeamandservice.adapter.BookingHistoryAdapter;
import com.shubham.ondeamandservice.adapter.WorkerAdapter;
import com.shubham.ondeamandservice.model.bookingList;
import com.shubham.ondeamandservice.model.workerList;

import java.util.ArrayList;
import java.util.List;

public class BookingFragment extends Fragment {

    private ListView bookingList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;
    private String PhoneNumber;
    private ArrayList<com.shubham.ondeamandservice.model.bookingList> booking = new ArrayList<com.shubham.ondeamandservice.model.bookingList>();
    private TextView nothing;


    public static BookingFragment newInstance() {
        return new BookingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_booking, container, false);

        nothing = (TextView) v.findViewById(R.id.fragment_booking_tv);
        progressDialog = new ProgressDialog(getActivity());

        booking.clear();
        bookingList = (ListView) v.findViewById(R.id.booking_list);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        PhoneNumber = firebaseUser.getPhoneNumber();
        PhoneNumber = PhoneNumber.substring(3);

        progressDialog.show();
        populateList();

        return v;
    }

    private void populateList() {
        db.collection("bookings").whereEqualTo("User", PhoneNumber)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String id = document.getId();
                        String carModel = document.getString("Car Model");
                        String carCompany = document.getString("Car Company");
                        String carNumber = document.getString("Car Number");
                        String carType = document.getString("Car Type");
                        String wash = document.getString("Wash Price");
                        String clean = document.getString("Clean Price");
                        String vacuum = document.getString("Vacuum Price");
                        String price = document.getString("Total Price");
                        String date = document.getString("Date");
                        String time = document.getString("Time");
                        String status = document.getString("Status");

                        String Model = carCompany + " " + carModel;
                        String Service = "";
                        if (!wash.equals("0"))
                            Service = Service + "Wash ";
                        if (!vacuum.equals("0"))
                            Service = Service + "Vacuum ";
                        if (!clean.equals("0"))
                            Service = Service + "Clean ";
                        String DateTime = date + " " + time;

                        booking.add(new bookingList(id, Model, carNumber, Service, DateTime, carType, price, status));
                    }

                    if (booking.size() > 0)
                        nothing.setVisibility(View.GONE);

                    BookingHistoryAdapter bookingHistoryAdapter = new BookingHistoryAdapter(getActivity(), booking);
                    progressDialog.dismiss();
                    bookingList.setAdapter(bookingHistoryAdapter);
                    bookingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bookingList d = booking.get(position);
                            Intent intent = new Intent(getActivity(), BookingHistoryInfoActivity.class);
                            intent.putExtra("Id", d.getId());
                            startActivity(intent);
                        }
                    });

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Error#121", Toast.LENGTH_SHORT).show();
                    Log.d("DATA FETCH ERROR", "Error getting documents: ", task.getException());
                }
            }
        });
    }
}