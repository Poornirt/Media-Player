package fragment;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.R;

import java.io.IOException;

import jdo.MediaJdo;
import listener.SendClickEventListener;

public class MediaPlayerFragment extends Fragment {

    private MediaJdo mMediaJdo;
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private Runnable mRunnable;
    private Uri uri;
    private SeekBar mSeekbar;
    private TextView mStartDurationView, mRepeatnumber;
    private ImageButton mPlayButton, mRewindButton, mForwardButton, mLoopButton, mShuffleButton;
    private SendClickEventListener mSendClickEventListener;
    private boolean mIsSongPrepared;
    String TAG = "MediaPlayerFragment";
    private int mMinutes = 0, mSeconds = 0;
    private int mCurrentAudioPosition;
    private boolean mIsLooped, mIsLoopAll, mIsShuffled;


    public MediaPlayerFragment(Context pContext, MediaJdo pMediaJdo) {
        mMediaJdo = pMediaJdo;
        mContext = pContext;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View lView = inflater.inflate(R.layout.media_viewpager, container, false);
        ImageView lImageView = lView.findViewById(R.id.audio_image);
        TextView lTitleView = lView.findViewById(R.id.title_name);
        mStartDurationView = lView.findViewById(R.id.start_duration);
        TextView lDurationView = lView.findViewById(R.id.total_duration);
        mSeekbar = lView.findViewById(R.id.seek_bar);
        mPlayButton = lView.findViewById(R.id.play_button);
        mRepeatnumber = lView.findViewById(R.id.repeat_times);
        mRewindButton = lView.findViewById(R.id.rewind_button);
        mForwardButton = lView.findViewById(R.id.forward_button);
        mLoopButton = lView.findViewById(R.id.loop);
        mShuffleButton = lView.findViewById(R.id.shuffle);
        Glide.with(getActivity()).load(mMediaJdo.getmImgUrl()).placeholder(R.drawable.musicplaceholder).into(lImageView);
        lTitleView.setText(mMediaJdo.getmAudioname());
        mStartDurationView.setText("00.00");
        lDurationView.setText((String.format("%02d", (mMediaJdo.getmDuration() / 1000) / 60)) + ":" + String.format("%02d", (mMediaJdo.getmDuration() / 1000) % 60));
        return lView;
    }


    /**
     * @param view is created and click events are mentioned
     * @param
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: " + mMediaJdo.getmAudioname());
        prepareAudio();
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playOrPauseAudio();
            }
        });

        mShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setShuffleCheck();
            }
        });

        mLoopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoopCheck();
            }
        });

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHandler != null) {
                    mHandler.removeCallbacks(mRunnable);
                }
                mIsSongPrepared = false;
                if (mIsShuffled) {
                    mSendClickEventListener.shuffleClick();
                } else {
                    mSendClickEventListener.sendClickEvent(true, false, false);
                }
            }
        });

        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHandler != null) {
                    mHandler.removeCallbacks(mRunnable);
                }
                mIsSongPrepared = false;
                if (mIsShuffled) {
                    mSendClickEventListener.shuffleClick();
                } else {
                    mSendClickEventListener.sendClickEvent(false, true, false);
                }

            }
        });


        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d(TAG, "onProgressChanged: progress " + i);
                int lSec = i;
                mMinutes = lSec / 60;
                mSeconds = lSec % 60;
                mStartDurationView.setText(String.format("%02d", mMinutes) + ":" + String.format("%02d", mSeconds));
                if (b) {
                    mHandler.removeCallbacks(mRunnable);
                    mSeekbar.setProgress(i);
                }
                mCurrentAudioPosition = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("Seekbarprogress", "start" + mSeekbar.getProgress());
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaPlayer.seekTo(mCurrentAudioPosition * 1000);
                mHandler.postDelayed(mRunnable, 0);
                Log.d("Seekbarprogress", "stop" + seekBar.getProgress());
            }
        });



    }


    private void setShuffleCheck() {
        if (!mIsShuffled) {
            mShuffleButton.setColorFilter(Color.argb(255, 156, 0, 0));
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (mIsShuffled) {
                        mSendClickEventListener.shuffleClick();
                    } else {
                        mSendClickEventListener.sendClickEvent(true, false, false);
                    }
                }
            });
            mIsShuffled = true;
        } else {
            mShuffleButton.setColorFilter(Color.BLACK);
            mIsShuffled = false;
        }
    }

    /**
     * To check the loop mode
     */
    private void setLoopCheck() {
        if (mIsLoopAll) {
            mLoopButton.setColorFilter(Color.BLACK);
            mRepeatnumber.setVisibility(View.GONE);
            mMediaPlayer.setLooping(false);
            mIsLoopAll = false;
            mIsLooped = false;
        } else {
            if (mIsLooped) {
                mLoopButton.setColorFilter(Color.argb(255, 156, 0, 0));
                mRepeatnumber.setVisibility(View.VISIBLE);
                mMediaPlayer.setLooping(true);
                mIsLoopAll = true;
            } else {
                mLoopButton.setColorFilter(Color.argb(255, 156, 0, 0));
                mMediaPlayer.setLooping(false);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (mIsShuffled) {
                            mSendClickEventListener.shuffleClick();
                        } else {
                            mSendClickEventListener.sendClickEvent(true, false, true);
                        }
                    }
                });
                mIsLooped = true;
            }
        }
    }

    /**
     * update the progress of seekbar
     */
    public void updateSeekbarProgress() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    Log.d(TAG, "run: " + mMediaPlayer.getCurrentPosition());
                    mSeekbar.setProgress(mMediaPlayer.getCurrentPosition() / 1000);
                    mHandler.postDelayed(this, 0);
                }
            }
        };
        mHandler.removeCallbacks(mRunnable);
        Handler lHandler = new Handler();
        lHandler.postDelayed(mRunnable, 0);
    }


    /**
     * play or pause the audio
     */
    private void playOrPauseAudio() {
        if (!mIsSongPrepared) {
            prepareAudio();
        }
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayButton.setImageResource(R.drawable.play);
        } else {
            mPlayButton.setImageResource(R.drawable.pause);
            mMediaPlayer.start();
            updateSeekbarProgress();
        }
    }

    private void onResumeFragment() {
        mSeekbar.setProgress(0);
        mMediaPlayer.seekTo(0);
    }


    /**
     * This method is used to initialize the media player
     */
    private void prepareAudio() {
        uri = Uri.parse(mMediaJdo.getmPath());
        Log.d(TAG, "prepareAudio: " + uri);
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mContext, uri);
            mMediaPlayer.prepare();
            mSeekbar.setMax(mMediaPlayer.getDuration() / 1000);
            mIsSongPrepared = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mPlayButton.setImageResource(R.drawable.play);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        Log.d(TAG, "onPause: " + mMediaJdo.getmAudioname());
    }


    @Override
    public void onResume() {
        super.onResume();
        setLoopCheck();
        setShuffleCheck();
        mIsSongPrepared = false;
        onResumeFragment();
        playOrPauseAudio();
        Log.d(TAG, "onResume: " + mMediaJdo.getmAudioname() + " " + mCurrentAudioPosition);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + mMediaJdo.getmAudioname());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: " + mMediaJdo.getmAudioname());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        if (context instanceof SendClickEventListener) {
            mSendClickEventListener = (SendClickEventListener) context;
        }
    }

}
