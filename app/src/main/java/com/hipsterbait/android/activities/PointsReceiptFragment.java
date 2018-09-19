package com.hipsterbait.android.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hipsterbait.android.R;
import com.hipsterbait.android.Resources.Exceptions.PointsNotFound;
import com.hipsterbait.android.models.Points;
import com.hipsterbait.android.models.Rank;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.models.UserPoints;
import com.hipsterbait.android.models.UserRank;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.PointsStore;
import com.hipsterbait.android.other.RanksStore;
import com.hipsterbait.android.widgets.HBTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PointsReceiptFragment extends BottomSheetDialogFragment {

    public HBTextView title, pointsTotal, progressLabel;
    public GridView gridView;
    public View progressContainer, progressBackground, progressBar;

    private ArrayList<UserPoints> mResult;
    private String mTitle;
    private int mOldPoints, mNewPoints;
    private User mUser;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
    };

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        mTitle = args.getString("title_string");
        mResult = args.getParcelableArrayList("userpoints_arraylist_arg");
        mOldPoints = args.getInt("old_points");
        mNewPoints = args.getInt("new_points");
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        final View contentView = View.inflate(getContext(), R.layout.fragment_points_receipt, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }


        mUser = ((HBApplication) getActivity().getApplication()).user;

        ImageView closeButton = (ImageView) contentView.findViewById(R.id.receipt_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        title = (HBTextView) contentView.findViewById(R.id.receipt_title);
        title.setText(mTitle);

        pointsTotal = (HBTextView) contentView.findViewById(R.id.receipt_total_points);
        pointsTotal.setText(String.valueOf(mNewPoints));

        progressLabel = (HBTextView) contentView.findViewById(R.id.receipt_progress_label);
        progressContainer = contentView.findViewById(R.id.receipt_progress_container);
        progressBackground = contentView.findViewById(R.id.receipt_progress_background);
        progressBar = contentView.findViewById(R.id.receipt_progress);

        final UserRank userRank = mUser.getCurrentRank();
        DecimalFormat formatter = new DecimalFormat("#,###,###");

        if (userRank != null) {
            try {
                final Rank currentRank = RanksStore.getInstance().getRank(userRank.getRank());
                String rankLabelString = currentRank.getLabel();
                progressLabel.setText(rankLabelString + " - " + formatter.format(mUser.getPoints()) + " of " + formatter.format(currentRank.getNextLevelPoints()));

            } catch (Exception e) {
                Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }
        }

        gridView = (GridView) contentView.findViewById(R.id.receipt_gridview);
        gridView.setAdapter(new UserPointsAdapter(mResult));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay()
                        .getSize(size);
                int width = size.x - 32;
                int points = mOldPoints % 1000;
                final float progress = ((float) points) / 1000;
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Math.round(progress * width),
                        progressBar.getHeight());
                layoutParams.setMargins(1, 1, 1, 1);
                progressBar.setLayoutParams(layoutParams);


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.requestLayout();

                        boolean levelUp = ((mOldPoints % 1000) + mNewPoints) > 1000;

                        if (levelUp) {

                            ProgressAnimation anim = new ProgressAnimation(1);
                            anim.setDuration(500);
                            anim.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    progressBar.getLayoutParams().width = 0;
                                    progressBar.requestLayout();

                                    int newPoints = ((mNewPoints + (mOldPoints % 1000)) - 1000);
                                    float progress = ((float) newPoints) / 1000;
                                    ProgressAnimation anim = new ProgressAnimation(progress);
                                    anim.setDuration(500);
                                    progressBar.startAnimation(anim);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });

                            progressBar.startAnimation(anim);

                        } else {
                            int newPoints = (mNewPoints + mOldPoints) % 1000;
                            float progress = ((float) newPoints) / 1000;
                            ProgressAnimation anim = new ProgressAnimation(progress);
                            anim.setDuration(500);
                            progressBar.startAnimation(anim);
                        }
                    }
                }, 50);
            }
        });
    }

    private class UserPointsAdapter extends BaseAdapter {
        private ArrayList<UserPoints> mUserPoints;

        private UserPointsAdapter(ArrayList<UserPoints> userPoints) {
            this.mUserPoints = userPoints;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(getActivity());

                gridView = inflater.inflate(R.layout.points_receipt_grid_item, null);

            } else {
                gridView = convertView;
            }

            UserPoints userPoints;

            try {
                userPoints = mUserPoints.get(position);
            } catch (IndexOutOfBoundsException e) {
                return gridView;
            }

            PointsStore store = PointsStore.getInstance();

            try {
                Points points = store.getPoints(userPoints.getPointsKey());

                HBTextView label = (HBTextView) gridView.findViewById(R.id.receipt_griditem_label);
                label.setText(points.getName());
                HBTextView pointsText = (HBTextView) gridView.findViewById(R.id.receipt_griditem_points);
                pointsText.setText(String.valueOf((int) userPoints.getValue()));

            } catch (PointsNotFound e) {
                Log.e(getString(R.string.hb_log_tag), "Points for " + userPoints.getPointsKey() + " not found in the store");
            }

            return gridView;
        }

        @Override
        public int getCount() {
            return mUserPoints.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    private class ProgressAnimation extends Animation {
        private int mStartWidth, mWidth;

        private ProgressAnimation(float progress) {
            mStartWidth = progressBar.getWidth();
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay()
                    .getSize(size);
            int width = size.x - 32;
            mWidth = Math.round(progress * width);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
            progressBar.getLayoutParams().width = newWidth;
            progressBar.requestLayout();
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
