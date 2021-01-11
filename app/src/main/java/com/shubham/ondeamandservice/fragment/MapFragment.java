package com.shubham.ondeamandservice.fragment;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.activity.BookingBillActivity;
import com.shubham.ondeamandservice.activity.BookingLocationActivity;
import com.shubham.ondeamandservice.activity.SignUpActivity;
import com.shubham.ondeamandservice.activity.WorkerInfoActivity;
import com.shubham.ondeamandservice.adapter.WorkerAdapter;
import com.shubham.ondeamandservice.model.mapWorkerList;
import com.shubham.ondeamandservice.model.workerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gMap;
    private double latitude, longitude;
    private double latitude2, longitude2;
    private ArrayList<mapWorkerList> worker = new ArrayList<mapWorkerList>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final int REQUEST_CODE_PERMISSION = 101;
    private boolean mLocationPermissionGranted;
    private LatLng mLastKnownLocation;


    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLocationPermission();
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


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mLocationPermissionGranted) {
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


                                    double dis = calculateDistance(latitude2, longitude2);
                                    worker.add(new mapWorkerList(Name, address, String.valueOf(dis), rating, phoneNumber, latitude2, longitude2));

                                }

                                if (isAdded()) {
                                    gMap.clear();
                                    // Add customised markers
                                    for (int i = 0; i < worker.size(); i++) {
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(new LatLng(worker.get(i).getLatitude(), worker.get(i).getLongitude()));
                                        markerOptions.title(worker.get(i).getName());
                                        markerOptions.snippet(worker.get(i).getAddress() + " | " + worker.get(i).getRatings() + getString(R.string.unicode_character_star) +
                                                " | " + worker.get(i).getPhone() + " | " + worker.get(i).getDistance() + "Km");
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                                        Marker marker = gMap.addMarker(markerOptions);
                                        marker.setTag(i);
                                    }
                                }

                                if (mLastKnownLocation != null) {
                                    // For zooming automatically to the location of the marker
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(mLastKnownLocation).zoom(16).build();
                                    gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                }
                            } else {
                                //progressDialog.dismiss();
                                Toast.makeText(getActivity(), "Error#121", Toast.LENGTH_SHORT).show();
                                Log.d("DATA FETCH ERROR", "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Set the zoom controls
        gMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable my location button if location permission is granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            gMap.setMyLocationEnabled(true);
        }

        showCurrentLocation();

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                int position = (int) (marker.getTag());
                Intent intent = new Intent(getActivity(), WorkerInfoActivity.class);
                intent.putExtra("Phone", worker.get(position).getPhone());
                intent.putExtra("Latitude", latitude);
                intent.putExtra("Longitude", longitude);
                startActivity(intent);

            }
        });
    }

    private void showCurrentLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
            if (mLocationPermissionGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                // Move the camera to the current location
                                LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                latitude=currentLocation.getLatitude();
                                longitude=currentLocation.getLongitude();
                                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 16));
                                mLastKnownLocation = currentLocationLatLng;

                            } else {
                                showDefaultLocation();
                            }
                        } else {
                            Toast.makeText(getContext(), "unable to find current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "permission denied default location", Toast.LENGTH_SHORT).show();
                showDefaultLocation();
            }
        } catch (SecurityException e) {
        }
    }

    private void showDefaultLocation() {
        LatLng currentLocationLatLng = new LatLng(26.4523971, 80.294914);
        latitude=currentLocationLatLng.latitude;
        longitude=currentLocationLatLng.longitude;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 16));
        mLastKnownLocation = currentLocationLatLng;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationPermissionGranted = true;
                    startMap();

                } else {
                    mLocationPermissionGranted = false;
                    startMap();
                }
            }

        }
    }

    private void startMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_PERMISSION);
        } else {
            mLocationPermissionGranted = true;
            startMap();
        }
    }

}