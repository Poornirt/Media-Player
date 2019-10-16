package service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static constants.Constants.AUDIO_DURATION;
import static constants.Constants.AUDIO_PATH;
import static constants.Constants.AUDIO_PATH_;
import static constants.Constants.AUDIO_PATH_INTENT;
import static constants.Constants.CHANGE_ICON;
import static constants.Constants.CURRENT_POSITION_INTENT;
import static constants.Constants.ICON;
import static constants.Constants.LOOP_ALL_INTENT;
import static constants.Constants.LOOP_MODE;
import static constants.Constants.LOOP_MODE_INTENT;
import static constants.Constants.MEDIA_CURRENT_POSITION;
import static constants.Constants.NEXT_AUDIO;
import static constants.Constants.NEXT_AUDIO_INTENT;
import static constants.Constants.PLAY_OR_PAUSE;
import static constants.Constants.PLAY_OR_PAUSE_INTENT;
import static constants.Constants.SEEK_POSITION;
import static constants.Constants.SEEK_TO_INTENT;
import static constants.Constants.SHUFFLE_INTENT;
import static constants.Constants.SHUFFLE_INTENT_SERVICE;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerservice extends JobService {

    private MediaPlayer mMediaPlayer;
    private String mAudiopath;
    private String TAG = "JobSchedulerservice";
    private JobParameters mJobParameters;
    private Handler mHandler;
    private Runnable mRunnable;
    private Intent PositionIntent;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        IntentFilter lIntentFilter = new IntentFilter();
        lIntentFilter.addAction(PLAY_OR_PAUSE_INTENT);
        lIntentFilter.addAction(SEEK_TO_INTENT);
        lIntentFilter.addAction(AUDIO_PATH_INTENT);
        lIntentFilter.addAction(LOOP_MODE_INTENT);
        lIntentFilter.addAction(SHUFFLE_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(lBroadcastReceiver, lIntentFilter);
        PositionIntent = new Intent(CURRENT_POSITION_INTENT);
    }


    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(TAG, "onStartJob: ");
        mJobParameters = jobParameters;
        mAudiopath = jobParameters.getExtras().getString(AUDIO_PATH);
        mMediaPlayer = new MediaPlayer();
        changeAudio(mAudiopath);
        return true;   //false(cannot be stopped)
    }


    /**
     * change the audio
     *
     * @param pPath
     */
    private void changeAudio(String pPath) {
        Log.d(TAG, "changeAudio: ");
        mAudiopath = pPath;
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            Toast.makeText(this, "reseting", Toast.LENGTH_SHORT).show();
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setDataSource(mAudiopath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkProgress();
    }

    public void checkProgress() {
        Log.d(TAG, "checkProgress: ");
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    PositionIntent.putExtra(MEDIA_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());
                }
                PositionIntent.putExtra(AUDIO_DURATION, mMediaPlayer.getDuration());
                LocalBroadcastManager.getInstance(JobSchedulerservice.this).sendBroadcast(PositionIntent);
                mHandler.postDelayed(this, 0);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(mRunnable, 0);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextAudioIntent();
            }
        });
    }


    public void destroyJob() {
        jobFinished(mJobParameters, false);
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob: ");
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        return true;   //false(cannot start)
    }

    public void playorpauseEvent() {
        Log.d(TAG, "playorpauseEvent: ");
        Intent pauseorplayIntent = new Intent(CHANGE_ICON);
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            pauseorplayIntent.putExtra(ICON, true);
        } else {
            pauseorplayIntent.putExtra(ICON, false);
            mMediaPlayer.start();
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(pauseorplayIntent);
    }

    BroadcastReceiver lBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PLAY_OR_PAUSE_INTENT)) {
                boolean eventOccured = intent.getExtras().getBoolean(PLAY_OR_PAUSE);
                if (eventOccured) {
                    playorpauseEvent();
                }
            } else if (intent.getAction().equals(SEEK_TO_INTENT)) {
                int seekPosition = intent.getExtras().getInt(SEEK_POSITION);
                mMediaPlayer.seekTo(seekPosition);
                mHandler.postDelayed(mRunnable, 0);
            } else if (intent.getAction().equals(AUDIO_PATH_INTENT)) {
                String lAudioPath = intent.getExtras().getString(AUDIO_PATH_);
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }
                changeAudio(lAudioPath);
            } else if (intent.getAction().equals(LOOP_MODE_INTENT)) {
                if (intent.getExtras().getInt(LOOP_MODE) == 0) {
                    mMediaPlayer.setLooping(false);
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            nextAudioIntent();
                        }
                    });
                } else if (intent.getExtras().getInt(LOOP_MODE) == 1) {
                    mMediaPlayer.setLooping(false);
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Intent loopAllIntent = new Intent(LOOP_ALL_INTENT);
                            LocalBroadcastManager.getInstance(JobSchedulerservice.this).sendBroadcast(loopAllIntent);
                        }
                    });
                } else if (intent.getExtras().getInt(LOOP_MODE) == 2)
                    mMediaPlayer.setLooping(true);
            } else if (intent.getAction().equals(SHUFFLE_INTENT)) {
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Intent shuffleIntent = new Intent(SHUFFLE_INTENT_SERVICE);
                        LocalBroadcastManager.getInstance(JobSchedulerservice.this).sendBroadcast(shuffleIntent);
                    }
                });
            }

        }
    };


    private void nextAudioIntent() {
        Intent changeAudioIntent = new Intent(NEXT_AUDIO_INTENT);
        changeAudioIntent.putExtra(NEXT_AUDIO, true);
        LocalBroadcastManager.getInstance(JobSchedulerservice.this).sendBroadcast(changeAudioIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(lBroadcastReceiver);
        destroyJob();
    }
}
