package com.hipsterbait.android.activities;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;

public class PhotoFullscreenActivity extends ImmersiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_fullscreen);

        String url = getIntent().getStringExtra(getString(R.string.image_extra));

        ImageView imageView = (ImageView) findViewById(R.id.photo_imageview);

        Glide.with(this)
                .load(url)
                .into(imageView);
    }

    public void closeButtonTapped(View v) {
        finish();
    }
}
