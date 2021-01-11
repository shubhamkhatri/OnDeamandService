package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.adapter.BookingHistoryAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.shubham.ondeamandservice.R.string.error;

public class BookingHistoryInfoActivity extends AppCompatActivity {

    private TextView name, phoneNumber, carCompany, carNumber, date, time, payment, wash, clean, vacuum, extra, total, report;
    private ImageView statusImage, carImage;
    private RatingBar ratings;
    private Button cancel;
    private String id, worker, user;
    private String Payment, Status, CarCompany, CarName, CarNumber, CarType, Date, Time, WashPrice, CleanPrice, VacuumPrice, ExtraPrice, TotalPrice;
    private String Name, Rating;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog2;
    private Calendar myCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history_info);

        setDefault();
        progressDialog = new ProgressDialog(BookingHistoryInfoActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        progressDialog2 = new ProgressDialog(BookingHistoryInfoActivity.this);
        progressDialog2.setMessage("Updating...");
        progressDialog2.setCancelable(false);

        Intent intent = getIntent();
        id = intent.getStringExtra("Id");

        setText();
    }

    private void setText() {
        db.collection("bookings").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        worker = documentSnapshot.getString("Worker");
                        user = documentSnapshot.getString("User");
                        Status = documentSnapshot.getString("Status");
                        CarCompany = documentSnapshot.getString("Car Company");
                        CarName = documentSnapshot.getString("Car Name");
                        CarNumber = documentSnapshot.getString("Car Number");
                        CarType = documentSnapshot.getString("Car Type");
                        Date = documentSnapshot.getString("Date");
                        Time = documentSnapshot.getString("Time");
                        Payment = documentSnapshot.getString("Payment");
                        WashPrice = documentSnapshot.getString("Wash Price");
                        CleanPrice = documentSnapshot.getString("Clean Price");
                        VacuumPrice = documentSnapshot.getString("Vacuum Price");
                        ExtraPrice = documentSnapshot.getString("Extra Price");
                        TotalPrice = documentSnapshot.getString("Total Price");
                        Rating = documentSnapshot.getString("Rating");

                        phoneNumber.setText(worker);

                        if (Status.equals("Ongoing")) {
                            statusImage.setImageResource(R.drawable.pending);
                        } else if (Status.equals("Complete")) {
                            statusImage.setImageResource(R.drawable.complete);
                        } else {
                            statusImage.setImageResource(R.drawable.cancel);
                        }

                        if (!Rating.equals("NA")) {
                            ratings.setRating(Float.parseFloat(Rating));
                        }

                        carCompany.setText(CarCompany + " " + CarName);
                        carNumber.setText(CarNumber);
                        date.setText(Date);
                        time.setText(Time);
                        payment.setText("Payment: " + Payment);

                        if (CarType.equals("Mini")) {
                            carImage.setImageResource(R.drawable.car_mini);
                        } else if (CarType.equals("Sedan")) {
                            carImage.setImageResource(R.drawable.car_sedan);
                        } else if (CarType.equals("Suv")) {
                            carImage.setImageResource(R.drawable.car_suv);
                        } else {
                            carImage.setImageResource(R.drawable.car_premium);
                        }

                        wash.setText(WashPrice);
                        clean.setText(CleanPrice);
                        vacuum.setText(VacuumPrice);
                        extra.setText(ExtraPrice);
                        total.setText(TotalPrice);

                        getWorkerInfo(worker);

                        if (Status.equals("Ongoing")) {
                            cancel.setVisibility(View.VISIBLE);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    setWarning(Date + " " + Time);
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(BookingHistoryInfoActivity.this, "Error650", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void getWorkerInfo(String worker) {
        db.collection("workers").document(worker).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String Fname = documentSnapshot.getString("First Name");
                        String Lname = documentSnapshot.getString("Last Name");
                        String WorkerRatings = documentSnapshot.getString("Rating");
                        String TotalRatings = documentSnapshot.getString("Total Rating");
                        Name = Fname + " " + Lname;
                        name.setText(Name);
                        setReport();

                        if (Rating.equals("NA") && !Status.equals("Cancel")) {
                            ratings.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    setRatings(WorkerRatings, TotalRatings);
                                    return false;
                                }
                            });
                        }

                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(BookingHistoryInfoActivity.this, "Error650", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void setRatings(String workerRatings, String TotalRatings) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BookingHistoryInfoActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.rating_dialog, null);
        RatingBar ratingBar = (RatingBar) mView.findViewById(R.id.dialog_ratingBar);
        Button btn_cancel = (Button) mView.findViewById(R.id.dialog_btn_cancel);
        Button btn_okay = (Button) mView.findViewById(R.id.dialog_btn_okay);
        alert.setView(mView);
        alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        btn_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog2.show();
                Float rate = ratingBar.getRating();
                int TotalRate = Integer.valueOf(TotalRatings);
                Float avg_rate = (rate + (TotalRate * Float.parseFloat(workerRatings))) / (TotalRate + 1);
                int finalTotal = TotalRate + 1;
                db.collection("bookings").document(id).update("Rating", String.valueOf(rate))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db.collection("workers").document(worker)
                                        .update("Rating", String.valueOf(avg_rate),
                                                "Total Rating", String.valueOf(finalTotal))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(BookingHistoryInfoActivity.this, "Ratings Updated", Toast.LENGTH_SHORT).show();
                                                BookingHistoryInfoActivity.this.ratings.setRating(rate);
                                                alertDialog.dismiss();
                                                progressDialog2.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(BookingHistoryInfoActivity.this, "Error610", Toast.LENGTH_SHORT).show();
                                        progressDialog2.dismiss();
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BookingHistoryInfoActivity.this, "Error611", Toast.LENGTH_SHORT).show();
                        progressDialog2.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void setReport() {
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = "Worker Name: " + Name + "\nBooking Id: " + id + "\n\nIssue Details:";
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_TEXT, s);
                intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"abc@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Issue Report By " + user);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    public void setWarning(String s) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BookingHistoryInfoActivity.this);
        builder.setTitle("Booking Canceling");
        builder.setIcon(R.drawable.warning);
        builder.setMessage("Are you sure you want to cancel your booking?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (Payment.equals("Cash")) {
                            progressDialog2.show();
                            cancelBooking(s);
                        } else {
                            setBankDetail(s);
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void setBankDetail(String s) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BookingHistoryInfoActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.bank_details_dialog, null);
        EditText name = (EditText) mView.findViewById(R.id.bank_dialog_holder_name);
        EditText account = (EditText) mView.findViewById(R.id.bank_dialog_account_number);
        EditText confirmAccount = (EditText) mView.findViewById(R.id.bank_dialog_confirm_account_number);
        EditText ifsc = (EditText) mView.findViewById(R.id.bank_dialog_ifsc);
        Button btn_cancel = (Button) mView.findViewById(R.id.bank_dialog_btn_cancel);
        Button btn_okay = (Button) mView.findViewById(R.id.bank_dialog_btn_okay);
        alert.setView(mView);
        alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        btn_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().trim().isEmpty()) {
                    name.setError(getString(R.string.error));
                    Toast.makeText(BookingHistoryInfoActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (account.getText().toString().trim().isEmpty()) {
                    account.setError(getString(R.string.error));
                    Toast.makeText(BookingHistoryInfoActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (confirmAccount.getText().toString().trim().isEmpty()) {
                    confirmAccount.setError(getString(R.string.error));
                    Toast.makeText(BookingHistoryInfoActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!account.getText().toString().trim().equals(confirmAccount.getText().toString().trim())) {
                    account.setError(getString(R.string.error));
                    confirmAccount.setError(getString(R.string.error));
                    Toast.makeText(BookingHistoryInfoActivity.this, "Account number do not match", Toast.LENGTH_SHORT).show();
                } else if (ifsc.getText().toString().trim().isEmpty()) {
                    ifsc.setError(getString(R.string.error));
                    Toast.makeText(BookingHistoryInfoActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog2.dismiss();
                    String HolderName = name.getText().toString().trim();
                    String HolderAccount = account.getText().toString().trim();
                    String HolderIfsc = ifsc.getText().toString().trim();

                    myCalendar=Calendar.getInstance();

                    String myFormat = "dd/MM/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                    String d=sdf.format(myCalendar.getTime());


                    Map<String, Object> bill = new LinkedHashMap<>();

                    bill.put("Account Name", HolderName);
                    bill.put("Account Number", HolderAccount);
                    bill.put("Ifsc", HolderIfsc);
                    bill.put("Amount", TotalPrice);
                    bill.put("Phone", user);
                    bill.put("From", "User");
                    bill.put("Id", id);
                    bill.put("Status", "Unpaid");
                    bill.put("Date",d);

                    db.collection("bills").document().set(bill)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(BookingHistoryInfoActivity.this, "Bill Request Sent", Toast.LENGTH_SHORT).show();
                                    cancelBooking(s);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BookingHistoryInfoActivity.this, "Error450", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        alertDialog.show();
    }

    private void cancelBooking(String dateTime) {
        db.collection("bookings").document(id).update("Status", "Cancel")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db.collection("workers").document(worker).update("Time Slot", FieldValue.arrayRemove(dateTime))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(BookingHistoryInfoActivity.this, "Booking Cancelled Successfully", Toast.LENGTH_SHORT).show();
                                        progressDialog2.dismiss();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(BookingHistoryInfoActivity.this, "Error650", Toast.LENGTH_SHORT).show();
                                progressDialog2.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(BookingHistoryInfoActivity.this, "Error651", Toast.LENGTH_SHORT).show();
                progressDialog2.dismiss();
            }
        });
    }

    private void setDefault() {
        name = (TextView) findViewById(R.id.booking_history_info_name_tv);
        phoneNumber = (TextView) findViewById(R.id.booking_history_info_phone_tv);
        carCompany = (TextView) findViewById(R.id.booking_history_info_car_name_tv);
        carNumber = (TextView) findViewById(R.id.booking_history_info_car_number_tv);
        date = (TextView) findViewById(R.id.booking_history_info_date_tv);
        time = (TextView) findViewById(R.id.booking_history_info_time_tv);
        payment = (TextView) findViewById(R.id.booking_history_info_payment_tv);
        wash = (TextView) findViewById(R.id.booking_history_info_wash_price_tv);
        clean = (TextView) findViewById(R.id.booking_history_info_clean_price_tv);
        vacuum = (TextView) findViewById(R.id.booking_history_info_vacuum_price_tv);
        extra = (TextView) findViewById(R.id.booking_history_info_extra_price_tv);
        total = (TextView) findViewById(R.id.booking_history_info_total_prize_tv);
        report = (TextView) findViewById(R.id.booking_history_info_report_tv);
        carImage = (ImageView) findViewById(R.id.booking_history_info_car_image);
        statusImage = (ImageView) findViewById(R.id.booking_history_info_status_image);
        ratings = (RatingBar) findViewById(R.id.booking_history_info_ratingBar);
        cancel = (Button) findViewById(R.id.booking_history_info_cancel_button);

    }

}