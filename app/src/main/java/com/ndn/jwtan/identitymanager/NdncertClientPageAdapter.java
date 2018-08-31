package com.ndn.jwtan.identitymanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import net.named_data.jndncert.client.RequestState;

public class NdncertClientPageAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;
    RequestState m_state;

    public NdncertClientPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
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
