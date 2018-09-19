package com.hipsterbait.android.other;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.hipsterbait.android.R;
import com.hipsterbait.android.models.CassetteModel;
import com.hipsterbait.android.models.ModelPropertySetCallback;
import com.hipsterbait.android.models.Play;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AudioPlayerManager {

    private Context mContext;

    private Handler mProgressHandler;
    private int mIndex = 0, mAudioDuration;
    private static AudioPlayerManager singleton = null;
    private boolean mPlaying = false, mPaused = false, mAutoplay = true, mRemotePlayback = false;
    private ArrayList<CassetteModel> mPlaylist;
    private MediaPlayer mPlayer;
    private ArrayList<AudioPlayerListener> mListeners;

    public static AudioPlayerManager getInstance() {
        if (singleton == null) {
            singleton = new AudioPlayerManager();
            singleton.mPlaylist = new ArrayList<>();
            singleton.mListeners = new ArrayList<>();
            singleton.mContext = HBApplication.getInstance();
        }
        return singleton;
    }

    public void addToPlaylist(CassetteModel model) {
        for (CassetteModel cassetteInPlaylist : mPlaylist) {
            if (model.getKey().equals(cassetteInPlaylist.getKey())) return;
        }
        final CassetteModel finalModel = model;
        finalModel.setSong(new ModelPropertySetCallback() {
            @Override
            public void onSuccess() {
                finalModel.getSong().setBand(null);
            }

            @Override
            public void onFail(String error) {
                Log.w(mContext.getString(R.string.hb_log_tag), error);
            }
        });
        mPlaylist.add(model);
    }

    public void removeFromPlaylist(CassetteModel model) {
        CassetteModel result = null;
        for (CassetteModel cassetteInPlaylist : mPlaylist) {
            if (model.getKey().equals(cassetteInPlaylist.getKey())) {
                result = cassetteInPlaylist;
            }
        }
        if (result != null) mPlaylist.remove(result);
    }

    public void play() {
        if (mPlaylist.isEmpty()) return;
        if (mPlaying) return;

        final CassetteModel model = mPlaylist.get(mIndex);

        if (mPaused) {
            if (mPlayer != null) {
                mPlayer.start();
                mPaused = false;
                mPlaying = true;
                mProgressHandler = new Handler();
                mProgressHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (AudioPlayerListener listener : mListeners) {
                            if (mPlayer != null)
                                listener.progress(mPlayer.getCurrentPosition() / (float) mAudioDuration);
                        }
                        mProgressHandler.postDelayed(this, 100);
                    }
                }, 100);

                for (AudioPlayerListener listener : mListeners) {
                    listener.played();
                }
            }
        } else {
            model.setSong(new ModelPropertySetCallback() {
                @Override
                public void onSuccess() {
                    model.getSong().downloadSong(new ModelPropertySetCallback() {
                        @Override
                        public void onSuccess() {

                            if (mPlayer != null) {
                                mPlayer.stop();
                                mPlayer.release();
                                mPlayer = null;
                            }

                            try {
                                mPlayer = new MediaPlayer();
                                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mPlayer.setDataSource(mContext, model.getSong().getDataPath());
                                mPlayer.prepare();
                                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {

                                        stop();

                                        if (mAutoplay) {
                                            if (mPlaylist.size() > mIndex + 1) {
                                                mIndex += 1;
                                                play();
                                            } else {
                                                mIndex = 0;
                                            }
                                        }
                                    }
                                });

                                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                mmr.setDataSource(mContext, model.getSong().getDataPath());
                                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                mAudioDuration = Integer.parseInt(durationStr);

                                mPlayer.start();
                                mPaused = false;
                                mPlaying = true;

                                mProgressHandler = new Handler();
                                mProgressHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (AudioPlayerListener listener : mListeners) {
                                            if (mPlayer != null)
                                                listener.progress(mPlayer.getCurrentPosition() / (float) mAudioDuration);
                                        }
                                        mProgressHandler.postDelayed(this, 100);
                                    }
                                }, 100);

                                for (AudioPlayerListener listener : mListeners) {
                                    listener.played();
                                }
                            } catch (IOException e) {
                                Log.w(mContext.getString(R.string.hb_log_tag), e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onFail(String error) {
                            Log.w(mContext.getString(R.string.hb_log_tag), error);
                        }
                    });
                }

                @Override
                public void onFail(String error) {
                    Log.w(mContext.getString(R.string.hb_log_tag), error);
                }
            });
        }
    }

    public void pause() {
        if (mRemotePlayback) return;
        if (mPlaying == false) return;

        if (mPlayer == null) {
            Log.w(mContext.getString(R.string.hb_log_tag), "ERROR: MediaPlayer not initialized");
            return;
        }

        mPlayer.pause();
        mPlaying = false;
        mPaused = true;
        mProgressHandler.removeCallbacksAndMessages(null);

        for (AudioPlayerListener listener : mListeners) {
            listener.paused();
        }
    }

    public void stop() {
        if (mRemotePlayback) return;

        if (mPlayer == null) {
            Log.w(mContext.getString(R.string.hb_log_tag), "ERROR: MediaPlayer not initialized");
            return;
        }

        mPlayer.stop();
        mPlaying = false;
        mPaused = false;
        mProgressHandler.removeCallbacksAndMessages(null);

        for (AudioPlayerListener listener : mListeners) {
            listener.stopped();
        }
    }

    public void next() {
        if (mRemotePlayback) return;

        stop();

        if (mPlaylist.size() > mIndex + 1) {
            mIndex += 1;
            play();
        } else {
            mIndex = 0;
            for (AudioPlayerListener listener : mListeners) {
                listener.stopped();
            }
        }
    }

    public void prev() {
        if (mRemotePlayback) return;

        stop();

        if (mPlayer.getCurrentPosition() > 10 * 1000) {
            play();
        } else {
            mIndex -= 1;
            if (mIndex < 0) {
                mIndex = 0;
            } else {
                play();
            }
        }
    }

    public boolean isPlaying() { return mPlaying; }

    public void addListener(AudioPlayerListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(AudioPlayerListener listener) {
        mListeners.remove(listener);
    }

    public void removeAllListeners() {
        mListeners = new ArrayList<>();
    }

    public ArrayList<CassetteModel> getPlaylist() { return mPlaylist; }

    public int getIndex() { return mIndex; }

    public void setIndex(int index) { mIndex = index; }

    public void setAutoplay(boolean autoplay) { mAutoplay = autoplay; }

    public void shuffle() { Collections.shuffle(mPlaylist); }

    public void sortByRating() { Collections.sort(mPlaylist, new Comparator<CassetteModel>() {
        @Override
        public int compare(CassetteModel cassetteModel, CassetteModel t1) {
            return t1.getSong().getAverageRating() - cassetteModel.getSong().getAverageRating();
        }
    }); }

    public void moveUp(int index) {
        if (index < 1) {
            return;
        }
        if (index > mPlaylist.size() - 1) {
            return;
        }

        int newIndex = index - 1;

        if (mIndex == index) {
            mIndex = newIndex;
        }
        if (mIndex == newIndex) {
            mIndex = index;
        }

        Collections.swap(mPlaylist, index, newIndex);
    }

    public void moveDown(int index) {
        if (index > mPlaylist.size() - 2) {
            return;
        }
        if (index < 0) {
            return;
        }

        int newIndex = index + 1;

        if (mIndex == index) {
            mIndex = newIndex;
        }
        if (mIndex == newIndex) {
            mIndex = index;
        }

        Collections.swap(mPlaylist, index, newIndex);
    }

    public void setPlaylist(ArrayList<CassetteModel> playlist) { mPlaylist = playlist; }

    public interface AudioPlayerListener {
        void played();
        void paused();
        void stopped();
        void progress(float progress);
    }
}
