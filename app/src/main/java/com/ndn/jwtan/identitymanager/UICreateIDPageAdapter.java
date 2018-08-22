package com.ndn.jwtan.identitymanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MotionEvent;

public class UICreateIDPageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    String mHintText;
    boolean mDisableExtra;

    public UICreateIDPageAdapter(FragmentManager fm, int NumOfTabs, String hint, boolean disableExtra) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.mHintText = hint;
        this.mDisableExtra = disableExtra;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new UICreateShowLegalInfo();
            case 1:
                return new UICreateInputIdName();
            case 2:
                return new UICreateSelectChallenge();
            case 3:
                return new UICreateInputSecret();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
