package app.fedilab.android.fragments;
/* Copyright 2018 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;


/**
 * Created by Thomas on 09/12/2018.
 * Tablayout for schedules
 */

public class TabLayoutScheduleFragment extends Fragment {



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.tablayout_toots, container, false);

        TabLayout tabLayout = inflatedView.findViewById(R.id.tabLayout);
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots_server)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots_client)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.reblog)));
        final ViewPager viewPager = inflatedView.findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter
                (getChildFragmentManager(), tabLayout.getTabCount()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

         tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return inflatedView;
    }

    /**
     * Page Adapter for settings
     */
    private class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    DisplayScheduledTootsFragment displayScheduledTootsFragment;
                    Bundle bundle;
                    if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                        displayScheduledTootsFragment = new DisplayScheduledTootsFragment();
                        bundle = new Bundle();
                        bundle.putSerializable("type", DisplayScheduledTootsFragment.typeOfSchedule.SERVER);
                        displayScheduledTootsFragment.setArguments(bundle);
                        return displayScheduledTootsFragment;
                    }else {
                        displayScheduledTootsFragment = new DisplayScheduledTootsFragment();
                        bundle = new Bundle();
                        bundle.putSerializable("type", DisplayScheduledTootsFragment.typeOfSchedule.TOOT);
                        displayScheduledTootsFragment.setArguments(bundle);
                        return displayScheduledTootsFragment;
                    }
                case 1:
                    if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                        displayScheduledTootsFragment = new DisplayScheduledTootsFragment();
                        bundle = new Bundle();
                        bundle.putSerializable("type", DisplayScheduledTootsFragment.typeOfSchedule.TOOT);
                        displayScheduledTootsFragment.setArguments(bundle);
                        return displayScheduledTootsFragment;
                    }else {
                        displayScheduledTootsFragment = new DisplayScheduledTootsFragment();
                        bundle = new Bundle();
                        bundle.putSerializable("type", DisplayScheduledTootsFragment.typeOfSchedule.BOOST);
                        displayScheduledTootsFragment.setArguments(bundle);
                        return displayScheduledTootsFragment;
                    }
                case 2:
                    displayScheduledTootsFragment = new DisplayScheduledTootsFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", DisplayScheduledTootsFragment.typeOfSchedule.BOOST);
                    displayScheduledTootsFragment.setArguments(bundle);
                    return displayScheduledTootsFragment;
                default:
                    displayScheduledTootsFragment = new DisplayScheduledTootsFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", DisplayScheduledTootsFragment.typeOfSchedule.TOOT);
                    displayScheduledTootsFragment.setArguments(bundle);
                    return displayScheduledTootsFragment;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}