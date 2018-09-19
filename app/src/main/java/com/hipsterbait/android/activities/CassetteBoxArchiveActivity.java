package com.hipsterbait.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hipsterbait.android.R;
import com.hipsterbait.android.models.Cassette;
import com.hipsterbait.android.models.CassetteModel;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.User;
import com.hipsterbait.android.other.AudioPlayerManager;
import com.hipsterbait.android.other.HBApplication;
import com.hipsterbait.android.other.OnSwipeTouchListener;
import com.hipsterbait.android.widgets.HBButton;
import com.hipsterbait.android.widgets.HBTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class CassetteBoxArchiveActivity extends ImmersiveActivity implements View.OnClickListener {

    public RecyclerView recyclerView;
    public LinearLayoutManager layoutManager;
    public CassetteBoxArchiveActivity.CassetteAdapter adapter;
    public Button button1, button2, button3, button4;

    private final int BUTTON_1 = 1, BUTTON_2 = 2, BUTTON_3 = 3, BUTTON_4 = 4;
    private Button mSelectedButton;

    private AudioPlayerManager mManager;
    private boolean mPlaying = false, mLoading = false;
    private String mPlayingKey = "", mLoadingKey = "", mSelectedKey = "";
    private ImageButton mSelectedDisclosureButton, mPlayingButton;
    private User mUser;
    private MediaPlayer mPlayer;
    private int mPlayingRow = -1, mSelectedRow = -1, mEditingRow = -1;
    private LinearLayout mEditingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cassette_box_archive);

        mUser = ((HBApplication) getApplication()).user;

        button1 = (Button) findViewById(R.id.archive_button_1);
        button1.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.archive_button_2);
        button2.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.archive_button_3);
        button3.setOnClickListener(this);
        button4 = (Button) findViewById(R.id.archive_button_4);
        button4.setOnClickListener(this);

        mManager = AudioPlayerManager.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.cassette_archive_recycler);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CassetteBoxArchiveActivity.CassetteAdapter(mUser.getArchivedCassettesByDate());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mEditingRow > -1) {
                    float distance = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 160,
                            getResources().getDisplayMetrics()
                    );
                    TranslateAnimation animation = new TranslateAnimation(
                            Animation.ABSOLUTE, 0,
                            Animation.ABSOLUTE, distance,
                            Animation.ABSOLUTE, 0,
                            Animation.ABSOLUTE, 0);
                    animation.setDuration(300);
                    final LinearLayout editingLayout = mEditingLayout;
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            editingLayout.setTranslationX(0);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    mEditingLayout.startAnimation(animation);
                    mEditingRow = -1;
                    mEditingLayout = null;
                }
            }
        });

        CassetteBoxArchiveActivity.CassetteItemTouchCallback callback = new CassetteBoxArchiveActivity.CassetteItemTouchCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, new CassetteBoxArchiveActivity.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (direction == ItemTouchHelper.LEFT) {
                    if (mEditingRow > -1 && position != mEditingRow) {
                        float distance = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 160,
                                getResources().getDisplayMetrics()
                        );
                        TranslateAnimation animation = new TranslateAnimation(
                                Animation.ABSOLUTE, 0,
                                Animation.ABSOLUTE, distance,
                                Animation.ABSOLUTE, 0,
                                Animation.ABSOLUTE, 0);
                        animation.setDuration(300);
                        final LinearLayout editingLayout = mEditingLayout;
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                editingLayout.setTranslationX(0);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        mEditingLayout.startAnimation(animation);
                        mEditingRow = -1;
                        mEditingLayout = null;
                    }

                    mEditingRow = position;
                    mEditingLayout = ((CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder) viewHolder).mainLayout;

                } else if (direction == ItemTouchHelper.RIGHT) {
                    if (position == mEditingRow) {
                        mEditingRow = -1;
                        mEditingLayout = null;
                    }
                }
            }
        });

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);

        mSelectedButton = button1;
    }

    @Override
    public void onResume() {
        super.onResume();
        String selectedKey = mSelectedKey;
        onClick(mSelectedButton);
        mSelectedKey = selectedKey;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mPlayer != null) mPlayer.release();
    }

    public void onClick(View v) {
        mSelectedRow = -1;
        mEditingRow = -1;
        mPlayingRow = -1;
        mEditingLayout = null;

        mSelectedButton.setBackgroundResource(R.color.hbCharcoal);

        mSelectedButton = (HBButton) v;
        mSelectedButton.setBackgroundResource(R.color.hbGreen);

        switch (Integer.parseInt((String) mSelectedButton.getTag())) {
            case BUTTON_1:
            default:
                adapter = new CassetteAdapter(mUser.getArchivedCassettesByDate());
                break;
            case BUTTON_2:
                adapter = new CassetteAdapter(mUser.getArchivedCassettesByRating());
                break;
            case BUTTON_3:
                adapter = new CassetteAdapter(mUser.getArchivedCassettesByPoints());
                break;
            case BUTTON_4:
                adapter = new CassetteAdapter(mUser.getArchivedCassettesByNumber());
                break;
        }

        recyclerView.setAdapter(adapter);
    }

    public class ArchivedCassetteAdapter extends BaseAdapter implements View.OnClickListener {

        private ArrayList<Cassette> mCassettes;

        private ArchivedCassetteAdapter(ArrayList<Cassette> cassettes) {
            this.mCassettes = cassettes;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) CassetteBoxArchiveActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = new View(CassetteBoxArchiveActivity.this);

                if (position > mCassettes.size() - 1) {
                    gridView = inflater.inflate(R.layout.cassette_empty_grid_item, null);
                    return gridView;
                }

                gridView = inflater.inflate(R.layout.cassette_grid_item, null);

            } else {
                gridView = convertView;
            }

            if (position > mCassettes.size() - 1) {
                return gridView;
            }

            final Cassette cassette = mCassettes.get(position);
            CassetteModel model = cassette.getCassetteModel();

            if (model == null) {
                Log.e(getString(R.string.hb_log_tag), "Null model for cassette: " + cassette.getKey());
                return gridView;
            }

            final View finalGridView = gridView;

            model.setCoverArt(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    ImageView imageView = (ImageView) finalGridView
                            .findViewById(R.id.cassette_cell_album_art);

                    try {
                        Glide.with(CassetteBoxArchiveActivity.this)
                                .load(cassette.getCassetteModel().getCoverArtURL())
                                .into(imageView);
                    } catch (Exception e) {
                        Log.w(getString(R.string.hb_log_tag), e);
                    }
                }

                @Override
                public void onFail(String error) {
                    Log.w(CassetteBoxArchiveActivity.this.getString(R.string.hb_log_tag), error);
                }
            });

            HBTextView songText = ((HBTextView) gridView.findViewById(R.id.cassette_cell_name));
            songText.setText("#" + cassette.getNumber() + " " + model.getName());

            ((HBTextView) gridView.findViewById(R.id.cassette_cell_archive_label))
                    .setText(getString(R.string.unarchive));

            long dateFound = new Date().getTime();

            HBTextView dateText = new HBTextView(CassetteBoxArchiveActivity.this);
            try {
                dateFound = mUser.getDateFoundByArchivedCassetteKey(cassette.getKey());
                String dateString = DateFormat.format("MMM d, yyyy", dateFound).toString();
                dateText = ((HBTextView) gridView.findViewById(R.id.cassette_cell_date));
                dateText.setText(dateString);
            } catch (Exception e) {
                Log.w(getString(R.string.hb_log_tag), e);
            }

            // TODO: Set flag if (cassette.flagged)

            ImageView pickImageView = (ImageView) gridView.findViewById(R.id.cassette_cell_pick);

            if (cassette.isHidden()) {
                pickImageView.setImageDrawable(ContextCompat.getDrawable(CassetteBoxArchiveActivity.this, R.drawable.pick_green));
            } else {
                pickImageView.setImageDrawable(ContextCompat.getDrawable(CassetteBoxArchiveActivity.this, R.drawable.pick_char));
            }

            ImageButton playPause;
            if (mPlayingKey.equals(cassette.getKey()) && mPlaying) {
                playPause = (ImageButton) gridView.findViewById(R.id.cassette_cell_play_pause);
                playPause.setImageDrawable(ContextCompat.getDrawable(
                        CassetteBoxArchiveActivity.this, R.drawable.box_pause_button));
                mPlayingRow = position;
            } else {
                playPause = (ImageButton) gridView.findViewById(R.id.cassette_cell_play_pause);
            }
            playPause.setTag(position);
            playPause.setOnClickListener(this);

            ImageButton disclosure = (ImageButton) gridView.findViewById(R.id.cassette_cell_disclosure);
            disclosure.setTag(position);
            if (position == mSelectedRow) {
                disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                R.drawable.disclosure_blue));
                mSelectedDisclosureButton = disclosure;
            } else {
                disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                R.drawable.disclosure_right));
            }
            disclosure.setOnClickListener(this);

            FrameLayout archive = (FrameLayout) gridView.findViewById(R.id.cassette_cell_archive_button);
            archive.setTag(position);
            archive.setOnClickListener(this);

            FrameLayout delete = (FrameLayout) gridView.findViewById(R.id.cassette_cell_delete_button);
            delete.setTag(position);
            delete.setOnClickListener(this);

            pickImageView.setTag(position);
            pickImageView.setOnClickListener(this);

            final LinearLayout mainLayout = (LinearLayout)
                    gridView.findViewById(R.id.cassette_cell_main_layout);
            final int finalPosition = position;

            OnSwipeTouchListener listener = new OnSwipeTouchListener(CassetteBoxArchiveActivity.this) {
                public void onTap(View v) { onClick(v); }
                public void onSwipeLeft() {
                    final float distance = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 160,
                            getResources().getDisplayMetrics()
                    );

                    if (mEditingRow != finalPosition && mEditingRow > -1) {
                        TranslateAnimation animation = new TranslateAnimation(
                                Animation.ABSOLUTE, 0,
                                Animation.ABSOLUTE, distance,
                                Animation.ABSOLUTE, 0,
                                Animation.ABSOLUTE, 0);
                        animation.setDuration(300);
                        final LinearLayout editingLayout = mEditingLayout;
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                editingLayout.setTranslationX(0);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        mEditingLayout.startAnimation(animation);
                        mEditingRow = -1;
                        mEditingLayout = null;
                    }

                    TranslateAnimation animation = new TranslateAnimation(
                            Animation.ABSOLUTE, 0,
                            Animation.ABSOLUTE, -distance,
                            Animation.ABSOLUTE, 0,
                            Animation.ABSOLUTE, 0);
                    animation.setDuration(300);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mainLayout.setTranslationX(-distance);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    mainLayout.startAnimation(animation);
                    mEditingLayout = mainLayout;
                    mEditingRow = finalPosition;
                }
                public void onSwipeRight() {
                    if (mEditingRow != finalPosition) {
                        return;
                    }
                    float distance = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 160,
                            getResources().getDisplayMetrics()
                    );
                    TranslateAnimation animation = new TranslateAnimation(
                            Animation.ABSOLUTE, 0,
                            Animation.ABSOLUTE, distance,
                            Animation.ABSOLUTE, 0,
                            Animation.ABSOLUTE, 0);
                    animation.setDuration(300);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mainLayout.setTranslationX(0);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    mainLayout.startAnimation(animation);
                    mEditingRow = -1;
                    mEditingLayout = null;
                }
            };
            mainLayout.setOnTouchListener(listener);
            songText.setOnTouchListener(listener);
            dateText.setOnTouchListener(listener);
            pickImageView.setOnTouchListener(listener);
            disclosure.setOnTouchListener(listener);

            return gridView;
        }

        @Override
        public int getCount() {
            if (mCassettes.size() > 6) {
                return mCassettes.size();
            }
            return 6;
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
            if (v.getTag() == null) {
                return;
            }
            final int tag = (int) v.getTag();
            final Cassette cassette = mCassettes.get(tag);
            final CassetteModel model = cassette.getCassetteModel();

            switch (v.getId()) {
                case R.id.cassette_cell_play_pause:
                    if (mManager.isPlaying()) {
                        mManager.pause();
                    }

                    final ImageButton playPauseButton = (ImageButton) v;

                    model.setSong(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {
                            model.getSong().downloadSong(new ModelPropertySetCallback() {
                                @Override
                                public void onSuccess() {
                                    if (mPlaying && mPlayingRow == tag) {
                                        mPlayer.pause();
                                        mPlaying = false;
                                        playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                CassetteBoxArchiveActivity.this, R.drawable.box_play_button));

                                    } else {

                                        if (mPlaying) {
                                            mPlayingButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxArchiveActivity.this, R.drawable.box_play_button));
                                            mPlayer.stop();
                                            mPlayer.release();
                                        }

                                        if (mPlayingRow == tag) {
                                            mPlayer.start();
                                            mPlaying = true;
                                            mPlayingButton = playPauseButton;
                                            mPlayingKey = cassette.getKey();
                                            playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxArchiveActivity.this, R.drawable.box_pause_button));
                                            return;
                                        }

                                        try {
                                            mPlayer = new MediaPlayer();
                                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            mPlayer.setDataSource(
                                                    CassetteBoxArchiveActivity.this,
                                                    model.getSong().getDataPath());
                                            mPlayer.prepare();
                                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mp) {
                                                    mp.stop();
                                                    playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                            CassetteBoxArchiveActivity.this, R.drawable.box_play_button));
                                                }
                                            });
                                            mPlayer.start();
                                            mPlaying = true;
                                            mPlayingRow = tag;
                                            mPlayingButton = playPauseButton;
                                            mPlayingKey = cassette.getKey();
                                            playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxArchiveActivity.this, R.drawable.box_pause_button));
                                        } catch (IOException e) {
                                            Log.w(CassetteBoxArchiveActivity.this.getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                        }
                                    }
                                }

                                @Override
                                public void onFail(String error) {
                                    Log.w(getString(R.string.hb_log_tag), error);
                                }
                            });
                        }

                        @Override
                        public void onFail(String error) {
                            Log.w(getString(R.string.hb_log_tag), error);
                        }
                    });
                    break;

                case R.id.cassette_cell_disclosure:
                    final ImageButton disclosureButton = (ImageButton) v;
                    disclosureButton.setImageDrawable(
                            ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                    R.drawable.disclosure_blue));
                    if (mSelectedDisclosureButton != null) mSelectedDisclosureButton.setImageDrawable(
                            ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                    R.drawable.disclosure_right));
                    mSelectedDisclosureButton = disclosureButton;
                    mSelectedRow = tag;
                    Intent intent = new Intent(CassetteBoxArchiveActivity.this, TrackCassetteTabActivity.class);
                    intent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                    intent.putExtra(getString(R.string.archived_extra_bool), true);
                    startActivity(intent);
                    break;
                case R.id.cassette_cell_delete_button:
                    if (mEditingRow == tag) {
                            Intent deleteIntent = new Intent(CassetteBoxArchiveActivity.this, ConfirmDeleteActivity.class);
                            deleteIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                            deleteIntent.putExtra(getString(R.string.cassette_extra_name), cassette.getCassetteModel().getName());
                            startActivity(deleteIntent);
                    }
                    break;
                case R.id.cassette_cell_archive_button:
                    if (mEditingRow == tag) {
                            Intent archiveIntent = new Intent(CassetteBoxArchiveActivity.this, ConfirmUnarchiveActivity.class);
                            archiveIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                            archiveIntent.putExtra(getString(R.string.cassette_extra_name), cassette.getCassetteModel().getName());
                            startActivity(archiveIntent);
                    }
                    break;
                case R.id.cassette_cell_pick:;

                    Intent detailsIntent = new Intent(CassetteBoxArchiveActivity.this, CassetteDetailActivity.class);
                    detailsIntent.putExtra(getString(R.string.cassette_extra), cassette);
                    startActivity(detailsIntent);
                    break;
                default:
                    break;
            }
        }
    }

    public class CassetteAdapter extends RecyclerView.Adapter<CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder> implements View.OnClickListener {

        private ArrayList<Cassette> mCassettes;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView album, pick;
            public HBTextView songText, dateText, empty, archiveLabel;
            public ImageButton playPause, disclosure;
            public ProgressBar progress;
            public FrameLayout archive, delete;
            public LinearLayout mainLayout;

            public ViewHolder(View view) {
                super(view);
                album = view.findViewById(R.id.cassette_cell_album_art);
                pick = view.findViewById(R.id.cassette_cell_pick);
                songText = view.findViewById(R.id.cassette_cell_name);
                dateText = view.findViewById(R.id.cassette_cell_date);
                playPause = view.findViewById(R.id.cassette_cell_play_pause);
                empty = view.findViewById(R.id.cassette_cell_empty);
                disclosure = view.findViewById(R.id.cassette_cell_disclosure);
                progress = view.findViewById(R.id.cassette_cell_progress);
                archive = view.findViewById(R.id.cassette_cell_archive_button);
                delete = view.findViewById(R.id.cassette_cell_delete_button);
                mainLayout = view.findViewById(R.id.cassette_cell_main_layout);
                archiveLabel = view.findViewById(R.id.cassette_cell_archive_label);
            }
        }

        public CassetteAdapter(ArrayList<Cassette> cassettes) {
            this.mCassettes = cassettes;
        }

        @Override
        @NonNull
        public CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cassette_grid_item, parent, false);

            return new CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder holder, int position) {
            if (position > mCassettes.size() - 1) {
                Log.e(getString(R.string.hb_log_tag), "Adapter position greater than cassette count!");
                holder.empty.setVisibility(View.VISIBLE);
                return;
            } else {
                holder.empty.setVisibility(View.GONE);
            }

            final Cassette cassette = mCassettes.get(position);
            CassetteModel model = cassette.getCassetteModel();

            if (model == null) {
                Log.e(getString(R.string.hb_log_tag), "Null model for cassette: " + cassette.getKey());
                return;
            }

            holder.mainLayout.setTag(position);

            final CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder finalHolder = holder;

            model.setCoverArt(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    try {
                        Glide.with(CassetteBoxArchiveActivity.this)
                                .load(cassette.getCassetteModel().getCoverArtURL())
                                .into(finalHolder.album);
                    } catch (Exception e) {
                        Log.w(getString(R.string.hb_log_tag), e);
                    }
                }

                @Override
                public void onFail(String error) {
                    Log.w(CassetteBoxArchiveActivity.this.getString(R.string.hb_log_tag), error);
                }
            });


            holder.archiveLabel.setText(getString(R.string.unarchive));

            holder.songText.setText("#" + cassette.getNumber() + " " + model.getName());

            long dateFound = new Date().getTime();

            try {
                dateFound = mUser.getDateFoundByCassetteKey(cassette.getKey());
                String dateString = DateFormat.format("MMM d, yyyy", dateFound).toString();
                holder.dateText.setText(dateString);
            } catch (Exception e) {
                Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }

            // TODO: Set flag if (cassette.flagged)

            boolean hidden = false;
            try {
                boolean inCassetteBox = mUser.inCassetteBoxByCassetteKey(cassette.getKey());
                hidden = !inCassetteBox;
            } catch (Exception e) {
                Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
            }

            if (cassette.isHidden()) {
                holder.pick.setImageDrawable(ContextCompat.getDrawable(CassetteBoxArchiveActivity.this, R.drawable.pick_green));
            } else {
                holder.pick.setImageDrawable(ContextCompat.getDrawable(CassetteBoxArchiveActivity.this, R.drawable.pick_char));
            }

            if (mPlayingKey.equals(cassette.getKey()) && mPlaying) {
                holder.playPause.setImageDrawable(ContextCompat.getDrawable(
                        CassetteBoxArchiveActivity.this, R.drawable.box_pause_button));
                mPlayingKey = cassette.getKey();
            } else {
                holder.playPause.setImageDrawable(ContextCompat.getDrawable(
                        CassetteBoxArchiveActivity.this, R.drawable.box_play_button));
            }
            holder.playPause.setTag(position);

            holder.disclosure.setTag(position);
            if (cassette.getKey().equals(mPlayingKey)) {
                holder.disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                R.drawable.disclosure_blue));
            } else if (cassette.getKey().equals(mSelectedKey)) {
                holder.disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                R.drawable.disclosure_right));
                mSelectedDisclosureButton = holder.disclosure;
            } else {
                holder.disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                R.drawable.disclosure_char));
            }

            holder.progress.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(CassetteBoxArchiveActivity.this, R.color.hbBlue),
                    android.graphics.PorterDuff.Mode.MULTIPLY);

            if (mLoading && mLoadingKey.equals(cassette.getKey())) {
                holder.playPause.setVisibility(View.GONE);
                holder.progress.setVisibility(View.VISIBLE);
            } else {
                holder.playPause.setVisibility(View.VISIBLE);
                holder.progress.setVisibility(View.GONE);
            }

            if (position == mEditingRow) {
                float distance = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 160,
                        getResources().getDisplayMetrics()
                );
                holder.mainLayout.setTranslationX(-distance);
            } else {
                holder.mainLayout.setTranslationX(0);
            }

            holder.archive.setTag(position);
            holder.delete.setTag(position);
            holder.pick.setTag(position);

            holder.songText.setOnClickListener(this);
            holder.dateText.setOnClickListener(this);
            holder.pick.setOnClickListener(this);
            holder.disclosure.setOnClickListener(this);
            holder.playPause.setOnClickListener(this);
            holder.delete.setOnClickListener(this);
            holder.archive.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            if (mCassettes.size() > 6) {
                return mCassettes.size();
            }
            return 6;
        }

        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }
            final int tag = (int) v.getTag();
            final Cassette cassette = mCassettes.get(tag);
            final CassetteModel model = cassette.getCassetteModel();

            switch (v.getId()) {
                case R.id.cassette_cell_play_pause:

                    if (mLoading) return;

                    if (mManager.isPlaying()) {
                        mManager.pause();
                    }

                    final ImageButton playPauseButton = (ImageButton) v;
                    View viewHolder = layoutManager.findViewByPosition(tag);
                    final ProgressBar progressBar = viewHolder.findViewById(R.id.cassette_cell_progress);
                    progressBar.setVisibility(View.VISIBLE);
                    playPauseButton.setVisibility(View.GONE);

                    final ImageButton disclosure = (ImageButton)
                            viewHolder.findViewById(R.id.cassette_cell_disclosure);
                    disclosure.setImageDrawable(
                            ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                    R.drawable.disclosure_blue));

                    mLoading = true;
                    mLoadingKey = "";

                    model.setSong(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {
                            model.getSong().downloadSong(new ModelPropertySetCallback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                    playPauseButton.setVisibility(View.VISIBLE);

                                    mLoading = false;
                                    mLoadingKey = cassette.getKey();

                                    if (mPlaying && mPlayingKey.equals(cassette.getKey())) {
                                        try {
                                            mPlayer.pause();
                                            mPlaying = false;
                                            playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxArchiveActivity.this, R.drawable.box_play_button));
                                        } catch (IllegalStateException e) {
                                            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                        }

                                    } else {

                                        if (mPlaying && mPlayer != null) {
                                            mPlayingButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxArchiveActivity.this, R.drawable.box_play_button));
                                            try {
                                                mPlayer.stop();
                                                mPlayer.release();
                                            } catch (Exception e) {
                                                Log.e(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                            }
                                            mPlaying = false;
                                            mPlayingKey = "";
                                        }

                                        if (mPlayingKey.equals(cassette.getKey()) && mPlayer != null) {
                                            mPlayer.start();
                                            mPlaying = true;
                                            mPlayingButton = playPauseButton;
                                            mPlayingKey = cassette.getKey();
                                            playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxArchiveActivity.this, R.drawable.box_pause_button));
                                            return;
                                        }

                                        try {
                                            mPlayer = new MediaPlayer();
                                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            mPlayer.setDataSource(
                                                    CassetteBoxArchiveActivity.this,
                                                    model.getSong().getDataPath());
                                            mPlayer.prepare();
                                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mp) {
                                                    mp.stop();
                                                    playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                            CassetteBoxArchiveActivity.this, R.drawable.box_play_button));
                                                    mPlayingKey = "";
                                                    mPlaying = false;
                                                    CassetteBoxArchiveActivity.CassetteAdapter.this.notifyDataSetChanged();
                                                    CassetteBoxArchiveActivity.this.recyclerView
                                                            .setAdapter(CassetteBoxArchiveActivity.CassetteAdapter.this);
                                                }
                                            });
                                            mPlayer.start();
                                            mPlaying = true;
                                            mPlayingKey = cassette.getKey();
                                            mPlayingButton = playPauseButton;
                                            mPlayingKey = cassette.getKey();
                                            playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxArchiveActivity.this, R.drawable.box_pause_button));
                                        } catch (IOException e) {
                                            Log.w(CassetteBoxArchiveActivity.this.getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                        }
                                    }
                                }

                                @Override
                                public void onFail(String error) {
                                    progressBar.setVisibility(View.GONE);
                                    playPauseButton.setVisibility(View.VISIBLE);
                                    Log.w(getString(R.string.hb_log_tag), error);
                                }
                            });
                        }

                        @Override
                        public void onFail(String error) {
                            progressBar.setVisibility(View.GONE);
                            playPauseButton.setVisibility(View.VISIBLE);

                            mLoading = true;
                            mLoadingKey = "";

                            Log.w(getString(R.string.hb_log_tag), error);
                        }
                    });

                    break;
                case R.id.cassette_cell_disclosure:
                    final ImageButton disclosureButton = (ImageButton) v;
                    disclosureButton.setImageDrawable(
                            ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                    R.drawable.disclosure_blue));
                    if (mSelectedDisclosureButton != null) mSelectedDisclosureButton.setImageDrawable(
                            ContextCompat.getDrawable(CassetteBoxArchiveActivity.this,
                                    R.drawable.disclosure_right));
                    mSelectedDisclosureButton = disclosureButton;
                    mSelectedRow = tag;
                    Intent intent = new Intent(CassetteBoxArchiveActivity.this, TrackCassetteTabActivity.class);
                    intent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                    intent.putExtra(getString(R.string.archived_extra_bool), true);
                    startActivity(intent);
                    break;
                case R.id.cassette_cell_delete_button:
                    if (mEditingRow == tag) {
                        Intent deleteIntent = new Intent(CassetteBoxArchiveActivity.this, ConfirmDeleteActivity.class);
                        deleteIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                        deleteIntent.putExtra(getString(R.string.cassette_extra_name), cassette.getCassetteModel().getName());
                        startActivity(deleteIntent);
                    }
                    break;
                case R.id.cassette_cell_archive_button:
                    if (mEditingRow == tag) {
                        Intent archiveIntent = new Intent(CassetteBoxArchiveActivity.this, ConfirmUnarchiveActivity.class);
                        archiveIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                        archiveIntent.putExtra(getString(R.string.cassette_extra_name), cassette.getCassetteModel().getName());
                        startActivity(archiveIntent);
                    }
                    break;
                case R.id.cassette_cell_pick:;

                    Intent detailsIntent = new Intent(CassetteBoxArchiveActivity.this, CassetteDetailActivity.class);
                    detailsIntent.putExtra(getString(R.string.cassette_extra), cassette);
                    startActivity(detailsIntent);
                    break;
                default:
                    break;
            }
        }
    }

    public class CassetteItemTouchCallback extends ItemTouchHelper.SimpleCallback {
        private CassetteBoxArchiveActivity.RecyclerItemTouchHelperListener listener;

        public CassetteItemTouchCallback(int dragDirs, int swipeDirs, CassetteBoxArchiveActivity.RecyclerItemTouchHelperListener listener) {
            super(dragDirs, swipeDirs);
            this.listener = listener;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

//        @Override
//        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//            if (viewHolder != null) {
//                final View foregroundView = ((CassetteAdapter.ViewHolder) viewHolder).mainLayout;
//
//                getDefaultUIUtil().onSelected(foregroundView);
//            }
//        }
//
//        @Override
//        public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
//                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
//                                    int actionState, boolean isCurrentlyActive) {
//            final View foregroundView = ((CassetteAdapter.ViewHolder) viewHolder).mainLayout;
//            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
//                    actionState, isCurrentlyActive);
//        }
//
//        @Override
//        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//            final View foregroundView = ((CassetteAdapter.ViewHolder) viewHolder).mainLayout;
//            getDefaultUIUtil().clearView(foregroundView);
//        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            if ((int) ((CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder) viewHolder).mainLayout.getTag() >= adapter.mCassettes.size()) return;

            final View foregroundView = ((CassetteBoxArchiveActivity.CassetteAdapter.ViewHolder) viewHolder).mainLayout;
            final float scale = getResources().getDisplayMetrics().density;
            float finalDX = dX;
            if (Math.abs(finalDX) > 160 * scale && finalDX < 0) {
                finalDX = 160 * scale * -1;
            }
            if (finalDX > 0) {
                finalDX = 0;
            }
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, finalDX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }
//
//        @Override
//        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
//            return super.convertToAbsoluteDirection(flags, layoutDirection);
//        }
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
