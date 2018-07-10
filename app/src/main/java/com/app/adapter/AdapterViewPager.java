package com.app.adapter;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import com.app.fragment.FragmentBase;
import java.util.ArrayList;
import java.util.List;

public class AdapterViewPager extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener
{

    private final List<FragmentBase> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private int _CurrentFragment = 0;

    public AdapterViewPager(FragmentManager manager) {
        super(manager);
        Log.d(AdapterViewPager.class.getName(), "Contrusctor");
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public Fragment getCurrentFragment() {
        return mFragmentList.get(_CurrentFragment);
    }
    public int getCurrentNumberOfFragment() {
        return _CurrentFragment;
    }
    public void setCurrentFragment(int currentFragment) {
        this._CurrentFragment = currentFragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add((FragmentBase) fragment);
        mFragmentTitleList.add(title);
    }

    public void updateDataFragments() {
        for(FragmentBase f : mFragmentList) {
            f.clearFragmentState();
            f.updateFragmentDataAdapter(null, null);
        }
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if(_CurrentFragment==position)  return;
        updateDataFragments();
        _CurrentFragment = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
    }
}
