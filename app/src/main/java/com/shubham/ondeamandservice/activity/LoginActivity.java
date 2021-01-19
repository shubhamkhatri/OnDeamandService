package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.utils.LoginPreferences;

import java.util.concurrent.TimeUnit;

import static com.shubham.ondeamandservice.R.string.error;

public class LoginActivity extends AppCompatActivity {

    private EditText phone, otp, countryCode;
    private Button login, verify;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private String mobile, codeSentToUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LoginPreferences loginPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        //getActionBar().hide();

        loginPreferences = new LoginPreferences(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Logging in");
        progressDialog.setMessage("Just a moment...");
        progressDialog.setCancelable(false);
        mAuth = FirebaseAuth.getInstance();

        setDefault();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countryCode.getText().toString().trim().isEmpty()) {
                    countryCode.setError(getString(error));
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (phone.getText().toString().trim().isEmpty()) {
                    phone.setError(getString(error));
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    checkNumber();
                }
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = otp.getText().toString().trim();
                if (code.length() != 6) {
                    otp.setError(getString(error));
                    otp.requestFocus();
                    return;
                }

                //progressDialog.show();
                verifyOTPcode(code);

            }
        });
    }

    private void setDefault() {
        phone = (EditText) findViewById(R.id.lg_mobileNumber);
        countryCode = (EditText) findViewById(R.id.lg_countryCode);
        otp = (EditText) findViewById(R.id.lg_otp);
        login = (Button) findViewById(R.id.lg_login_button);
        verify = (Button) findViewById(R.id.lg_verify_button);
        progressBar = (ProgressBar) findViewById(R.id.lg_progressbar);
    }

    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
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
                progressBar.setVisibility(View.INVISIBLE);
                otp.setText(code);
                verifyOTPcode(code);
            }
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSentToUser = s;
            verify.setClickable(true);
            //OTP that is sent to the user
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(LoginActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(LoginActivity.this, "Too many Request", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void verifyOTPcode(String code) {
        progressBar.setVisibility(View.INVISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSentToUser, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            loginPreferences.setLaunch(1);
                            Intent intent = new Intent(LoginActivity.this, TabLayoutActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Something is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    }
                });
    }

    private void checkNumber() {
        String e = phone.getText().toString().trim();
        db.collection("users").document(e).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                mobile = countryCode.getText().toString().trim() +
                                        phone.getText().toString().trim();

                                login.setVisibility(View.GONE);
                                verify.setVisibility(View.VISIBLE);
                                otp.setVisibility(View.VISIBLE);
                                countryCode.setFocusable(false);
                                countryCode.setEnabled(false);
                                phone.setFocusable(false);
                                phone.setEnabled(false);

                                progressBar.setVisibility(View.VISIBLE);

                                sendVerificationCode(mobile);
                            } else {
                                phone.setError("Not a Registered User!!");
                                Toast.makeText(LoginActivity.this, "User does not exists with this number \n Please SignUp first", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Error650", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}