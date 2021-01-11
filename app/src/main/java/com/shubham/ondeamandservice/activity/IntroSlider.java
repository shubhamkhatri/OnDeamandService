package com.shubham.ondeamandservice.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shubham.ondeamandservice.R;
import com.shubham.ondeamandservice.utils.PreferenceManager;

public class IntroSlider extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private LinearLayout Layout_bars;
    private TextView[] bottomBars;
    private int[] screens = {
            R.layout.intro1,
            R.layout.intro2,
            R.layout.intro3,
            R.layout.intro4
    };
    private TextView Skip, Next;
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            ColoredBars(position);
            if (position == screens.length - 1) {
                Next.setText("START");
                Next.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.heading));
                Skip.setVisibility(View.GONE);
            } else {
                Next.setText("NEXT");
                Next.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.heading_text));
                Skip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };
    private ViewPager vp;
    private MyViewPagerAdapter myvpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_slider);
        getSupportActionBar().hide();
        //getActionBar().hide();

        vp = findViewById(R.id.view_pager);
        Layout_bars = findViewById(R.id.layoutBars);
        Skip = findViewById(R.id.skip);
        Next = findViewById(R.id.next);
        myvpAdapter = new MyViewPagerAdapter();


        preferenceManager = new PreferenceManager(this);

        if (!preferenceManager.FirstLaunch()) {
            launchMain();
            finish();
        }
        vp.setAdapter(myvpAdapter);
        vp.addOnPageChangeListener(viewPagerPageChangeListener);

        ColoredBars(0);
    }

    public void next(View v) {
        int i = getItem(+1);
        if (i < screens.length) {
            vp.setCurrentItem(i);
        } else {
            launchMain();
        }
    }

    public void skip(View view) {
        launchMain();
    }

    private void ColoredBars(int thisScreen) {
        int[] colorsInactive = getResources().getIntArray(R.array.dot_on_page_not_active);
        int[] colorsActive = getResources().getIntArray(R.array.dot_on_page_active);
        bottomBars = new TextView[screens.length];

        Layout_bars.removeAllViews();
        for (int i = 0; i < bottomBars.length; i++) {
            bottomBars[i] = new TextView(this);
            bottomBars[i].setTextSize(50);
            bottomBars[i].setText(Html.fromHtml("&#8226;"));
            bottomBars[i].setTextColor(colorsInactive[thisScreen]);
            Layout_bars.addView(bottomBars[i]);
        }
        if (bottomBars.length > 0)
            bottomBars[thisScreen].setTextColor(colorsActive[thisScreen]);
    }

    private int getItem(int i) {
        return vp.getCurrentItem() + i;
    }

    private void launchMain() {
        preferenceManager.setFirstTimeLaunch(false);
        startActivity(new Intent(IntroSlider.this, PreLoginActivity.class));
        finish();
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(screens[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return screens.length;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View v = (View) object;
            container.removeView(v);
        }

        @Override
        public boolean isViewFromObject(View v, Object object) {
            return v == object;
        }
    }
}