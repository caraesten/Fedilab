package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2018 Thomas Schneider
 *
 * This file is a part of Mastalab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 05/09/2018.
 * Tablayout selection for  toots in a profile
 */

public class TabLayoutTootsFragment extends Fragment {


    private String targetedId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.tablayout_toots, container, false);

        TabLayout tabLayout = inflatedView.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.replies)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.media)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.pinned_toots)));

        final ViewPager viewPager = inflatedView.findViewById(R.id.viewpager);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.targetedId = bundle.getString("targetedId", null);
        }
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
            Log.v(Helper.TAG,"position: " + position);
            switch (position) {
                case 0:

                    DisplayStatusFragment displayStatusFragment = new DisplayStatusFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedId", targetedId);
                    bundle.putBoolean("showReply",false);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 1:
                    displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedId", targetedId);
                    bundle.putBoolean("showReply",true);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 2:
                    displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedId", targetedId);
                    bundle.putBoolean("showMediaOnly",true);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 3:
                    displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedId", targetedId);
                    bundle.putBoolean("showPinned",true);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                default:
                    displayStatusFragment = new DisplayStatusFragment();
                    return displayStatusFragment;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}