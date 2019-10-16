package fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.R;

import activity.MediaPlayerBoundActivity;
import activity.MediaPlayerForegroundActivity;
import jdo.MediaJdo;
import listener.boundservicelistener.SendPlayPauseEventListener;
import listener.boundservicelistener.UpdateProgressinFragmentListener;

public class MediaPlayerForegroundServiceFragment extends Fragment implements  UpdateProgressinFragmentListener {

    private MediaJdo mMediaJdo;
    private Context mContext;
    private SeekBar mSeekbar;
    private TextView mStartDurationView, mRepeatnumber;
    private ImageButton mPlayButton, mRewindButton, mForwardButton, mLoopButton, mShuffleButton;
    private int lMinutes = 0, lSeconds = 0;
    private int mCurrentAudioPosition;
    private boolean  isLooped, isLoopAll,isShuffled;
    private int loopMode;
    private boolean shuffleMode;
    String TAG = "MediaPlayerBoundServiceFragment";
    private boolean mIsPlay;
    private SendPlayPauseEventListener mPlayorpauseEvent;


    public MediaPlayerForegroundServiceFragment(Context pContext, MediaJdo pMediaJdo) {
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
        mPlayButton.setImageResource(R.drawable.pause);
        mIsPlay = true;
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playOrPauseEvent();
            }
        });
        mLoopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoopCheck();
            }
        });
        mShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setShuffleCheck();
            }
        });

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayorpauseEvent.event(true,false,loopMode,shuffleMode);
            }
        });
        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayorpauseEvent.event(false,true,loopMode,shuffleMode);
            }
        });
    }

    private void setLoopCheck() {
        if (isLoopAll) {
            mLoopButton.setColorFilter(Color.BLACK);
            mRepeatnumber.setVisibility(View.GONE);
            loopMode=0;
            isLoopAll = false;
            isLooped = false;
        } else {
            if (isLooped) {
                mLoopButton.setColorFilter(Color.argb(255, 156, 0, 0));
                mRepeatnumber.setVisibility(View.VISIBLE);
                loopMode=2;
                isLoopAll = true;
            } else {
                mLoopButton.setColorFilter(Color.argb(255, 156, 0, 0));
                loopMode=1;
                isLooped = true;
            }
        }
        mPlayorpauseEvent.event(false,false,loopMode,shuffleMode);
    }


    private void setShuffleCheck() {
        if (!isShuffled) {
            mShuffleButton.setColorFilter(Color.argb(255, 156, 0, 0));
            shuffleMode=true;
            isShuffled = true;
        } else {
            mShuffleButton.setColorFilter(Color.BLACK);
            shuffleMode=false;
            isShuffled = false;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MediaPlayerForegroundActivity) {
            mPlayorpauseEvent = (SendPlayPauseEventListener) context;
        }

    }


    public void  playOrPauseEvent(){
        if (mIsPlay) {
            mPlayButton.setImageResource(R.drawable.play);
            mIsPlay = false;
        } else {
            mPlayButton.setImageResource(R.drawable.pause);
            mIsPlay = true;
        }
        mPlayorpauseEvent.sendPlayorPauseEvent();
    }


    @Override
    public void updateProgress(int pPosition, int pAudioDuration) {
        mSeekbar.setMax(pAudioDuration / 1000);
        mSeekbar.setProgress(pPosition / 1000);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                lMinutes = i / 60;
                lSeconds = i % 60;
                mStartDurationView.setText(String.format("%02d", lMinutes) + ":" + String.format("%02d", lSeconds));
                if (b) {
                    mSeekbar.setProgress(i);
                }
                mCurrentAudioPosition = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //use listener
                mPlayorpauseEvent.seekToevent(mCurrentAudioPosition * 1000);
            }
        });
    }
}
