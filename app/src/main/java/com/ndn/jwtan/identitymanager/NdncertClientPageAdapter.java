package com.ndn.jwtan.identitymanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import net.named_data.jndncert.client.RequestState;

public class NdncertClientPageAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;

    public NdncertClientPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    // Hack learned from stackoverflow to get current fragment
    // See here: https://stackoverflow.com/questions/18609261/getting-the-current-fragment-instance-in-the-viewpager
    private Fragment mCurrentFragment;
    public Fragment getCurrentFragment(){
        return mCurrentFragment;
    }
    @Override
    public void setPrimaryItem(
            ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            mCurrentFragment = (Fragment) object;
        }
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return NdncertClientLegalInfo.newInstance();
            case 1:
                return NdncertClientIdentityInput.newInstance();
            case 2:
                return NdncertClientSelectChallenge.newInstance();
            case 3:
                return UICreateOpenmHealthIDRequestSent.newInstance("Some hint", false);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
