package com.hipsterbait.android.other;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hipsterbait.android.R;

public class LoadingPick extends RelativeLayout {

    private ImageView mMask;
    private Context mContext;
    private Animation mAnimation;

    public LoadingPick(Context context) {
        super(context);

        mContext = context;
        init();
    }

    public LoadingPick(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init();
    }

    public LoadingPick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        init();
    }

    private void init() {
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    LoadingPick.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    LoadingPick.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                startAnimating();
            }
        });
    }

    public void startAnimating() {
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        ImageView mPick = new ImageView(mContext);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mPick.setLayoutParams(layoutParams);
        mPick.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.progress_pick));
        mPick.setScaleType(ImageView.ScaleType.MATRIX);
        this.addView(mPick);

        final Matrix pickMatrix = mPick.getImageMatrix();
        final float pickImageHeight = mPick.getDrawable().getIntrinsicHeight();
        final int screenHeight = LoadingPick.this.getHeight();
        final float pickScaleRatio = screenHeight / pickImageHeight;
        pickMatrix.postScale(pickScaleRatio, pickScaleRatio);
        mPick.setImageMatrix(pickMatrix);

        final RelativeLayout.LayoutParams maskLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mMask = new ImageView(mContext);
        maskLayoutParams.height = 100;
        maskLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mMask.setLayoutParams(maskLayoutParams);
        mMask.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.progress_pick_mask));
        mMask.setScaleType(ImageView.ScaleType.MATRIX);

        final Matrix matrix = mMask.getImageMatrix();
        final float imageHeight = mMask.getDrawable().getIntrinsicHeight();
        final float scaleRatio = screenHeight / imageHeight;
        matrix.postScale(scaleRatio, scaleRatio);
        mMask.setImageMatrix(matrix);

        this.addView(mMask);

        animateMask();
    }

    private void animateMask() {
        final int height = getHeight();

        mAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mMask.getLayoutParams();
                params.height = Math.round(height * (1 - interpolatedTime));
                mMask.setLayoutParams(params);
            }
        };
        mAnimation.setDuration(1000);
        mAnimation.setRepeatCount(Animation.INFINITE);

        mMask.startAnimation(mAnimation);
    }

    public void stopAnimating() {
        mAnimation.cancel();

        final RelativeLayout.LayoutParams maskLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mMask.setLayoutParams(maskLayoutParams);
    }
}
