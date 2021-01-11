package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.shubham.ondeamandservice.R;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class BookingBillActivity extends AppCompatActivity implements PaymentResultListener {

    private String carType, carModel, carNumber, carCompany, date, time, address, PhoneNumberWorker,PhoneNumber,payment;
    private int wash_ele, clean_ele, vacuum_ele;
    private TextView Model, Number, Type, Company, Address, Date, Time, WashPrice, CleanPrice, VacuumPrice, ExtraPrice, TotalPrice;
    private int wash, clean, vacuum, extra,amount;
    private Button cash, online;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String Latitude = "", Longitude = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_bill);

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        Intent i = getIntent();
        carType = i.getStringExtra("Car Type");
        wash_ele = i.getIntExtra("Wash", 0);
        clean_ele = i.getIntExtra("Clean", 0);
        vacuum_ele = i.getIntExtra("Vacuum", 0);
        carModel = i.getStringExtra("Car Model");
        carNumber = i.getStringExtra("Car Number");
        carCompany = i.getStringExtra("Car Company");
        date = i.getStringExtra("Date");
        time = i.getStringExtra("Time");
        address = i.getStringExtra("Address");
        PhoneNumberWorker = i.getStringExtra("Phone");
        Latitude=i.getStringExtra("Latitude");
        Longitude=i.getStringExtra("Longitude");


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        PhoneNumber = firebaseUser.getPhoneNumber();
        PhoneNumber=PhoneNumber.substring(3);

        setDefault();
        setText();

        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Booking...");
                progressDialog.show();
                payment="Cash";
                updateWorkerTimeSlot();
            }
        });

        online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payment="Online";
                startPayment();
            }
        });

    }

    private void updateWorkerTimeSlot() {
        String timeSlot=date+" "+time;
        db.collection("workers").document(PhoneNumberWorker).update("Time Slot", FieldValue.arrayUnion(timeSlot))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(BookingBillActivity.this, "Error#331", Toast.LENGTH_SHORT).show();
                Log.d("TAG Database Error", e.toString());
            }
        });
    }

    private void updateData() {

        Map<String, Object> booking = new LinkedHashMap<>();

        booking.put("Car Model",carModel);
        booking.put("Car Number",carNumber);
        booking.put("Car Type",carType);
        booking.put("Car Company",carCompany);
        booking.put("Address",address);
        booking.put("Date",date);
        booking.put("Time",time);
        booking.put("Wash Price",WashPrice.getText().toString().trim());
        booking.put("Clean Price",CleanPrice.getText().toString().trim());
        booking.put("Vacuum Price",VacuumPrice.getText().toString().trim());
        booking.put("Extra Price",ExtraPrice.getText().toString().trim());
        booking.put("Total Price",TotalPrice.getText().toString().trim());
        booking.put("Worker",PhoneNumberWorker);
        booking.put("User",PhoneNumber);
        booking.put("Status","Ongoing");
        booking.put("Payment",payment);
        booking.put("Rating","NA");
        booking.put("Latitude",Latitude);
        booking.put("Longitude",Longitude);

        db.collection("bookings").document().set(booking)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        startActivity(new Intent(BookingBillActivity.this,BookingConfirmationActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(BookingBillActivity.this, "Error#333", Toast.LENGTH_SHORT).show();
                Log.d("TAG Database Error", e.toString());
            }
        });
    }

    private void setText() {
        Model.setText(carModel);
        Number.setText(carNumber);
        Type.setText(carType);
        Company.setText(carCompany);
        Address.setText(address);
        Date.setText(date);
        Time.setText(time);

        if (carType.equals("Mini")) {
            wash = 100;
            clean = 50;
            vacuum = 50;
            extra = 50;
        } else if (carType.equals("Sedan")) {
            wash = 100;
            clean = 80;
            vacuum = 80;
            extra = 50;
        } else if (carType.equals("Suv")) {
            wash = 150;
            clean = 100;
            vacuum = 100;
            extra = 80;
        } else {
            wash = 200;
            clean = 150;
            vacuum = 100;
            extra = 80;
        }

        int w = wash * wash_ele;
        int c = clean * clean_ele;
        int v = vacuum * vacuum_ele;
        int total = w + c + v + extra;

        WashPrice.setText(String.valueOf(w));
        CleanPrice.setText(String.valueOf(c));
        VacuumPrice.setText(String.valueOf(v));
        ExtraPrice.setText(String.valueOf(extra));
        TotalPrice.setText(String.valueOf(total));

        amount=total*100;
        progressDialog.dismiss();
    }

    private void setDefault() {
        Model = (TextView) findViewById(R.id.booking_bill_model_tv);
        Number = (TextView) findViewById(R.id.booking_bill_number_tv);
        Type = (TextView) findViewById(R.id.booking_bill_type_tv);
        Company = (TextView) findViewById(R.id.booking_bill_company_tv);
        Address = (TextView) findViewById(R.id.booking_bill_address_tv);
        Date = (TextView) findViewById(R.id.booking_bill_date_tv);
        Time = (TextView) findViewById(R.id.booking_bill_time_tv);
        WashPrice = (TextView) findViewById(R.id.booking_bill_wash_price_tv);
        CleanPrice = (TextView) findViewById(R.id.booking_bill_clean_price_tv);
        VacuumPrice = (TextView) findViewById(R.id.booking_bill_vacuum_price_tv);
        ExtraPrice = (TextView) findViewById(R.id.booking_bill_extra_price_tv);
        TotalPrice = (TextView) findViewById(R.id.booking_bill_total_prize_tv);
        cash = (Button) findViewById(R.id.booking_bill_cash_button);
        online = (Button) findViewById(R.id.booking_bill_online_button);
    }


    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(BookingBillActivity.this, "Payment Successful", Toast.LENGTH_SHORT).show();
        progressDialog.setMessage("Booking...");
        progressDialog.show();
        updateWorkerTimeSlot();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(BookingBillActivity.this, "Error#985", Toast.LENGTH_SHORT).show();
    }

    public void startPayment() {

        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_Ym06v9KZm44fgK");

        /**
         * Set your logo here
         */
        // checkout.setImage(R.drawable.logo);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", "Shubham");
            options.put("description", "Booking By #" + PhoneNumber+"\nFor Worker: "+PhoneNumberWorker);
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            //options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount",amount );//pass amount in currency subunits
            //  options.put("prefill.email", "shubhamkhatri474@gmail.com");
            // options.put("prefill.contact","8299305579");
            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e("ERROR", "Error in starting Razorpay Checkout", e);
        }
    }

}