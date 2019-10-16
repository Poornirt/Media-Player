package activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.mediaplayer.R;

import java.util.ArrayList;
import java.util.Random;

import adapter.FragmentstatePagerAdapter;
import fragment.MediaPlayerWithServiceFragment;
import jdo.MediaJdo;
import listener.CurrentPositionListener;
import listener.sendEventToJobService;

import static constants.Constants.AUDIO_DURATION;
import static constants.Constants.AUDIO_PATH_;
import static constants.Constants.AUDIO_PATH_INTENT;
import static constants.Constants.CURRENT_POSITION_INTENT;
import static constants.Constants.LOOP_ALL_INTENT;
import static constants.Constants.LOOP_MODE;
import static constants.Constants.LOOP_MODE_INTENT;
import static constants.Constants.MEDIA_CURRENT_POSITION;
import static constants.Constants.NEXT_AUDIO;
import static constants.Constants.NEXT_AUDIO_INTENT;
import static constants.Constants.PLAY_OR_PAUSE;
import static constants.Constants.PLAY_OR_PAUSE_INTENT;
import static constants.Constants.RECYCLER_POSITION;
import static constants.Constants.SHUFFLE_INTENT_SERVICE;

public class MediaPlayerServiceActivity extends FragmentActivity implements sendEventToJobService {

    private ViewPager mViewpager;
    private ArrayList<MediaJdo> mMediaJdoArrayList;
    private ArrayList<Fragment> mMediaArrayListOfFragment;
    private FragmentstatePagerAdapter mFragmentstatePagerAdapter;
    private MediaPlayerWithServiceFragment mMediaPlayerWithServiceFragment;
    private int mPosition;
    private String TAG = "MediaPlayerActivity";
    private int mMediaCurrentPostion, mAudioDuration;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);
        mViewpager = findViewById(R.id.view_pager);
        mMediaJdoArrayList = MediaList.getmMediaJdoArrayList();
        mPosition = getIntent().getExtras().getInt(RECYCLER_POSITION);
        mMediaArrayListOfFragment = new ArrayList<>();
        for (MediaJdo mediaJdo : mMediaJdoArrayList) {
            mMediaPlayerWithServiceFragment = new MediaPlayerWithServiceFragment(MediaPlayerServiceActivity.this, mediaJdo);
            mMediaArrayListOfFragment.add(mMediaPlayerWithServiceFragment);
        }
        mFragmentstatePagerAdapter = new FragmentstatePagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, mMediaArrayListOfFragment);
        mViewpager.setAdapter(mFragmentstatePagerAdapter);
        mViewpager.setOffscreenPageLimit(5);
        mViewpager.setCurrentItem(mPosition);
        IntentFilter lIntentFilter = new IntentFilter();
        lIntentFilter.addAction(CURRENT_POSITION_INTENT);
        lIntentFilter.addAction(NEXT_AUDIO_INTENT);
        lIntentFilter.addAction(LOOP_MODE_INTENT);
        lIntentFilter.addAction(LOOP_ALL_INTENT);
        lIntentFilter.addAction(SHUFFLE_INTENT_SERVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(lbroadcastReceiverPosition, lIntentFilter);
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Intent audioPathIntent = new Intent(AUDIO_PATH_INTENT);
                audioPathIntent.putExtra(AUDIO_PATH_, mMediaJdoArrayList.get(position).getmPath());
                LocalBroadcastManager.getInstance(MediaPlayerServiceActivity.this).sendBroadcast(audioPathIntent);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    @Override
    public void playorpauseEvent() {
        Intent intent = new Intent(PLAY_OR_PAUSE_INTENT);
        intent.putExtra(PLAY_OR_PAUSE, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void sendClickEvent(boolean pForwardEventOccured, boolean pRewindEventOccured, int pLoopMode) {
        if (pForwardEventOccured) {
            int lPosition = mViewpager.getCurrentItem();
            ++lPosition;
            if (lPosition > mMediaJdoArrayList.size() - 1) {
                if (pLoopMode == 0)
                    lPosition = mMediaJdoArrayList.size() - 1;
                else if (pLoopMode == 1)
                    lPosition = 0;
                // else if (pLoopMode == 2)

            }
            mViewpager.setCurrentItem(lPosition, true);
        } else if (pRewindEventOccured) {
            int lPosition = mViewpager.getCurrentItem();
            --lPosition;
            if (lPosition < 0) {
                lPosition = 0;
            }
            mViewpager.setCurrentItem(lPosition, true);
        }
        Intent loopmodeIntent = new Intent(LOOP_MODE_INTENT);
        if (pLoopMode == 2) {
            loopmodeIntent.putExtra(LOOP_MODE, pLoopMode);
        }
        if (pLoopMode == 0) {
            loopmodeIntent.putExtra(LOOP_MODE, pLoopMode);
        }
        if (pLoopMode == 1) {
            loopmodeIntent.putExtra(LOOP_MODE, pLoopMode);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(loopmodeIntent);
    }

    @Override
    public void shuffleClick() {
        Random random = new Random();
        int lPosition = random.nextInt(mMediaJdoArrayList.size());
        mViewpager.setCurrentItem(lPosition, true);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(lbroadcastReceiverPosition);
    }

    BroadcastReceiver lbroadcastReceiverPosition = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CURRENT_POSITION_INTENT)) {
                mMediaCurrentPostion = intent.getExtras().getInt(MEDIA_CURRENT_POSITION);
                mAudioDuration = intent.getExtras().getInt(AUDIO_DURATION);
                ((CurrentPositionListener) mMediaArrayListOfFragment.get(mViewpager.getCurrentItem())).sendCurrentPosition(mMediaCurrentPostion, mAudioDuration);
            } else if (intent.getAction().equals(NEXT_AUDIO_INTENT)) {
                if (intent.getExtras().getBoolean(NEXT_AUDIO)) {
                    int lPosition = mViewpager.getCurrentItem();
                    mViewpager.setCurrentItem(++lPosition, true);
                }
            } else if (intent.getAction().equals(LOOP_ALL_INTENT)) {
                int lPosition = mViewpager.getCurrentItem();
                ++lPosition;
                if (lPosition > mMediaJdoArrayList.size() - 1) {
                    lPosition = 0;
                    mViewpager.setCurrentItem(lPosition, true);
                }
            } else if (intent.getAction().equals(SHUFFLE_INTENT_SERVICE)) {
                shuffleClick();
            }
        }
    };

}



