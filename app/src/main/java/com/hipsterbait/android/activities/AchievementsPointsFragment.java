package com.hipsterbait.android.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.hipsterbait.android.R;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBTextView;

import java.text.DecimalFormat;

public class AchievementsPointsFragment extends Fragment {

    public LinearLayout layout;
    public HBTextView pointsTotal, bestDayLabel, bestMonthLabel, bestCassetteLabel;
    public View dayProgress, dayBackground, monthProgress, monthBackground, cassetteProgress, cassetteBackground;

    public static AchievementsPointsFragment newInstance() {
        AchievementsPointsFragment fragment = new AchievementsPointsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_achievements_points, container, false);

        layout = (LinearLayout) view.findViewById(R.id.points_layout);

        pointsTotal = (HBTextView) view.findViewById(R.id.points_total_value);
        bestDayLabel = (HBTextView) view.findViewById(R.id.points_best_day_value);
        bestMonthLabel = (HBTextView) view.findViewById(R.id.points_best_month_value);
        bestCassetteLabel = (HBTextView) view.findViewById(R.id.points_best_cassette_value);

        dayProgress = view.findViewById(R.id.points_day_progress);
        dayBackground = view.findViewById(R.id.points_day_progress_bg);
        monthProgress = view.findViewById(R.id.points_month_progress);
        monthBackground = view.findViewById(R.id.points_month_progress_bg);
        cassetteProgress = view.findViewById(R.id.points_cassette_progress);
        cassetteBackground = view.findViewById(R.id.points_cassette_bg);

        User user = ((HBApplication) getActivity().getApplication()).user;

        DecimalFormat formatter = new DecimalFormat("#,###,###");

        pointsTotal.setText(formatter.format(user.getPoints()));

        User.BestDayPointsResult bestDayResult = user.getBestPointsDay();
        User.BestMonthPointsResult bestMonthResult = user.getBestPointsMonth();
        User.BestCassetteResult bestCassetteResult = user.getBestCassettePoints();

        if (bestDayResult.getPoints() != 0) {
            bestDayLabel.setText(bestDayResult.getMonth() +
                    " " + bestDayResult.getDay() +
                    ", " + bestDayResult.getYear() +
                    " : " + formatter.format(bestDayResult.getPoints()) +
                    " Points");
        }

        if (bestMonthResult.getPoints() != 0) {
            bestMonthLabel.setText(bestMonthResult.getMonth() +
                    " " + bestMonthResult.getYear() +
                    " : " + formatter.format(bestMonthResult.getPoints()) +
                    " Points");
        }

        if (bestCassetteResult.getPoints() != 0) {
            bestCassetteLabel.setText(bestCassetteResult.getName() +
                    " : " + formatter.format(bestCassetteResult.getPoints()) +
                    " Points");
        }

        Animation anim = new ShowProgressAnimation(user);
        anim.setDuration(300);
        layout.startAnimation(anim);

        return view;
    }

    private class ShowProgressAnimation extends Animation {
        float initialDayWeight, initialMonthWeight, initialCassetteWeight;
        float dayWeightDelta, monthWeightDelta, cassetteWeightDelta;
        float bgDayWeightDelta, bgMonthWeightDelta, bgCassetteWeightDelta;

        public ShowProgressAnimation(User user) {
            initialDayWeight = ((LinearLayout.LayoutParams)
                    dayProgress.getLayoutParams()).weight;
            initialMonthWeight = ((LinearLayout.LayoutParams)
                    monthProgress.getLayoutParams()).weight;
            initialCassetteWeight = ((LinearLayout.LayoutParams)
                    cassetteProgress.getLayoutParams()).weight;

            int userPoints = user.getPoints();

            User.BestDayPointsResult bestDayResult = user.getBestPointsDay();
            User.BestMonthPointsResult bestMonthResult = user.getBestPointsMonth();
            User.BestCassetteResult bestCassetteResult = user.getBestCassettePoints();

            float progress = (float) bestDayResult.getPoints() / (float) userPoints;
            dayWeightDelta = progress - initialDayWeight;
            bgDayWeightDelta = 0 - progress;
            if (bestDayResult.getPoints() == 0) {
                dayWeightDelta = 0;
                bgDayWeightDelta = 0;
            }

            progress = (float) bestMonthResult.getPoints() / (float) userPoints;
            monthWeightDelta = progress - initialMonthWeight;
            bgMonthWeightDelta = 0 - progress;
            if (bestMonthResult.getPoints() == 0) {
                monthWeightDelta = 0;
                bgMonthWeightDelta = 0;
            }

            progress = (float) bestCassetteResult.getPoints() / (float) userPoints;
            cassetteWeightDelta = progress - initialCassetteWeight;
            bgCassetteWeightDelta = 0 - progress;
            if (bestCassetteResult.getPoints() == 0) {
                cassetteWeightDelta = 0;
                bgCassetteWeightDelta = 0;
            }
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            LinearLayout.LayoutParams dayParams = (LinearLayout.LayoutParams)
                    dayProgress.getLayoutParams();
            dayParams.weight = initialDayWeight + (interpolatedTime * dayWeightDelta);
            dayProgress.setLayoutParams(dayParams);
            LinearLayout.LayoutParams bgDayParams = (LinearLayout.LayoutParams)
                    dayBackground.getLayoutParams();
            bgDayParams.weight = 1 + (interpolatedTime * bgDayWeightDelta);
            dayBackground.setLayoutParams(bgDayParams);

            LinearLayout.LayoutParams monthParams = (LinearLayout.LayoutParams)
                    monthProgress.getLayoutParams();
            monthParams.weight = initialMonthWeight + (interpolatedTime * monthWeightDelta);
            monthProgress.setLayoutParams(monthParams);
            LinearLayout.LayoutParams bgMonthParams = (LinearLayout.LayoutParams)
                    monthBackground.getLayoutParams();
            bgMonthParams.weight = 1 + (interpolatedTime * bgMonthWeightDelta);
            monthBackground.setLayoutParams(bgMonthParams);

            LinearLayout.LayoutParams cassetteParams = (LinearLayout.LayoutParams)
                    cassetteProgress.getLayoutParams();
            cassetteParams.weight = initialCassetteWeight + (interpolatedTime * cassetteWeightDelta);
            cassetteProgress.setLayoutParams(cassetteParams);
            LinearLayout.LayoutParams bgCassetteParams = (LinearLayout.LayoutParams)
                    cassetteBackground.getLayoutParams();
            bgCassetteParams.weight = 1 + (interpolatedTime * bgCassetteWeightDelta);
            cassetteBackground.setLayoutParams(bgCassetteParams);
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() { return true; }
    }
}
