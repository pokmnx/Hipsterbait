package com.hipsterbait.android.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.hipsterbait.android.R;

public class AchievementsTabActivity extends ImmersiveActivity {

    public ViewPager mViewPager;
    public LinearLayout activeTabLayout;
    public View leftPadding, activeTab, rightPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements_tab);

        activeTabLayout = (LinearLayout) findViewById(R.id.achievements_tab_activetab_layout);

        leftPadding = findViewById(R.id.achievements_tab_left_padding);
        activeTab = findViewById(R.id.achievements_tab_active_tab);
        rightPadding = findViewById(R.id.achievements_tab_right_padding);

        mViewPager = (ViewPager) findViewById(R.id.achievements_tab_viewpager);

        mViewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
    }

    public void pointsTapped(View v) {
        selectTab(0);
    }

    public void badgesTapped(View v) {
        selectTab(1);
    }

    public void rankTapped(View v) {
        selectTab(2);
    }

    public void logoTapped(View v) {
        Intent intent = new Intent(AchievementsTabActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void burgerTapped(View v) {
        Intent intent = new Intent(AchievementsTabActivity.this, LoggedMenuActivity.class);
        startActivity(intent);
    }

    private void selectTab(int tab) {
        mViewPager.setCurrentItem(tab);

        Animation animation = new ChangeTabAnimation(tab);
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
                    return AchievementsPointsFragment.newInstance();
                case 1:
                    return AchievementsBadgeFragment.newInstance();
                case 2:
                default:
                    return AchievementsRankFragment.newInstance();
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
                    return "Points";
                case 1:
                    return "Badges";
                case 2:
                default:
                    return "Ranks";
            }
        }
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
