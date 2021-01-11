package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.shubham.ondeamandservice.R;

public class BookingLocationActivity extends AppCompatActivity {

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    private Button next;
    private String Latitude = "", Longitude = "";
    private String CarType, CarModel, CarCompany, CarNumber, Date, Time, Address, PhoneNumberWorker;
    private int Wash, Clean, Vacuum;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_location);
        getSupportActionBar().hide();
        //getActionBar().hide();

        progressDialog=new ProgressDialog(BookingLocationActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Intent intent = getIntent();
        CarType = intent.getStringExtra("Car Type");
        Wash = intent.getIntExtra("Wash",0);
        Clean = intent.getIntExtra("Clean",0);
        Vacuum = intent.getIntExtra("Vacuum",0);
        CarModel = intent.getStringExtra("Car Model");
        CarCompany = intent.getStringExtra("Car Company");
        CarNumber = intent.getStringExtra("Car Number");
        Date = intent.getStringExtra("Date");
        Time = intent.getStringExtra("Time");
        Address = intent.getStringExtra("Address");
        PhoneNumberWorker = intent.getStringExtra("Phone");


        next = (Button) findViewById(R.id.booking_location_next_button);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.booking_location_google_map);

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(BookingLocationActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkClient();
        } else {
            ActivityCompat.requestPermissions(BookingLocationActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {

                            googleMap.getUiSettings().setZoomControlsEnabled(true);
                            if (ActivityCompat.checkSelfPermission(BookingLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(BookingLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            googleMap.setMyLocationEnabled(true);

                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            MarkerOptions options = new MarkerOptions().position(latLng).draggable(true);

                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                            googleMap.addMarker(options);

                            Latitude = String.valueOf(latLng.latitude);
                            Longitude = String.valueOf(latLng.longitude);

                            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    options.position(latLng);

                                    googleMap.clear();

                                    googleMap.addMarker(options);

                                    Latitude = String.valueOf(latLng.latitude);
                                    Longitude = String.valueOf(latLng.longitude);
                                }
                            });

                            next.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(BookingLocationActivity.this, BookingBillActivity.class);
                                    i.putExtra("Car Type", CarType);
                                    i.putExtra("Wash", Wash);
                                    i.putExtra("Clean", Clean);
                                    i.putExtra("Vacuum", Vacuum);
                                    i.putExtra("Car Model", CarModel);
                                    i.putExtra("Car Number", CarNumber);
                                    i.putExtra("Car Company", CarCompany);
                                    i.putExtra("Date", Date);
                                    i.putExtra("Time", Time);
                                    i.putExtra("Address", Address);
                                    i.putExtra("Phone", PhoneNumberWorker);
                                    i.putExtra("Latitude", Latitude);
                                    i.putExtra("Longitude", Longitude);
                                    startActivity(i);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkClient();
            }
        }
    }

    public void checkClient() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        progressDialog.dismiss();
                        getCurrentLocation();
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(BookingLocationActivity.this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }
}