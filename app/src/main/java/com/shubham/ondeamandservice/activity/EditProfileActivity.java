package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shubham.ondeamandservice.R;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.shubham.ondeamandservice.R.string.error;

public class EditProfileActivity extends AppCompatActivity {

    private EditText fname, lname, email, cCode, mnumber, age, address, city;
    private RadioButton genderMale, genderFemale;
    private boolean male, female;
    private Button save;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String Phone,Gender="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().hide();
        //getActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        Phone = firebaseUser.getPhoneNumber();
        Phone = Phone.substring(3);
        setDefault();
        setText();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fname.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    fname.setError(getString(error));
                } else if (lname.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    lname.setError(getString(error));
                } else if (email.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    email.setError(getString(error));
                } else if (age.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    cCode.setError(getString(error));
                } else if (address.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    address.setError(getString(error));
                } else if (city.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    city.setError(getString(error));
                } else {
                    dataUpdate();
                }
            }
        });

    }

    public void setDefault() {
        save = (Button) findViewById(R.id.ed_saveBtn);
        fname = (EditText) findViewById(R.id.ed_firstname);
        lname = (EditText) findViewById(R.id.ed_lastname);
        email = (EditText) findViewById(R.id.ed_email);
        cCode = (EditText) findViewById(R.id.ed_countryCode);
        mnumber = (EditText) findViewById(R.id.ed_mobileNumber);
        age = (EditText) findViewById(R.id.ed_age);
        address = (EditText) findViewById(R.id.ed_address);
        city = (EditText) findViewById(R.id.ed_city);
        genderMale = (RadioButton) findViewById(R.id.ed_male);
        genderFemale = (RadioButton) findViewById(R.id.ed_female);

    }

    private void setText() {
        db.collection("users").document(Phone).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String FName = documentSnapshot.getString("First Name");
                    String LName = documentSnapshot.getString("Last Name");
                    String Age = documentSnapshot.getString("Age");
                    String Emaill = documentSnapshot.getString("Email");
                    String Ccode = documentSnapshot.getString("Country Code");
                    String Mnumber = documentSnapshot.getString("Phone");
                    String Address = documentSnapshot.getString("Address");
                    String City = documentSnapshot.getString("City");
                    String Genderr = documentSnapshot.getString("Gender");

                    fname.setText(FName);
                    lname.setText(LName);
                    email.setText(Emaill);
                    cCode.setText(Ccode);
                    mnumber.setText(Mnumber);
                    age.setText(Age);
                    address.setText(Address);
                    city.setText(City);

                    if (Genderr.equals("Male")) {
                        genderMale.setChecked(true);
                    } else {
                        genderFemale.setChecked(true);
                    }

                }
            }
        });

    }

    public void dataUpdate() {
        String FirstName = fname.getText().toString().trim();
        String LastName = lname.getText().toString().trim();
        String Email = email.getText().toString().trim();
        String CountryCode = cCode.getText().toString().trim();
        String PhoneNo = mnumber.getText().toString().trim();
        String Age = age.getText().toString().trim();
        Gender = genderCheck();
        String Address = address.getText().toString().trim();
        String City = city.getText().toString().trim();

        Map<String, Object> user = new LinkedHashMap<>();

        user.put("First Name", FirstName);
        user.put("Last Name", LastName);
        user.put("Email", Email);
        user.put("Country Code", CountryCode);
        user.put("Phone", PhoneNo);
        user.put("Age", Age);
        user.put("Gender", Gender);
        user.put("Address", Address);
        user.put("City", City);



        db.collection("users").document(Phone).update(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent i = new Intent(EditProfileActivity.this, TabLayoutActivity.class);
                        startActivity(i);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public String genderCheck() {
        String g = "";
        male = genderMale.isChecked();
        female = genderFemale.isChecked();
        if (male)
            g = "Male";
        if (female)
            g = "Female";

        return g;
    }

}