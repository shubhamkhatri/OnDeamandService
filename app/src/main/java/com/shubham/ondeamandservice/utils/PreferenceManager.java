package com.shubham.ondeamandservice.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String FIRST_LAUNCH = "firstLaunch";
    private static final String PREFERENCE = "OnDemandService";
    int MODE = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;
    private Context context;

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences(PREFERENCE, MODE);
        spEditor = sharedPreferences.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        spEditor.putBoolean(FIRST_LAUNCH, isFirstTime);
        spEditor.commit();
    }

    public boolean FirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH, true);
    }

}
