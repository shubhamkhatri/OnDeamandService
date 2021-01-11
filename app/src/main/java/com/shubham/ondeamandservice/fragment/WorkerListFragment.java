package com.shubham.ondeamandservice.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.activity.SignUpActivity;
import com.shubham.ondeamandservice.activity.WorkerInfoActivity;
import com.shubham.ondeamandservice.adapter.WorkerAdapter;
import com.shubham.ondeamandservice.model.workerList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WorkerListFragment extends Fragment {

    private EditText datePicker;
    private Spinner timeSlot;
    private ImageButton search;
    private ListView workerList;
    private String[] slot = {"Choose one", "08:00AM-12:00PM", "12:00PM-04:00PM", "04:00PM-08:00PM"};
    private String Slot = "", TimeSlot;
    private Calendar myCalendar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private double latitude, longitude;
    private double latitude2, longitude2;
    private ArrayList<com.shubham.ondeamandservice.model.workerList> worker = new ArrayList<workerList>();
    private TextView nothing;

    public static WorkerListFragment newInstance() {
        return new WorkerListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_worker_list, container, false);

        setDefault(v);
        dateSet();
        nothing = (TextView) v.findViewById(R.id.fragment_worker_tv);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, slot);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSlot.setAdapter(adapter1);
        timeSlot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Slot = slot[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getActivity(), "Please choose time slot", Toast.LENGTH_SHORT).show();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (datePicker.getText().toString().trim().isEmpty()) {
                    datePicker.setError(getString(R.string.error));
                    Toast.makeText(getActivity(), "Please choose date", Toast.LENGTH_SHORT).show();
                } else if (Slot.isEmpty() || Slot.compareTo("Choose one") == 0) {
                    Toast.makeText(getActivity(), "Please choose time slot", Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        if(compareDate(datePicker.getText().toString().trim())>0){
                            Toast.makeText(getActivity(), "Date is before today's date", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            datePicker.setError(null);
                            TimeSlot = datePicker.getText().toString().trim() + " " + Slot;
                            if (ContextCompat.checkSelfPermission(
                                    getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                            } else {
                                progressDialog.show();
                                getCurrentLocation();
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getActivity(), "Location Permission Denied!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setDefault(View v) {
        datePicker = (EditText) v.findViewById(R.id.worker_list_date);
        timeSlot = (Spinner) v.findViewById(R.id.worker_list_spinner);
        search = (ImageButton) v.findViewById(R.id.worker_list_search);
        workerList = (ListView) v.findViewById(R.id.worker_list);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        datePicker.setText(sdf.format(myCalendar.getTime()));
    }

    public void dateSet() {
        myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        datePicker.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void getCurrentLocation() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(getActivity())
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(getActivity())
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            Location location = new Location("providerNA");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            getData();
                        }
                    }
                }, Looper.getMainLooper());
    }

    private void getData() {
        worker.clear();

        db.collection("workers").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String firstName = document.getString("First Name");
                                String lastName = document.getString("Last Name");
                                String address = document.getString("Address");
                                String rating = document.getString("Rating");
                                String phoneNumber = document.getString("Phone");
                                ArrayList<String> timeList = (ArrayList<String>) document.get("Time Slot");
                                String latt = document.getString("Latitude");
                                String longg = document.getString("Longitude");
                                latitude2 = Double.parseDouble(latt);
                                longitude2 = Double.parseDouble(longg);
                                String Name = firstName + " " + lastName;

                                if (timeList.contains(TimeSlot)) {
                                    continue;
                                } else {
                                    double dis = calculateDistance(latitude2, longitude2);
                                    worker.add(new workerList(Name, address, String.valueOf(dis), rating, phoneNumber));
                                }
                            }

                            if(worker.size()>0)
                                nothing.setVisibility(View.GONE);

                            WorkerAdapter workerAdapter = new WorkerAdapter(getActivity(), worker);

                            progressDialog.dismiss();
                            workerList.setAdapter(workerAdapter);
                       workerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                workerList d = worker.get(position);
                                Intent intent = new Intent(getActivity(), WorkerInfoActivity.class);
                                intent.putExtra("Phone", d.getPhone());
                                intent.putExtra("Latitude", latitude);
                                intent.putExtra("Longitude", longitude);
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

    public int compareDate(String d2) throws ParseException {
        myCalendar=Calendar.getInstance();

        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Date date1=sdf.parse(sdf.format(myCalendar.getTime()));
        Date date2=sdf.parse(d2);

        return date1.compareTo(date2);
    }
}