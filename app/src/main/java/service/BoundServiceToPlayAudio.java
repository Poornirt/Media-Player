package service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import listener.boundservicelistener.BoundServiceEventLiIstener;

public class BoundServiceToPlayAudio extends Service {

    public IBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Handler mHandler;
    private Runnable mRunnable;
    private BoundServiceEventLiIstener mBoundServiceEventLiIstener;
    private final String TAG = "BoundServiceToPlayAudio";
    public static boolean isBoundServiceRunning=false;

    public BoundServiceToPlayAudio() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mBoundServiceEventLiIstener.onComplete();
            }
        });

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: Handler for Bound service ");
                if (mMediaPlayer!=null) {
                    if (mMediaPlayer.isPlaying() && mBoundServiceEventLiIstener != null) {
                        mBoundServiceEventLiIstener.updateProgress(mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
                        mHandler.postDelayed(this, 1000);
                    }
                }
            }
        };
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        isBoundServiceRunning=true;
        return mBinder;
    }


    public void playAudio(String pAudioPath) {
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setDataSource(pAudioPath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateProgress();
    }

    public void playOrPauseAudio() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
            if (mHandler!=null) {
                mHandler.postDelayed(mRunnable,0);
            }
        }
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

    public void activity(BoundServiceEventLiIstener pBoundServiceEventLiIstener) {
        mBoundServiceEventLiIstener = pBoundServiceEventLiIstener;
    }

    public void updateProgress() {
        if (mHandler!=null){
            mHandler.removeCallbacks(mRunnable);
        }
        mHandler.postDelayed(mRunnable, 0);
    }

    public void seekPosition(int position) {
        mMediaPlayer.seekTo(position);
    }


    public class LocalBinder extends Binder {
        public BoundServiceToPlayAudio getService() {
            return BoundServiceToPlayAudio.this;
        }
    }


}
