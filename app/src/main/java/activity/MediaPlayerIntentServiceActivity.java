package activity;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.mediaplayer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import adapter.FragmentstatePagerAdapter;
import fragment.MediaPlayerIntentServiceFragment;
import jdo.MediaJdo;
import listener.SeekToServiceListener;
import listener.boundservicelistener.BoundServiceEventLiIstener;
import listener.boundservicelistener.SendPlayPauseEventListener;
import service.IntentServiceToPlayAudio;

import static constants.Constants.AUDIO_DURATION;
import static constants.Constants.AUDIO_PATH;
import static constants.Constants.AUDIO_PATH_INTENT;
import static constants.Constants.CURRENT_POSITION_INTENT;
import static constants.Constants.LOOP_MODE;
import static constants.Constants.LOOP_MODE_INTENT;
import static constants.Constants.MEDIA_CURRENT_POSITION;
import static constants.Constants.ON_COMPLETE;
import static constants.Constants.PLAY_OR_PAUSE_INTENT;
import static constants.Constants.RECYCLER_POSITION;
import static constants.Constants.SEEK_POSITION;
import static constants.Constants.SEEK_TO_INTENT;

public class MediaPlayerIntentServiceActivity extends FragmentActivity implements SendPlayPauseEventListener {
    private ViewPager mViewPager;
    private ArrayList<MediaJdo> mMediaJdoArrayList;
    private ArrayList<Fragment> mMediaArrayListOfFragment;
    private FragmentstatePagerAdapter mFragmentstatePagerAdapter;
    private MediaPlayerIntentServiceFragment mMediaPlayerIntentServiceFragment;
    private int mPosition;
    private String TAG = "MediaPlayerIntentActivity";
    private int mLoopMode;
    private boolean mShuffleMode;
    private int mCurrentposition, mAudioDuration;
    private BoundServiceEventLiIstener mBoundServiceEventLiIstener;
    private SeekToServiceListener mSeekToServiceListener;
    private Intent serviceIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);
        Log.d(TAG, "onCreate: ");
        mViewPager = findViewById(R.id.view_pager);
        mMediaJdoArrayList = MediaList.getmMediaJdoArrayList();
        mPosition = getIntent().getExtras().getInt(RECYCLER_POSITION);
        mMediaArrayListOfFragment = new ArrayList<>();
        for (MediaJdo mediaJdo : mMediaJdoArrayList) {
            mMediaPlayerIntentServiceFragment = new MediaPlayerIntentServiceFragment(MediaPlayerIntentServiceActivity.this, mediaJdo);
            mMediaArrayListOfFragment.add(mMediaPlayerIntentServiceFragment);
        }
        mFragmentstatePagerAdapter = new FragmentstatePagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, mMediaArrayListOfFragment);
        mViewPager.setAdapter(mFragmentstatePagerAdapter);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Intent intent = new Intent(AUDIO_PATH_INTENT);
                intent.putExtra(AUDIO_PATH, mMediaJdoArrayList.get(position).getmPath());
                //serviceIntent.putExtra(AUDIO_PATH,mMediaJdoArrayList.get(position).getmPath());
                LocalBroadcastManager.getInstance(MediaPlayerIntentServiceActivity.this).sendBroadcast(intent);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CURRENT_POSITION_INTENT);
        intentFilter.addAction(ON_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        serviceIntent = new Intent(MediaPlayerIntentServiceActivity.this, IntentServiceToPlayAudio.class);
        serviceIntent.putExtra(AUDIO_PATH, mMediaJdoArrayList.get(mPosition).getmPath());

        if (IntentServiceToPlayAudio.isServiceRunning) {
            Intent intent = new Intent(AUDIO_PATH_INTENT);
            intent.putExtra(AUDIO_PATH, mMediaJdoArrayList.get(mPosition).getmPath());
            LocalBroadcastManager.getInstance(MediaPlayerIntentServiceActivity.this).sendBroadcast(intent);
        } else {
            startService(serviceIntent);
        }
    }

    public boolean isServiceRunning(Class serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof MediaPlayerIntentServiceFragment) {
            mSeekToServiceListener = (SeekToServiceListener) fragment;
        }
    }


    @Override
    public void sendPlayorPauseEvent() {
        Intent intent = new Intent(PLAY_OR_PAUSE_INTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void event(boolean forward, boolean rewind, int loopMode, boolean shuffle) {
        mLoopMode = loopMode;
        mShuffleMode = shuffle;
        int lPosition = mViewPager.getCurrentItem();
        if (!shuffle) {
            if (forward) {
                lPosition++;
                if (lPosition == mMediaArrayListOfFragment.size() - 1) {
                    lPosition = mMediaArrayListOfFragment.size() - 1;
                }
            } else if (rewind) {
                lPosition--;
                if (lPosition < 0) {
                    lPosition = 0;
                }
            }
            mViewPager.setCurrentItem(lPosition, true);
        } else {
            shuffleAudio();
        }
    }

    public void shuffleAudio() {
        Random random = new Random();
        int lPosition = random.nextInt(mMediaJdoArrayList.size() - 1);
        mViewPager.setCurrentItem(lPosition, true);
    }

    public void loopCheck() {
        if (mShuffleMode) {
            shuffleAudio();
        } else {
            if (mLoopMode == 0) {
                int lPosition = mViewPager.getCurrentItem();
                lPosition++;
                if (lPosition == mMediaArrayListOfFragment.size() - 1) {
                    lPosition = 0;
                }
                mViewPager.setCurrentItem(lPosition, true);
            } else if (mLoopMode == 1) {
                int lPosition = mViewPager.getCurrentItem();
                lPosition++;
                if (lPosition == mMediaArrayListOfFragment.size() - 1) {
                    lPosition = 0;
                }
                mViewPager.setCurrentItem(lPosition, true);
            }
            Intent loopIntent = new Intent(LOOP_MODE_INTENT);
            loopIntent.putExtra(LOOP_MODE, mLoopMode);
            LocalBroadcastManager.getInstance(this).sendBroadcast(loopIntent);
        }
    }

    @Override
    public void seekToevent(int pCurrentPosition) {
        Intent intent = new Intent(SEEK_TO_INTENT);
        intent.putExtra(SEEK_POSITION, pCurrentPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CURRENT_POSITION_INTENT)) {
                mCurrentposition = intent.getExtras().getInt(MEDIA_CURRENT_POSITION);
                mAudioDuration = intent.getExtras().getInt(AUDIO_DURATION);
                ((SeekToServiceListener) (mMediaArrayListOfFragment.get(mViewPager.getCurrentItem()))).updateProgress(mCurrentposition, mAudioDuration);
            } else if (intent.getAction().equals(ON_COMPLETE)) {
                loopCheck();
            }
        }
    };

}
