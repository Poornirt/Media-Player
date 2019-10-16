package service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import static constants.Constants.AUDIO_DURATION;
import static constants.Constants.AUDIO_PATH;
import static constants.Constants.AUDIO_PATH_INTENT;
import static constants.Constants.CURRENT_POSITION_INTENT;
import static constants.Constants.LOOP_MODE;
import static constants.Constants.LOOP_MODE_INTENT;
import static constants.Constants.MEDIA_CURRENT_POSITION;
import static constants.Constants.ON_COMPLETE;
import static constants.Constants.PLAY_OR_PAUSE_INTENT;
import static constants.Constants.SEEK_POSITION;
import static constants.Constants.SEEK_TO_INTENT;

public class IntentServiceToPlayAudio extends android.app.IntentService {

    private String TAG = "IntentServiceToPlayAudio";
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private String mAudioPath;
    private Handler mHandler;
    private Runnable mRunnable;
    private Intent intent = new Intent(CURRENT_POSITION_INTENT);
    public static boolean isServiceRunning=false;

    public IntentServiceToPlayAudio() {
        super("IntentServiceToPlayAudio");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AUDIO_PATH_INTENT);
        intentFilter.addAction(SEEK_TO_INTENT);
        intentFilter.addAction(PLAY_OR_PAUSE_INTENT);
        intentFilter.addAction(LOOP_MODE_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Intent intent = new Intent(ON_COMPLETE);
                LocalBroadcastManager.getInstance(IntentServiceToPlayAudio.this).sendBroadcast(intent);
            }
        });

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                intent.putExtra(MEDIA_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());
                intent.putExtra(AUDIO_DURATION, mMediaPlayer.getDuration());
                LocalBroadcastManager.getInstance(IntentServiceToPlayAudio.this).sendBroadcast(intent);
                mHandler.postDelayed(this, 1000);
                Log.d(TAG, "onReceive: " + mMediaPlayer.getCurrentPosition());
            }
        };
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        isServiceRunning=true;
        return super.onStartCommand(intent, flags, startId);
    }

    public void loopMode(int loopMode) {
        if (loopMode == 0) {
            mMediaPlayer.setLooping(false);
        } else if (loopMode == 1) {
            mMediaPlayer.setLooping(false);
        } else if (loopMode == 2) {
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }
        mMediaPlayer.setLooping(false);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        mAudioPath = intent.getExtras().getString(AUDIO_PATH);
        nextAudio(mAudioPath);
    }

    private void nextAudio(String pAudioPath) {
        mMediaPlayer.stop();
        mMediaPlayer.reset();

        try {
            mMediaPlayer.setDataSource(pAudioPath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateProgress();
    }

    public void playOrPauseAudio() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
            if (mHandler != null) {
                mHandler.postDelayed(mRunnable, 0);
            }
        }
    }


    public void updateProgress() {
        mHandler.postDelayed(mRunnable, 0);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AUDIO_PATH_INTENT)) {
                nextAudio(intent.getStringExtra(AUDIO_PATH));
            } else if (intent.getAction().equals(SEEK_TO_INTENT)) {
                mMediaPlayer.seekTo(intent.getExtras().getInt(SEEK_POSITION));
            } else if (intent.getAction().equals(PLAY_OR_PAUSE_INTENT)) {
                playOrPauseAudio();
            } else if (intent.getAction().equals(LOOP_MODE_INTENT)) {
                loopMode(intent.getExtras().getInt(LOOP_MODE));
            }
        }
    };


}
