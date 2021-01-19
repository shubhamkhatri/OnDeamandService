package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.model.workerList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookingFormActivity extends AppCompatActivity {

    private RadioButton mini, sedan, suv, premium;
    private CheckBox wash, clean, vacuum;
    private EditText model, number, date_et;
    private Spinner company, time;
    private Button next;
    private boolean mini_flag, sedan_flag, suv_flag, premium_flag;
    private String[] slot_array = {"Choose one", "08:00AM-09:00AM", "09:00AM-10:00AM", "10:00AM-11:00AM", "11:00AM-12:00PM", "12:00PM-01:00PM","01:00PM-02:00PM","02:00PM-03:00PM","03:00PM-04:00PM","04:00PM-05:00PM", "05:00PM-06:00PM","06:00PM-07:00PM"};
    private String[] company_array = {"Choose one", "Maruti Suzuki", "Hyundai", "Mahindra", "Tata", "Toyota",
            "Kia", "Volkswagen", "Renault", "MG Motor", "Jeep", "Honda", "BMW", "Audi", "Mercedes Benz",
            "Jaguar", "Skoda", "Nissan", "Ferrari", "Lamborghini", "Tesla", "Porsche", "Datsun", "Other"};
    private String Slot = "", Company = "";
    private Calendar myCalendar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String PhoneNumber, Address, PhoneNumberWorker;
    private ArrayList<String> timeSlot;
    private LocationSettingsRequest.Builder builder;
    private static final int REQUEST_CODE_PERMISSION = 101;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);

        setDefault();

        Intent intent = getIntent();
        PhoneNumberWorker = intent.getStringExtra("Phone");

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        PhoneNumber = firebaseUser.getPhoneNumber();
        PhoneNumber = PhoneNumber.substring(3);
        getAddress();
        getTimeSlot();

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(BookingFormActivity.this, android.R.layout.simple_spinner_item, slot_array);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time.setAdapter(adapter1);
        time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Slot = slot_array[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(BookingFormActivity.this, "Please choose time slot", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(BookingFormActivity.this, android.R.layout.simple_spinner_item, company_array);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        company.setAdapter(adapter2);
        company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Company = company_array[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(BookingFormActivity.this, "Please choose company", Toast.LENGTH_SHORT).show();
            }
        });

        dateSet();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCarType().isEmpty()) {
                    Toast.makeText(BookingFormActivity.this, "Please choose Car Type", Toast.LENGTH_SHORT).show();
                } else if (!wash.isChecked() && !clean.isChecked() && !vacuum.isChecked()) {
                    Toast.makeText(BookingFormActivity.this, "Please choose at least one service type", Toast.LENGTH_SHORT).show();
                } else if (model.getText().toString().trim().isEmpty()) {
                    model.setError(getString(R.string.error));
                    Toast.makeText(BookingFormActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (number.getText().toString().trim().isEmpty()) {
                    number.setError(getString(R.string.error));
                    Toast.makeText(BookingFormActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (Company.isEmpty() || Company.compareTo("Choose one") == 0) {
                    Toast.makeText(BookingFormActivity.this, "Please choose company", Toast.LENGTH_SHORT).show();
                } else if (date_et.getText().toString().trim().isEmpty()) {
                    date_et.setError(getString(R.string.error));
                    Toast.makeText(BookingFormActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (Slot.isEmpty() || Slot.compareTo("Choose one") == 0) {
                    Toast.makeText(BookingFormActivity.this, "Please choose time slot", Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        if(compareDate(date_et.getText().toString().trim())>0){
                            Toast.makeText(BookingFormActivity.this, "Date is before today's date", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            checkTimeSlot();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void checkTimeSlot() {
       String TimeSlot=date_et.getText().toString().trim()+" "+Slot;
        if (timeSlot.contains(TimeSlot)) {
            Toast.makeText(BookingFormActivity.this, "Worker not available at ths time slot. \n Please change date-time.", Toast.LENGTH_SHORT).show();
        } else {
            if (ContextCompat.checkSelfPermission(
                    BookingFormActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BookingFormActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                if (isLocationEnabled(BookingFormActivity.this))
                    setWarning();
                else
                    setLocation();
            }
        }
    }

    private void getTimeSlot() {

       db.collection("workers").document(PhoneNumberWorker).get()
               .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                   @Override
                   public void onSuccess(DocumentSnapshot documentSnapshot) {
                       if (documentSnapshot.exists()) {
                            timeSlot= (ArrayList<String>) documentSnapshot.get("Time Slot");
                       }
                   }
               });
    }

    private void getAddress() {
        db.collection("users").document(PhoneNumber).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Address = documentSnapshot.getString("Address");

                }
            }
        });
    }

    private void setDefault() {
        mini = (RadioButton) findViewById(R.id.bf_mini);
        sedan = (RadioButton) findViewById(R.id.bf_sedan);
        suv = (RadioButton) findViewById(R.id.bf_suv);
        premium = (RadioButton) findViewById(R.id.bf_premium);
        wash = (CheckBox) findViewById(R.id.bf_water_wash);
        clean = (CheckBox) findViewById(R.id.bf_polish);
        vacuum = (CheckBox) findViewById(R.id.bf_inner_vacuum_clean);
        model = (EditText) findViewById(R.id.bf_car_model);
        number = (EditText) findViewById(R.id.bf_car_number);
        date_et = (EditText) findViewById(R.id.bf_date_picker_et);
        company = (Spinner) findViewById(R.id.bf_car_company_spinner);
        time = (Spinner) findViewById(R.id.bf_time_picker_spinner);
        next = (Button) findViewById(R.id.bf_next_button);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        date_et.setText(sdf.format(myCalendar.getTime()));
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

        date_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BookingFormActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public String checkCarType() {

        String g = "";

        mini_flag = mini.isChecked();
        sedan_flag = sedan.isChecked();
        suv_flag = suv.isChecked();
        premium_flag = premium.isChecked();

        if (mini_flag)
            g = "Mini";
        if (sedan_flag)
            g = "Sedan";
        if (suv_flag)
            g = "Suv";
        if (premium_flag)
            g = "Premium";

        return g;
    }

    private void sendData() {
        int wash_ele = 0, clean_ele = 0, vacuum_ele = 0;
        if (wash.isChecked())
            wash_ele = 1;
        if (clean.isChecked())
            clean_ele = 1;
        if (vacuum.isChecked())
            vacuum_ele = 1;

        Intent i = new Intent(BookingFormActivity.this, BookingLocationActivity.class);
        i.putExtra("Car Type", checkCarType());
        i.putExtra("Wash", wash_ele);
        i.putExtra("Clean", clean_ele);
        i.putExtra("Vacuum", vacuum_ele);
        i.putExtra("Car Model", model.getText().toString().trim());
        i.putExtra("Car Number", number.getText().toString().trim());
        i.putExtra("Car Company", Company);
        i.putExtra("Date", date_et.getText().toString().trim());
        i.putExtra("Time", Slot);
        i.putExtra("Address", Address);
        i.putExtra("Phone", PhoneNumberWorker);
        startActivity(i);
    }

    public void setWarning() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BookingFormActivity.this);
        builder.setTitle("Warning!!");
        builder.setIcon(R.drawable.warning);
        builder.setMessage("The process will fetch your location that will be used to show you to the workers on the map." +
                "\nKindly set the pin at your place where you want our service.\n" +
                "\nWould you like to Continue?").setCancelable(false)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        sendData();
                    }
                }).setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    public int compareDate(String d2) throws ParseException {
        myCalendar=Calendar.getInstance();

        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Date date1=sdf.parse(sdf.format(myCalendar.getTime()));
        Date date2=sdf.parse(d2);

        return date1.compareTo(date2);
    }

    public void setLocation() {
        LocationRequest request = new LocationRequest()
                .setFastestInterval(1500)
                .setInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes
                                .RESOLUTION_REQUIRED: {
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(BookingFormActivity.this, REQUEST_CODE_PERMISSION);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            } catch (ClassCastException ex) {
                            }
                            break;
                        }
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                            break;
                        }
                    }
                }
            }
        });
    }

    private boolean isLocationEnabled(Context context) {
        int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
        final boolean enabled = (mode != android.provider.Settings.Secure.LOCATION_MODE_OFF);
        return enabled;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                switch (resultCode) {
                    case RESULT_OK:
                        setWarning();
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(BookingFormActivity.this, "Location permission denied please turn on gps/location of device manually and continue...", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled(BookingFormActivity.this))
                    setWarning();
                else
                    setLocation();
            }
        }
    }

}