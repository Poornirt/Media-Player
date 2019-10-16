package activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.mediaplayer.R;

import java.util.ArrayList;
import java.util.Random;

import adapter.FragmentstatePagerAdapter;
import fragment.MediaPlayerFragment;
import jdo.MediaJdo;
import listener.SendClickEventListener;

public class MediaPlayerActivity extends FragmentActivity implements SendClickEventListener {

    private ViewPager mViewpager;
    private ArrayList<MediaJdo> mMediaJdoArrayList;
    private ArrayList<Fragment> mMediaArrayListOfFragment;
    private FragmentstatePagerAdapter mFragmentstatePagerAdapter;
    private MediaPlayerFragment mMediaPlayerFragment;
    private int mPosition;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);
        mViewpager = findViewById(R.id.view_pager);
        mMediaJdoArrayList = MediaList.getmMediaJdoArrayList();
        mPosition = getIntent().getExtras().getInt("recyclerPosition");
        mMediaArrayListOfFragment = new ArrayList<>();
        for (MediaJdo mediaJdo : mMediaJdoArrayList) {
            mMediaPlayerFragment = new MediaPlayerFragment(MediaPlayerActivity.this, mediaJdo);
            mMediaArrayListOfFragment.add(mMediaPlayerFragment);
        }
        mFragmentstatePagerAdapter = new FragmentstatePagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, mMediaArrayListOfFragment);
        mViewpager.setAdapter(mFragmentstatePagerAdapter);
        mViewpager.setOffscreenPageLimit(5);
        mViewpager.setCurrentItem(mPosition);
    }


    @Override
    public void sendClickEvent(boolean pForwardevent, boolean pBackwardevent, boolean pLoopmode) {
        if (pForwardevent) {
            mPosition++;
            if (mPosition > mMediaJdoArrayList.size() - 1) {
                if (!pLoopmode)
                    mPosition = mMediaJdoArrayList.size() - 1;
                else
                    mPosition = 0;
            }
            mViewpager.setCurrentItem(mPosition, true);
        } else {
            mPosition--;
            if (mPosition < 0) {
                mPosition = 0;
            }
            mViewpager.setCurrentItem(mPosition, true);
        }
    }

    @Override
    public void shuffleClick() {
        Random lGenerateRandom = new Random();
        int lPosition = lGenerateRandom.nextInt(mMediaJdoArrayList.size());
        mViewpager.setCurrentItem(lPosition, true);
    }


}



