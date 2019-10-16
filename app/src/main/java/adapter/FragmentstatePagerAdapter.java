package adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class FragmentstatePagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<Fragment> mMediaArrayList;

    public FragmentstatePagerAdapter( FragmentManager fm, int behavior, ArrayList<Fragment> mMediaArrayList) {
        super(fm, behavior);
        this.mMediaArrayList = mMediaArrayList;
    }

    @Override
    public Fragment getItem(int position) {
        return mMediaArrayList.get(position);
    }

    @Override
    public int getCount() {
        return mMediaArrayList.size();
    }



}
