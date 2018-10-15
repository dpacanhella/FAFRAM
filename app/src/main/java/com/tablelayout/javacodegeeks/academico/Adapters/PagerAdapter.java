package com.tablelayout.javacodegeeks.academico.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.tablelayout.javacodegeeks.academico.Fragments.PresencaFragment;
import com.tablelayout.javacodegeeks.academico.Fragments.SecondFragment;

public class PagerAdapter extends FragmentPagerAdapter {

    int numberOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.numberOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                PresencaFragment tab1 = new PresencaFragment();
                return tab1;
            case 1:
                SecondFragment tab2 = new SecondFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
