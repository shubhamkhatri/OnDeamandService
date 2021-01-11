package com.shubham.ondeamandservice.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginPreferences {
    private static final String LAUNCH = "Launch";
    private static final String PREFERENCE = "MyPref";
    int MODE = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;
    private Context context;

    public LoginPreferences(Context context) {
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences(PREFERENCE, MODE);
        spEditor = sharedPreferences.edit();
    }

    public void setLaunch(int value) {
        spEditor.putInt(LAUNCH, value);
        spEditor.apply();
    }

    public int UserLaunch() {
        return sharedPreferences.getInt(LAUNCH, 0);
    }
}
