package fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.R;

import jdo.MediaJdo;
import listener.CurrentPositionListener;
import listener.sendEventToJobService;

import static constants.Constants.CHANGE_ICON;
import static constants.Constants.SEEK_POSITION;
import static constants.Constants.SEEK_TO_INTENT;
import static constants.Constants.SHUFFLE_INTENT;

public class MediaPlayerWithServiceFragment extends Fragment implements CurrentPositionListener {

    private MediaJdo mMediaJdo;
    private Context mContext;
    private SeekBar mSeekbar;
    private TextView lstartDurationView, mRepeatnumber;
    private ImageButton playButton, rewindButton, forwardButton, loopButton, shuffleButton;
    String TAG = "MediaPlayerFragment";
    private int lMinutes = 0, lSeconds = 0;
    private boolean isLooped, isLoopAll, isShuffled;
    private sendEventToJobService mSendEventToJobServiceListener;
    private int mCurrentAudioPosition;


    public MediaPlayerWithServiceFragment(Context pContext, MediaJdo pMediaJdo) {
        mMediaJdo = pMediaJdo;
        mContext = pContext;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View lView = inflater.inflate(R.layout.media_viewpager, container, false);
        ImageView lImageView = lView.findViewById(R.id.audio_image);
        TextView lTitleView = lView.findViewById(R.id.title_name);
        lstartDurationView = lView.findViewById(R.id.start_duration);
        TextView lDurationView = lView.findViewById(R.id.total_duration);
        mSeekbar = lView.findViewById(R.id.seek_bar);
        playButton = lView.findViewById(R.id.play_button);
        mRepeatnumber = lView.findViewById(R.id.repeat_times);
        rewindButton = lView.findViewById(R.id.rewind_button);
        forwardButton = lView.findViewById(R.id.forward_button);
        loopButton = lView.findViewById(R.id.loop);
        shuffleButton = lView.findViewById(R.id.shuffle);
        Glide.with(getActivity()).load(mMediaJdo.getmImgUrl()).placeholder(R.drawable.musicplaceholder).into(lImageView);
        lTitleView.setText(mMediaJdo.getmAudioname());
        lstartDurationView.setText("00.00");
        lDurationView.setText((String.format("%02d", (mMediaJdo.getmDuration() / 1000) / 60)) + ":" + String.format("%02d", (mMediaJdo.getmDuration() / 1000) % 60));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(lbroadcastReceiverChangeIcon, new IntentFilter(CHANGE_ICON));
        playButton.setImageResource(R.drawable.pause);
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
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendEventToJobServiceListener.playorpauseEvent();
            }
        });
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShuffled) {
                    mSendEventToJobServiceListener.shuffleClick();
                } else {
                    mSendEventToJobServiceListener.sendClickEvent(true, false, 0);
                }
            }
        });
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShuffled) {
                    mSendEventToJobServiceListener.shuffleClick();
                } else {
                    mSendEventToJobServiceListener.sendClickEvent(false, true, 0);
                }
            }
        });
        loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoopCheck();
            }
        });
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setShuffleCheck();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setLoopCheck() {
        if (isLoopAll) {
            loopButton.setColorFilter(Color.BLACK);
            mRepeatnumber.setVisibility(View.GONE);
            mSendEventToJobServiceListener.sendClickEvent(false, false, 0);
            isLoopAll = false;
            isLooped = false;
        } else {
            if (isLooped) {
                loopButton.setColorFilter(Color.argb(255, 156, 0, 0));
                mRepeatnumber.setVisibility(View.VISIBLE);
                mSendEventToJobServiceListener.sendClickEvent(false, false, 2);
                isLoopAll = true;
            } else {
                loopButton.setColorFilter(Color.argb(255, 156, 0, 0));
                mSendEventToJobServiceListener.sendClickEvent(false, false, 1);
                isLooped = true;
            }
        }
    }


    private void setShuffleCheck() {
        if (!isShuffled) {
            shuffleButton.setColorFilter(Color.argb(255, 156, 0, 0));
            Intent shuffleIntent=new Intent(SHUFFLE_INTENT);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(shuffleIntent);
            isShuffled = true;
        } else {
            shuffleButton.setColorFilter(Color.BLACK);
            isShuffled = false;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSendEventToJobServiceListener = (sendEventToJobService) context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onPause: " + mMediaJdo.getmAudioname());
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(lbroadcastReceiverChangeIcon);
    }


    private BroadcastReceiver lbroadcastReceiverChangeIcon = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean pause = intent.getExtras().getBoolean("Icon");
            if (pause) {
                playButton.setImageResource(R.drawable.play);
            } else {
                playButton.setImageResource(R.drawable.pause);
            }
        }
    };


    @Override
    public void sendCurrentPosition(int pPosition, int pAudioDuration) {
        if (mSeekbar != null) {
            mSeekbar.setMax(pAudioDuration / 1000);
            mSeekbar.setProgress(pPosition / 1000);
            mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    lMinutes = i / 60;
                    lSeconds = i % 60;
                    lstartDurationView.setText(String.format("%02d", lMinutes) + ":" + String.format("%02d", lSeconds));
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
                    Intent seekIntent = new Intent(SEEK_TO_INTENT);
                    seekIntent.putExtra(SEEK_POSITION, mCurrentAudioPosition * 1000);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(seekIntent);
                }
            });
        }

    }


}
