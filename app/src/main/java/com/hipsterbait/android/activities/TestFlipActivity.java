package com.hipsterbait.android.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.hipsterbait.android.R;

import junit.framework.Test;

public class TestFlipActivity extends AppCompatActivity {

    public ImageView imageView, backImageView;
    public Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_flip);

        imageView = (ImageView) findViewById(R.id.test_iv);
        backImageView = (ImageView) findViewById(R.id.test_iv_back);
        button = (Button) findViewById(R.id.test_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet frontSet = (AnimatorSet) AnimatorInflater.loadAnimator(TestFlipActivity.this, R.animator.front_badge_flip);
                frontSet.setTarget(imageView);
                frontSet.start();
                AnimatorSet backSet = (AnimatorSet) AnimatorInflater.loadAnimator(TestFlipActivity.this, R.animator.back_badge_flip);
                backSet.setTarget(backImageView);
                backSet.start();
            }
        });
    }
}
