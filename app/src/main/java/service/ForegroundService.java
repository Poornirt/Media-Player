package service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.mediaplayer.R;

import java.io.IOException;

import activity.MediaPlayerForegroundActivity;
import listener.boundservicelistener.BoundServiceEventLiIstener;

public class ForegroundService extends Service {

    private static final String NOTIFICATION_CHANNEL_ID ="123" ;
    private static final CharSequence NOTIFICATION_CHANNEL_NAME ="Foreground" ;
    private static final String NOTIFICATION_CHANNEL_DESC = "Music";
    public IBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Handler mHandler;
    private Runnable mRunnable;
    private BoundServiceEventLiIstener mBoundServiceEventLiIstener;
    private final String TAG = "BoundServiceToPlayAudio";
    private String mAudioName;

    public ForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
        return START_NOT_STICKY;
    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, MediaPlayerForegroundActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.mediaicon)
                .setContentTitle(mAudioName)
                .addAction(mMediaPlayer.isPlaying()?R.drawable.pausenotification:R.drawable.play,mMediaPlayer.isPlaying()?"playing":"pause",pendingIntent)
                .setContentText("Playing...")
                .setTicker("TICKER")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void playAudio(String pAudioPath,String pAudioName) {
        mAudioName=pAudioName;
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
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }


}
