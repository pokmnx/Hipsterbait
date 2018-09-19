package com.hipsterbait.android.activities;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.AudioPlayerManager;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.ProgressCircle;
import com.hipsterbait.android.other.ProgressCircleMenu;

public class LoggedMenuActivity extends ImmersiveActivity implements AudioPlayerManager.AudioPlayerListener {

    public ImageView avatarImage, playPauseButton;
    public SeekBar seekBar;

    private boolean mPlaying = false;
    private User user;
    private AudioPlayerManager manager;
    private ProgressCircleMenu mCircle;
    private SettingsContentObserver mSettingsContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_menu);

        avatarImage = (ImageView) findViewById(R.id.logged_menu_avatar_image);
        playPauseButton = (ImageView) findViewById(R.id.logged_menu_play_button);
        mCircle = (ProgressCircleMenu) findViewById(R.id.logged_menu_progress_circle);
        seekBar = (SeekBar) findViewById(R.id.logged_menu_seekBar);
        seekBar.setMax(0);

        user = ((HBApplication) getApplication()).user;

        if (user == null) {
            Log.w(getString(R.string.hb_log_tag), "user is not set");
            FirebaseAuth.getInstance().signOut();
            finish();
            return;
        }

        Bitmap avatar = user.getAvatarImage();
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), avatar);
        drawable.setCornerRadius(avatarImage.getHeight() / 2);

        if (avatar != null) {
            avatarImage.setImageDrawable(drawable);
        } else {
            avatarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar_generic));
        }

        manager = AudioPlayerManager.getInstance();
        manager.addListener(this);
        mPlaying = manager.isPlaying();
        if (mPlaying) {
            playPauseButton.setImageDrawable(
                    ContextCompat.getDrawable(LoggedMenuActivity.this, R.drawable.pause_button));
        }

        final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        seekBar.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBar.setProgress(currentVolume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mSettingsContentObserver = new SettingsContentObserver(this,new Handler());
        getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
    }

    @Override
    public void onDestroy() {
        if (manager != null) manager.removeListener(this);
        if (mSettingsContentObserver != null) getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        super.onDestroy();
    }

    // Menu buttons
    public void findCassettesTapped(View v) {
        Intent intent = new Intent(LoggedMenuActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void qrScannerTapped(View v) {
        Intent intent = new Intent(LoggedMenuActivity.this, QRScannerActivity.class);
        startActivity(intent);
    }

    public void accountTapped(View v) {
        Intent intent = new Intent(LoggedMenuActivity.this, UserAccountActivity.class);
        startActivity(intent);
        finish();
    }

    public void baitTapped(View v) {

        if (user.getUnhiddenCassettes().size() < 1) {
            Intent intent = new Intent(LoggedMenuActivity.this, NoBaitsActivity.class);
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(LoggedMenuActivity.this, BaitSelectActivity.class);
        startActivity(intent);
        finish();
    }

    public void cassetteBoxTapped(View v) {
        Intent intent = new Intent(LoggedMenuActivity.this, CassetteBoxActivity.class);
        startActivity(intent);
        finish();
    }

    public void achievementsTapped(View v) {
        Intent intent = new Intent(LoggedMenuActivity.this, AchievementsTabActivity.class);
        startActivity(intent);
        finish();
    }

    public void artistTapped(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://hipsterbait.com/artists"));
        startActivity(browserIntent);
    }

    public void chartsTapped(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://hipsterbait.com/charts"));
        startActivity(browserIntent);
    }

    public void logoutTapped(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(LoggedMenuActivity.this, HomeMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void closeButtonTapped(View v) {
        finish();
    }

    // Cassette controls
    public void rewTapped(View v) {
        manager.prev();
    }

    public void playTapped(View v) {
        if (mPlaying) {
            manager.pause();
            mPlaying = false;
        } else {
            manager.play();
            mPlaying = true;
        }
    }

    public void fwdTapped(View v) {
        manager.next();
    }

    public void ejectTapped(View v) {
        Intent intent = new Intent(LoggedMenuActivity.this, PlaylistActivity.class);
        startActivity(intent);
    }

    public void played() {
        playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(LoggedMenuActivity.this, R.drawable.pause_button));
    }

    public void paused() {
        playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(LoggedMenuActivity.this, R.drawable.play_button));
    }

    public void stopped() {
        playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(LoggedMenuActivity.this, R.drawable.play_button));
        mCircle.setAngle(0);
    }

    public void progress(float progress) {
        mCircle.setAngle(progress);
    }

    public class SettingsContentObserver extends ContentObserver {
        int previousVolume;
        Context context;

        public SettingsContentObserver(Context c, Handler handler) {
            super(handler);
            context=c;

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            int delta=previousVolume-currentVolume;

            if(delta>0)
            {
                previousVolume=currentVolume;
                seekBar.setProgress(currentVolume);
            }
            else if(delta<0)
            {
                previousVolume=currentVolume;
                seekBar.setProgress(currentVolume);
            }
        }
    }
}
