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
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;


/**
 * Created by Thomas on 05/09/2018.
 * Tablayout selection for  toots in a profile
 */

public class TabLayoutTootsFragment extends Fragment {


    private String targetedId;
    private ViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.tablayout_toots, container, false);

        TabLayout tabLayout = inflatedView.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.replies)));
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.media)));
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.pinned_toots)));
        }

        viewPager = inflatedView.findViewById(R.id.viewpager);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.targetedId = bundle.getString("targetedid", null);
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
            switch (position) {
                case 0:

                    DisplayStatusFragment displayStatusFragment = new DisplayStatusFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedid", targetedId);
                    bundle.putBoolean("showReply",false);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 1:
                    displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedid", targetedId);
                    bundle.putBoolean("showReply",true);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 2:
                    DisplayMediaFragment displayMediaFragment = new DisplayMediaFragment();
                    bundle = new Bundle();
                    bundle.putString("targetedid", targetedId);
                    displayMediaFragment.setArguments(bundle);
                    return displayMediaFragment;
                case 3:
                    displayStatusFragment = new DisplayStatusFragment();
                    bundle = new Bundle();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedid", targetedId);
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

    public ViewPager getViewPager(){
        return viewPager;
    }
}