package com.hipsterbait.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class CassetteBoxActivity extends ImmersiveActivity implements View.OnClickListener {

    public RecyclerView recyclerView;
    public LinearLayoutManager layoutManager;
    public CassetteAdapter adapter;
    public ImageButton filterButton, archiveButton;
    public Button button1, button2, button3, button4;

    private AudioPlayerManager mManager;
    private boolean mSort = false, mPlaying = false, mLoading = false;
    private final int BUTTON_1 = 0, BUTTON_2 = 1, BUTTON_3 = 2, BUTTON_4 = 3;
    private final int MIN_DISTANCE = 150;
    private String mPlayingKey = "", mLoadingKey = "", mSelectedKey = "";
    private Button mSelectedButton;
    private ImageButton mPlayingButton;
    private User mUser;
    private MediaPlayer mPlayer;
    private int mEditingRow = -1;
    private float x1,x2;
    private ImageButton mSelectedDisclosureButton;
    private LinearLayout mEditingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cassette_box);

        mUser = ((HBApplication) getApplication()).user;

        filterButton = (ImageButton) findViewById(R.id.box_filter_button);
        archiveButton = (ImageButton) findViewById(R.id.box_archive_button);
        button1 = (Button) findViewById(R.id.box_button_1);
        button1.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.box_button_2);
        button2.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.box_button_3);
        button3.setOnClickListener(this);
        button4 = (Button) findViewById(R.id.box_button_4);
        button4.setOnClickListener(this);

        mManager = AudioPlayerManager.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.cassette_box_recycler);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CassetteAdapter(mUser.getCassettesByDate());
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

        CassetteItemTouchCallback callback = new CassetteItemTouchCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, new RecyclerItemTouchHelperListener() {
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
                    mEditingLayout = ((CassetteAdapter.ViewHolder) viewHolder).mainLayout;

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

        if (mUser.getArchivedCassettesArrayList().isEmpty()) {
            archiveButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.archive_off));
        } else {
            archiveButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.archive_on));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mPlayer != null) mPlayer.release();
    }

    public void onClick(View v) {
        mEditingRow = -1;
        mEditingLayout = null;

        mSelectedButton.setBackgroundResource(R.color.hbCharcoal);

        mSelectedButton = (HBButton) v;
        mSelectedButton.setBackgroundResource(R.color.hbGreen);

        adapter.notifyDataSetChanged();

        switch (Integer.parseInt((String) mSelectedButton.getTag())) {
            case BUTTON_1:
            default:
                if (mSort) {
                    adapter = new CassetteAdapter(mUser.getCassettesByDate());
                } else {
                    adapter = new CassetteAdapter(mUser.getCassettesByDate());
                }
                break;
            case BUTTON_2:
                if (mSort) {
                    adapter = new CassetteAdapter(mUser.getCassettesByRating());
                } else {
                    adapter = new CassetteAdapter(mUser.getHiddenCassettes());
                }
                break;
            case BUTTON_3:
                if (mSort) {
                    adapter = new CassetteAdapter(mUser.getCassettesByPoints());
                } else {
                    adapter = new CassetteAdapter(mUser.getUnhiddenCassettes());
                }
                break;
            case BUTTON_4:
                if (mSort) {
                    adapter = new CassetteAdapter(mUser.getCassettesByNumber());
                } else {
                    adapter = new CassetteAdapter(new ArrayList<Cassette>());
                }
                break;
        }

        recyclerView.setAdapter(adapter);
    }

    public void filterButtonTapped(View v) {
        onClick(button1);

        if (mSort) {
            mSort = false;

            button1.setText("All");
            button2.setText("Hidden");
            button3.setText("Holding");
            button4.setText("Flagged");

        } else {
            mSort = true;

            button1.setText("Date");
            button2.setText("Rating");
            button3.setText("Points");
            button4.setText("Number");
        }
    }

    public void homeButtonTapped(View v) {
        Intent intent = new Intent(CassetteBoxActivity.this, UserAccountActivity.class);
        startActivity(intent);
        finish();
    }

    public void archiveButtonTapped(View v) {
        Intent intent = new Intent(CassetteBoxActivity.this, CassetteBoxArchiveActivity.class);
        startActivity(intent);
    }

    public class CassetteAdapter extends RecyclerView.Adapter<CassetteAdapter.ViewHolder> implements View.OnClickListener {

        private ArrayList<Cassette> mCassettes;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView album, pick;
            public HBTextView songText, dateText, empty;
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
            }
        }

        public CassetteAdapter(ArrayList<Cassette> cassettes) {
            this.mCassettes = cassettes;
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cassette_grid_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

            final ViewHolder finalHolder = holder;

            model.setCoverArt(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    try {
                        Glide.with(CassetteBoxActivity.this)
                                .load(cassette.getCassetteModel().getCoverArtURL())
                                .into(finalHolder.album);
                    } catch (Exception e) {
                        Log.w(getString(R.string.hb_log_tag), e);
                    }
                }

                @Override
                public void onFail(String error) {
                    Log.w(CassetteBoxActivity.this.getString(R.string.hb_log_tag), error);
                }
            });

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

            if (hidden) {
                if (cassette.isHidden()) {
                    holder.pick.setImageDrawable(ContextCompat.getDrawable(CassetteBoxActivity.this, R.drawable.pick_green));
                } else {
                    holder.pick.setImageDrawable(ContextCompat.getDrawable(CassetteBoxActivity.this, R.drawable.pick_char));
                }
            } else {
                holder.pick.setImageDrawable(ContextCompat.getDrawable(CassetteBoxActivity.this, R.drawable.pick_blue));

                long diff = Math.abs(dateFound - new Date().getTime());
                if (diff > 7 * 24 * 60 * 60 * 1000) {
                    holder.pick.setImageDrawable(ContextCompat.getDrawable(CassetteBoxActivity.this, R.drawable.pick_orange));
                }
            }

            if (mPlayingKey.equals(cassette.getKey()) && mPlaying) {
                holder.playPause.setImageDrawable(ContextCompat.getDrawable(
                        CassetteBoxActivity.this, R.drawable.box_pause_button));
                mPlayingKey = cassette.getKey();
            } else {
                holder.playPause.setImageDrawable(ContextCompat.getDrawable(
                        CassetteBoxActivity.this, R.drawable.box_play_button));
            }
            holder.playPause.setTag(position);

            holder.disclosure.setTag(position);
            if (cassette.getKey().equals(mPlayingKey)) {
                holder.disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxActivity.this,
                                R.drawable.disclosure_blue));
            } else if (cassette.getKey().equals(mSelectedKey)) {
                holder.disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxActivity.this,
                                R.drawable.disclosure_right));
                mSelectedDisclosureButton = holder.disclosure;
            } else {
                holder.disclosure.setImageDrawable(
                        ContextCompat.getDrawable(CassetteBoxActivity.this,
                                R.drawable.disclosure_char));
            }

            holder.progress.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(CassetteBoxActivity.this, R.color.hbBlue),
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
                            ContextCompat.getDrawable(CassetteBoxActivity.this,
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
                                                    CassetteBoxActivity.this, R.drawable.box_play_button));
                                        } catch (IllegalStateException e) {
                                            Log.w(getString(R.string.hb_log_tag), e.getLocalizedMessage());
                                        }

                                    } else {

                                        if (mPlaying && mPlayer != null) {
                                            mPlayingButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxActivity.this, R.drawable.box_play_button));
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
                                                    CassetteBoxActivity.this, R.drawable.box_pause_button));
                                            return;
                                        }

                                        try {
                                            mPlayer = new MediaPlayer();
                                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                            mPlayer.setDataSource(
                                                    CassetteBoxActivity.this,
                                                    model.getSong().getDataPath());
                                            mPlayer.prepare();
                                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mp) {
                                                    mp.stop();
                                                    playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                            CassetteBoxActivity.this, R.drawable.box_play_button));
                                                    mPlayingKey = "";
                                                    mPlaying = false;
                                                    CassetteAdapter.this.notifyDataSetChanged();
                                                    CassetteBoxActivity.this.recyclerView
                                                            .setAdapter(CassetteAdapter.this);
                                                }
                                            });
                                            mPlayer.start();
                                            mPlaying = true;
                                            mPlayingKey = cassette.getKey();
                                            mPlayingButton = playPauseButton;
                                            mPlayingKey = cassette.getKey();
                                            playPauseButton.setImageDrawable(ContextCompat.getDrawable(
                                                    CassetteBoxActivity.this, R.drawable.box_pause_button));
                                        } catch (IOException e) {
                                            Log.w(CassetteBoxActivity.this.getString(R.string.hb_log_tag), e.getLocalizedMessage());
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
                    if (mSelectedDisclosureButton != null) {
                        mSelectedDisclosureButton.setImageDrawable(ContextCompat.getDrawable(CassetteBoxActivity.this,
                                R.drawable.disclosure_char));
                    }
                    final ImageButton disclosureButton = (ImageButton) v;
                    disclosureButton.setImageDrawable(
                            ContextCompat.getDrawable(CassetteBoxActivity.this,
                                    R.drawable.disclosure_right));
                    mSelectedDisclosureButton = disclosureButton;
                    mSelectedKey = cassette.getKey();
                    Intent intent = new Intent(CassetteBoxActivity.this, TrackCassetteTabActivity.class);
                    intent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                    startActivity(intent);
                    break;
                case R.id.cassette_cell_delete_button:
                    if (mEditingRow == tag) {
                        if (mUser.isHoldingCassette(cassette.getKey())) {
                            Intent deleteIntent = new Intent(CassetteBoxActivity.this, DeleteErrorActivity.class);
                            startActivity(deleteIntent);
                        } else {
                            Intent deleteIntent = new Intent(CassetteBoxActivity.this, ConfirmDeleteActivity.class);
                            deleteIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                            deleteIntent.putExtra(getString(R.string.cassette_extra_name), cassette.getCassetteModel().getName());
                            startActivity(deleteIntent);
                        }
                    }
                    break;
                case R.id.cassette_cell_archive_button:
                    if (mEditingRow == tag) {
                        if (mUser.isHoldingCassette(cassette.getKey())) {
                            Intent archiveIntent = new Intent(CassetteBoxActivity.this, ArchiveErrorActivity.class);
                            startActivity(archiveIntent);
                        } else {
                            Intent archiveIntent = new Intent(CassetteBoxActivity.this, ConfirmArchiveActivity.class);
                            archiveIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                            archiveIntent.putExtra(getString(R.string.cassette_extra_name), cassette.getCassetteModel().getName());
                            startActivity(archiveIntent);
                        }
                    }
                    break;
                case R.id.cassette_cell_pick:
                    boolean hidden = !mUser.inCassetteBoxByCassetteKey(cassette.getKey());

                    if (hidden) {
                        if (mSelectedDisclosureButton != null) {
                            mSelectedDisclosureButton.setImageDrawable(ContextCompat.getDrawable(CassetteBoxActivity.this,
                                    R.drawable.disclosure_char));
                        }
                        final ImageButton pick = (ImageButton) v;
                        final ViewGroup parent = (ViewGroup) pick.getParent().getParent();
                        final ImageButton disclosureSelected = (ImageButton)
                                parent.findViewById(R.id.cassette_cell_disclosure);

                        disclosureSelected.setImageDrawable(
                                ContextCompat.getDrawable(CassetteBoxActivity.this,
                                        R.drawable.disclosure_right));
                        mSelectedDisclosureButton = disclosureSelected;
                        mSelectedKey = cassette.getKey();
                        Intent newIntent = new Intent(CassetteBoxActivity.this, TrackCassetteTabActivity.class);
                        newIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                        startActivity(newIntent);
                    } else {
                        Intent hideIntent = new Intent(CassetteBoxActivity.this, BaitSelectActivity.class);
                        hideIntent.putExtra(getString(R.string.cassette_extra_key), cassette.getKey());
                        startActivity(hideIntent);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public class CassetteItemTouchCallback extends ItemTouchHelper.SimpleCallback {
        private RecyclerItemTouchHelperListener listener;

        public CassetteItemTouchCallback(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
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
            final View foregroundView = ((CassetteAdapter.ViewHolder) viewHolder).mainLayout;
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
