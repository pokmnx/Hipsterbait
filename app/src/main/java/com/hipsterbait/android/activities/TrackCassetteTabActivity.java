package com.hipsterbait.android.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBTextView;

public class TrackCassetteTabActivity extends ImmersiveActivity {

    public ViewPager mViewPager;
    public LinearLayout activeTabLayout;
    public View leftPadding, activeTab, rightPadding;
    public String cassetteKey;
    public HBTextView mCassetteNameView;

    private User mUser;
    private Cassette mCassette;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_cassette_tab);

        cassetteKey = getIntent().getStringExtra(getString(R.string.cassette_extra_key));
        Boolean archived = getIntent().getBooleanExtra(getString(R.string.archived_extra_bool), false);

        activeTabLayout = (LinearLayout) findViewById(R.id.track_tab_activetab_layout);

        leftPadding = findViewById(R.id.track_tab_left_padding);
        activeTab = findViewById(R.id.track_tab_active_tab);
        rightPadding = findViewById(R.id.track_tab_right_padding);

        mUser = ((HBApplication) getApplication()).user;

        if (archived) {
            mCassette = mUser.getArchivedCassetteByKey(cassetteKey);
        } else {
            mCassette = mUser.getCassetteByKey(cassetteKey);
        }

        mViewPager = (ViewPager) findViewById(R.id.track_tab_viewpager);

        mViewPager.setAdapter(new TrackCassetteTabActivity.SectionPagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mCassetteNameView = (HBTextView) findViewById(R.id.track_tab_cassette_name);
        mCassetteNameView.setText("#" + mCassette.getNumber() + " " + mCassette.getCassetteModel().getName());
    }

    private void selectTab(int tab) {
        mViewPager.setCurrentItem(tab);

        Animation animation = new TrackCassetteTabActivity.ChangeTabAnimation(tab);
        animation.setDuration(300);
        activeTabLayout.startAnimation(animation);
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TrackCassetteFragment.newInstance(mCassette);
                case 1:
                    return CassetteStatsFragment.newInstance(mCassette);
                case 2:
                default:
                    LocationHintsFragment locFragment = LocationHintsFragment.newInstance();
                    locFragment.setData(mCassette, TrackCassetteTabActivity.this);
                    return locFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Track";
                case 1:
                    return "Stats";
                case 2:
                default:
                    return "Location Hints";
            }
        }
    }

    public void trackTapped(View v) {
        selectTab(0);
    }

    public void statsTapped(View v) {
        selectTab(1);
    }

    public void locationTapped(View v) {
        selectTab(2);
    }

    private class ChangeTabAnimation extends Animation {
        int selectedTab;
        float initialLeftWeight, leftDeltaWeight;
        float initialRightWeight, rightDeltaWeight;

        private ChangeTabAnimation(int selectedTab) {
            this.selectedTab = selectedTab;
            initialLeftWeight = ((LinearLayout.LayoutParams)
                    leftPadding.getLayoutParams()).weight;
            leftDeltaWeight = selectedTab - initialLeftWeight;

            initialRightWeight = ((LinearLayout.LayoutParams)
                    rightPadding.getLayoutParams()).weight;
            rightDeltaWeight = (2 - selectedTab) - initialRightWeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            leftPadding.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.MATCH_PARENT,
                            initialLeftWeight + (interpolatedTime * leftDeltaWeight)));
            rightPadding.setLayoutParams
                    (new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.MATCH_PARENT,
                            initialRightWeight + (interpolatedTime * rightDeltaWeight)));
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
