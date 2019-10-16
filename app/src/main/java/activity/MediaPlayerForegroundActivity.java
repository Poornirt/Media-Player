package activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.mediaplayer.R;

import java.util.ArrayList;
import java.util.Random;

import adapter.FragmentstatePagerAdapter;
import fragment.MediaPlayerForegroundServiceFragment;
import jdo.MediaJdo;
import listener.boundservicelistener.BoundServiceEventLiIstener;
import listener.boundservicelistener.SendPlayPauseEventListener;
import listener.boundservicelistener.UpdateProgressinFragmentListener;
import service.ForegroundService;

import static constants.Constants.AUDIO_PATH;
import static constants.Constants.RECYCLER_POSITION;

public class MediaPlayerForegroundActivity extends FragmentActivity implements SendPlayPauseEventListener, BoundServiceEventLiIstener {

    private ViewPager mViewPager;
    private ArrayList<MediaJdo> mMediaJdoArrayList;
    private ArrayList<Fragment> mMediaArrayListOfFragment;
    private FragmentstatePagerAdapter mFragmentstatePagerAdapter;
    private MediaPlayerForegroundServiceFragment mMediaPlayerForegroundServiceFragment;
    private int mPosition;
    private String TAG = "MediaPlayerBoundActivity";
    private ForegroundService mForegroundService;
    private int mLoopMode;
    private boolean mShuffleMode;
    private Intent foregroundIntent;
    private UpdateProgressinFragmentListener mUpdateProgressinFragmentListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);
        mViewPager = findViewById(R.id.view_pager);
        mMediaJdoArrayList = MediaList.getmMediaJdoArrayList();
        mPosition = getIntent().getExtras().getInt(RECYCLER_POSITION);
        mMediaArrayListOfFragment = new ArrayList<>();
        for (MediaJdo mediaJdo : mMediaJdoArrayList) {
            mMediaPlayerForegroundServiceFragment = new MediaPlayerForegroundServiceFragment(MediaPlayerForegroundActivity.this, mediaJdo);
            mMediaArrayListOfFragment.add(mMediaPlayerForegroundServiceFragment);
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
                mForegroundService.playAudio(mMediaJdoArrayList.get(position).getmPath(), mMediaJdoArrayList.get(position).getmAudioname());
                mForegroundService.updateProgress();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        foregroundIntent = new Intent(MediaPlayerForegroundActivity.this, ForegroundService.class);
        bindService(foregroundIntent, lServiceConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection lServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: ");
            ForegroundService.LocalBinder localBinder = (ForegroundService.LocalBinder) iBinder;
            mForegroundService = localBinder.getService();
            mForegroundService.playAudio(mMediaJdoArrayList.get(mPosition).getmPath(), mMediaJdoArrayList.get(mPosition).getmAudioname());
            mForegroundService.activity(MediaPlayerForegroundActivity.this);
            mForegroundService.updateProgress();
            ContextCompat.startForegroundService(MediaPlayerForegroundActivity.this, foregroundIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };


    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof MediaPlayerForegroundServiceFragment) {
            mUpdateProgressinFragmentListener = (UpdateProgressinFragmentListener) fragment;
        }
    }

    @Override
    public void sendPlayorPauseEvent() {
        mForegroundService.playOrPauseAudio();
    }

    @Override
    public void event(boolean forward, boolean rewind, int loopMode, boolean shuffleMode) {
        mLoopMode = loopMode;
        mShuffleMode = shuffleMode;
        int lPosition = mViewPager.getCurrentItem();
        if (!shuffleMode) {
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
            mForegroundService.loopMode(mLoopMode);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void updateProgress(int position, int audioDuration) {
        ((UpdateProgressinFragmentListener) mMediaArrayListOfFragment.get(mViewPager.getCurrentItem())).updateProgress(position, audioDuration);
    }

    @Override
    public void onComplete() {
        loopCheck();
    }

    @Override
    public void seekToevent(int pCurrentPosition) {
        mForegroundService.seekPosition(pCurrentPosition);
    }


}



