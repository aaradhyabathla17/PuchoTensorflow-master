package com.puchoInc.diya;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ListenViewPager extends FragmentStatePagerAdapter {
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    public ListenViewPager(@NonNull FragmentManager fm, ArrayList<Fragment> fragments)
    {
        super(fm);
        mFragments=fragments;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
