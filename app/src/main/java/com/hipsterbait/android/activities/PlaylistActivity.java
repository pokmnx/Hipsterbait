package com.hipsterbait.android.activities;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.CassetteModel;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.AudioPlayerManager;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.widgets.HBButton;
import com.hipsterbait.android.widgets.HBTextView;

import java.util.ArrayList;
import java.util.Map;

public class PlaylistActivity extends ImmersiveActivity implements AudioPlayerManager.AudioPlayerListener {

    public HBButton allButton, shuffleButton, ratingButton;
    public GridView gridView;

    private HBButton mSelectedButton;
    private AudioPlayerManager mManager;
    private String mPlayingKey = "", mLoadingKey = "";
    private Boolean mLoading = false;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        allButton = (HBButton) findViewById(R.id.playlist_button_1);
        shuffleButton = (HBButton) findViewById(R.id.playlist_button_2);
        ratingButton = (HBButton) findViewById(R.id.playlist_button_3);
        gridView = (GridView) findViewById(R.id.playlist_gridview);
        mUser = ((HBApplication) getApplication()).user;
        mSelectedButton = allButton;

        mManager = AudioPlayerManager.getInstance();
        mManager.addListener(this);

        gridView.setAdapter(new PlaylistAdapter(mManager.getPlaylist()));
    }

    @Override
    public void onDestroy() {
        mManager.removeListener(this);
        super.onDestroy();
    }

    public void played() {
        mLoading = false;
        mLoadingKey = "";
        gridView.invalidateViews();
    }

    public void paused() {
        mLoading = false;
        mLoadingKey = "";
        gridView.invalidateViews();
    }

    public void stopped() {
        mLoading = false;
        mLoadingKey = "";
        mPlayingKey = "";
        gridView.invalidateViews();
    }

    public void progress(float progress) {}

    public void playAllTapped(View v) {
        if (mSelectedButton != null)
            mSelectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.hbCharcoal));
        mSelectedButton = (HBButton) v;
        mSelectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.hbGreen));
        mManager.setAutoplay(true);
    }

    public void shuffleTapped(View v) {
        if (mSelectedButton != null)
            mSelectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.hbCharcoal));
        mSelectedButton = (HBButton) v;
        mSelectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.hbGreen));
        mManager.shuffle();
        gridView.setAdapter(new PlaylistAdapter(mManager.getPlaylist()));
        gridView.invalidateViews();
    }

    public void ratingTapped(View v) {
        if (mSelectedButton != null)
            mSelectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.hbCharcoal));
        mSelectedButton = (HBButton) v;
        mSelectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.hbGreen));
        mManager.sortByRating();
        gridView.setAdapter(new PlaylistAdapter(mManager.getPlaylist()));
        gridView.invalidateViews();
    }

    public class PlaylistAdapter extends BaseAdapter implements View.OnClickListener {

        private ArrayList<CassetteModel> mCassettes;

        private PlaylistAdapter(ArrayList<CassetteModel> cassettes) {
            this.mCassettes = cassettes;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) PlaylistActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(PlaylistActivity.this);

                gridView = inflater.inflate(R.layout.playlist_grid_item, null);

            } else {
                gridView = convertView;
            }

            if (position > mCassettes.size() - 1) {
                return gridView;
            }

            final CassetteModel model = mCassettes.get(position);

            if (model == null) {
                Log.e(getString(R.string.hb_log_tag), "Null model for cassette: " + model.getKey());
                return gridView;
            }

            final View finalGridView = gridView;

            model.setCoverArt(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    ImageView imageView = (ImageView) finalGridView
                            .findViewById(R.id.playlist_cell_album_art);

                    try {
                        Glide.with(PlaylistActivity.this)
                                .load(model.getCoverArtURL())
                                .into(imageView);
                    } catch (Exception e) {
                        Log.w(getString(R.string.hb_log_tag), e);
                    }
                }

                @Override
                public void onFail(String error) {
                    Log.w(PlaylistActivity.this.getString(R.string.hb_log_tag), error);
                }
            });

            model.setSong(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {

                    int rating = model.getSong().getAverageRating();

                    ImageView horns1 = finalGridView.findViewById(R.id.playlist_cell_horns_1);
                    ImageView horns2 = finalGridView.findViewById(R.id.playlist_cell_horns_2);
                    ImageView horns3 = finalGridView.findViewById(R.id.playlist_cell_horns_3);
                    ImageView horns4 = finalGridView.findViewById(R.id.playlist_cell_horns_4);
                    ImageView horns5 = finalGridView.findViewById(R.id.playlist_cell_horns_5);

                    if (rating > 0) {
                        horns1.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_blue));
                    } else {
                        horns1.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_char10));
                    }
                    if (rating > 1) {
                        horns2.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_blue));
                    } else {
                        horns2.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_char10));
                    }
                    if (rating > 2) {
                        horns3.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_blue));
                    } else {
                        horns3.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_char10));
                    }
                    if (rating > 3) {
                        horns4.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_blue));
                    } else {
                        horns4.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_char10));
                    }
                    if (rating > 4) {
                        horns5.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_blue));
                    } else {
                        horns5.setImageDrawable(ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.horns_char10));
                    }

                    model.getSong().setBand(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {
                            if (model.getSong().getBand() == null) {
                                return;
                            }

                            ((HBTextView) finalGridView.findViewById(R.id.playlist_item_artist))
                                    .setText(model.getSong().getBand().getName());
                            ((HBTextView) finalGridView.findViewById(R.id.playlist_item_song))
                                    .setText(model.getSong().getName());
                        }

                        @Override
                        public void onFail(String error) {
                            Log.w(PlaylistActivity.this.getString(R.string.hb_log_tag), error);
                        }
                    });
                }

                @Override
                public void onFail(String error) {
                    Log.w(PlaylistActivity.this.getString(R.string.hb_log_tag), error);
                }
            });

            ImageButton playPause = (ImageButton) gridView.findViewById(R.id.playlist_cell_play_pause);
            playPause.setTag(position);
            playPause.setOnClickListener(this);

            if (mManager.isPlaying() && model.getKey().equals(mPlayingKey)) {
                playPause.setImageDrawable(
                        ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.box_pause_button));
            } else {
                playPause.setImageDrawable(
                        ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.box_play_button));
            }

            ProgressBar progressBar = gridView.findViewById(R.id.playlist_cell_progressbar);
            progressBar.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(PlaylistActivity.this, R.color.hbBlue),
                    android.graphics.PorterDuff.Mode.MULTIPLY);

            if (mLoading && model.getKey().equals(mLoadingKey)) {
                progressBar.setVisibility(View.VISIBLE);
                playPause.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                playPause.setVisibility(View.VISIBLE);
            }

            ImageView upArrow = (ImageView) gridView.findViewById(R.id.playlist_cell_up_arrow);
            upArrow.setTag(position);
            upArrow.setOnClickListener(this);

            ImageView downArrow = (ImageView) gridView.findViewById(R.id.playlist_cell_down_arrow);
            downArrow.setTag(position);
            downArrow.setOnClickListener(this);

            if (mManager.isPlaying() && model.getKey().equals(mPlayingKey)) {
                upArrow.setImageDrawable(
                        ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_up_blue));
                downArrow.setImageDrawable(
                        ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_down_blue));
            } else {
                upArrow.setImageDrawable(
                        ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_up_char));
                downArrow.setImageDrawable(
                        ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_down_char));
            }

            return gridView;
        }

        @Override
        public int getCount() {
            return mCassettes.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void onClick(View v) {
            final int tag = (int) v.getTag();
            final View item = gridView.getChildAt(tag);
            final CassetteModel cassetteModel = mCassettes.get(tag);

            if (item == null) {
                return;
            }

            switch (v.getId()) {
                case R.id.playlist_cell_play_pause:
                    if (mLoading) return;

                    final ImageButton playPauseButton = (ImageButton) v;
                    ImageView upArrow = (ImageView) item.findViewById(R.id.playlist_cell_up_arrow);
                    ImageView downArrow = (ImageView) item.findViewById(R.id.playlist_cell_down_arrow);

                    if (mManager.isPlaying() && cassetteModel.getKey().equals(mPlayingKey)) {
                        mManager.pause();
                        playPauseButton.setImageDrawable(
                                ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.box_play_button));

                        upArrow.setImageDrawable(
                                ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_up_char));
                        downArrow.setImageDrawable(
                                ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_down_char));
                    } else {
                        if (mManager.isPlaying()) {
                            mManager.stop();
                        }

                        mLoading = true;
                        mLoadingKey = cassetteModel.getKey();

                        View gridView = this.getView(tag, null, null);
                        final ProgressBar progressBar = gridView.findViewById(R.id.playlist_cell_progressbar);
                        progressBar.setVisibility(View.VISIBLE);
                        playPauseButton.setVisibility(View.GONE);

                        mManager.setIndex(tag);
                        mManager.play();

                        mPlayingKey = cassetteModel.getKey();

                        playPauseButton.setImageDrawable(
                                ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.box_pause_button));

                        upArrow.setImageDrawable(
                                ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_up_blue));
                        downArrow.setImageDrawable(
                                ContextCompat.getDrawable(PlaylistActivity.this, R.drawable.playlist_down_blue));
                    }

                    break;
                case R.id.playlist_cell_up_arrow:
                    mManager.moveUp(tag);
                    gridView.invalidateViews();

                    break;

                case R.id.playlist_cell_down_arrow:
                    mManager.moveDown(tag);
                    gridView.invalidateViews();

                    break;

                default:
                    break;
            }
        }
    }
}
