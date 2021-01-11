package com.shubham.ondeamandservice.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.fragment.BookingFragment;
import com.shubham.ondeamandservice.fragment.MapFragment;
import com.shubham.ondeamandservice.fragment.ProfileFragment;
import com.shubham.ondeamandservice.fragment.WorkerListFragment;

public class TabLayoutActivity extends AppCompatActivity {

    private final Fragment mMapFragment = MapFragment.newInstance();
    private final Fragment mWorkerListFragment = WorkerListFragment.newInstance();
    private final Fragment mBookingFragment = BookingFragment.newInstance();
    private final Fragment mProfileFragment = ProfileFragment.newInstance();


    private Fragment mActiveFragment = mMapFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    mActiveFragment = mMapFragment;
                    loadFragment(mActiveFragment);
                    return true;

                case R.id.navigation_list:
                    mActiveFragment = mWorkerListFragment;
                    loadFragment(mActiveFragment);
                    return true;

                case R.id.navigation_booking:
                    mActiveFragment = mBookingFragment;
                    loadFragment(mActiveFragment);
                    return true;

                case R.id.navigation_profile:
                    mActiveFragment = mProfileFragment;
                    loadFragment(mActiveFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_layout);

        loadFragment(mActiveFragment);

        // Set the onNavigationItemSelected listener
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MapFragment.REQUEST_CODE_PERMISSION){
            mMapFragment.onActivityResult(requestCode, resultCode, data);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onBackPressed() {
        BottomNavigationView mBottomNavigationView = findViewById(R.id.navigation);
        if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_map)
        {
            super.onBackPressed();
            finishAffinity();
        }
        else
        {
            mBottomNavigationView.setSelectedItemId(R.id.navigation_map);
        }
    }
}
