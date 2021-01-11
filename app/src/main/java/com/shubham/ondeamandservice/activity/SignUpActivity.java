package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.utils.LoginPreferences;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.shubham.ondeamandservice.R.string.error;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, countryCode, mobileNo, age, address, city, otp;
    private RadioButton genderMale, genderFemale;
    private TextView login;
    private Button register;
    private boolean male, female;
    private String Gender = "", mobile, codeSentToUser;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private AlertDialog alertDialog;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LoginPreferences loginPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();
        //getActionBar().hide();
        setDefault();

        loginPreferences = new LoginPreferences(this);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    firstName.setError(getString(error));
                } else if (lastName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    lastName.setError(getString(error));
                } else if (email.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    email.setError(getString(error));
                } else if (!email.getText().toString().trim().contains("@")) {
                    Toast.makeText(SignUpActivity.this, "Please enter valid Email!!", Toast.LENGTH_SHORT).show();
                    email.setError("Invalid Email");
                } else if (countryCode.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    countryCode.setError(getString(error));
                } else if (mobileNo.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    mobileNo.setError(getString(error));
                } else if (mobileNo.getText().toString().trim().length() != 10) {
                    Toast.makeText(SignUpActivity.this, "Please enter a valid number!!", Toast.LENGTH_SHORT).show();
                    mobileNo.setError("Invalid Number!");
                } else if (age.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    age.setError(getString(error));
                } else if (genderCheck().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please select Gender", Toast.LENGTH_SHORT).show();
                } else if (address.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    address.setError(getString(error));
                } else if (city.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields!!", Toast.LENGTH_SHORT).show();
                    city.setError(getString(error));
                } else {
                    checkNumber();
                }

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

    }

    private void setDefault() {
        firstName = (EditText) findViewById(R.id.su_firstname);
        lastName = (EditText) findViewById(R.id.su_lastname);
        email = (EditText) findViewById(R.id.su_email);
        countryCode = (EditText) findViewById(R.id.su_countryCode);
        mobileNo = (EditText) findViewById(R.id.su_mobileNumber);
        age = (EditText) findViewById(R.id.su_age);
        address = (EditText) findViewById(R.id.su_address);
        city = (EditText) findViewById(R.id.su_city);
        genderMale = (RadioButton) findViewById(R.id.su_male);
        genderFemale = (RadioButton) findViewById(R.id.su_female);
        login = (TextView) findViewById(R.id.su_login_text);
        register = (Button) findViewById(R.id.su_registerBtn);
    }

    public void setAlert() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(SignUpActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.otp_dialog, null);
        otp = (EditText) mView.findViewById(R.id.txt_input);
        Button btn_cancel = (Button) mView.findViewById(R.id.btn_cancel);
        Button btn_okay = (Button) mView.findViewById(R.id.btn_okay);
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
                String code = otp.getText().toString().trim();
                if (code.length() != 6) {
                    otp.setError(getString(error));
                    otp.requestFocus();
                    return;
                } else {
                    progressDialog.setMessage("SignUp in progress");
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    verifyOTPcode(code);
                }
            }
        });
        alertDialog.show();
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

    private void checkNumber() {
        String e = mobileNo.getText().toString().trim();
        db.collection("users").document(e).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                mobileNo.setError("Already Registered!!");
                                Toast.makeText(SignUpActivity.this, "User already exists with this Number \n Please Login", Toast.LENGTH_SHORT).show();
                            } else {
                                mobile = countryCode.getText().toString().trim() +
                                        mobileNo.getText().toString().trim();
                                progressDialog.setMessage("Loading...");
                                progressDialog.show();
                                sendVerificationCode(mobile);
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error650", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider
            .OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otp.setText(code);
                verifyOTPcode(code);
            }
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSentToUser = s;
            progressDialog.dismiss();
            setAlert();
            //OTP that is sent to the user
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(SignUpActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(SignUpActivity.this, "Too many Request", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void verifyOTPcode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSentToUser, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            dataUpdate();
                            alertDialog.dismiss();
                        } else {
                            //verification unsuccessful.. display an error message
                            progressDialog.dismiss();
                            String message = "Something is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }
                            Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void dataUpdate() {
        String FirstName = firstName.getText().toString().trim();
        String LastName = lastName.getText().toString().trim();
        String Email = email.getText().toString().trim();
        String CountryCode = countryCode.getText().toString().trim();
        String PhoneNo = mobileNo.getText().toString().trim();
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
        user.put("Services Done","0");


        db.collection("users").document(PhoneNo).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loginPreferences.setLaunch(1);
                        Intent intent = new Intent(SignUpActivity.this, TabLayoutActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "Error#333", Toast.LENGTH_SHORT).show();
                        Log.d("TAG Database Error", e.toString());
                        firebaseAuth.signOut();
                    }
                });

    }
}