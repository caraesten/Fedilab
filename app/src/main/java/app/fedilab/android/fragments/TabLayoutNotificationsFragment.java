package app.fedilab.android.fragments;
/* Copyright 2017 Thomas Schneider
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import app.fedilab.android.helper.Helper;
import app.fedilab.android.helper.SwipeControledViewPager;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;


/**
 * Created by Thomas on 03/02/2019.
 * Tablayout for notifications
 */

public class TabLayoutNotificationsFragment extends Fragment {

    private Context context;
    private SwipeControledViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        View inflatedView = inflater.inflate(R.layout.tablayout_notifications, container, false);

        TabLayout tabLayout = inflatedView.findViewById(R.id.tabLayout);

        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all)));

        TabLayout.Tab tabMention = tabLayout.newTab();
        TabLayout.Tab tabFav = tabLayout.newTab();
        TabLayout.Tab tabBoost = tabLayout.newTab();
        TabLayout.Tab tabPoll = tabLayout.newTab();
        TabLayout.Tab tabFollow = tabLayout.newTab();

        tabMention.setCustomView(R.layout.tab_badge);
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
            tabFav.setCustomView(R.layout.tab_badge);
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
            tabBoost.setCustomView(R.layout.tab_badge);
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
            tabPoll.setCustomView(R.layout.tab_badge);
        tabFollow.setCustomView(R.layout.tab_badge);




        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconMention = tabMention.getCustomView().findViewById(R.id.tab_icon);
        iconMention.setImageResource(R.drawable.ic_mention_notif_tab);


        ImageView iconFav =null;
        if( tabFav.getCustomView() != null) {
            iconFav = tabFav.getCustomView().findViewById(R.id.tab_icon);
            iconFav.setImageResource(R.drawable.ic_star_notif_tab);
        }

        ImageView iconBoost =null;
        if( tabBoost.getCustomView() != null) {
            iconBoost = tabBoost.getCustomView().findViewById(R.id.tab_icon);
            iconBoost.setImageResource(R.drawable.ic_repeat_notif_tab);
        }

        ImageView iconPoll =null;
        if( tabPoll.getCustomView() != null) {
            iconPoll = tabPoll.getCustomView().findViewById(R.id.tab_icon);
            iconPoll.setImageResource(R.drawable.ic_view_list_poll_notif);
        }

        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconFollow = tabFollow.getCustomView().findViewById(R.id.tab_icon);
        iconFollow.setImageResource(R.drawable.ic_follow_notif_tab);



        tabLayout.addTab(tabMention);
        if( tabFav.getCustomView() != null)
            tabLayout.addTab(tabFav);
        if( tabBoost.getCustomView() != null)
            tabLayout.addTab(tabBoost);
        if( tabPoll.getCustomView() != null)
            tabLayout.addTab(tabPoll);
        tabLayout.addTab(tabFollow);

        if (theme == Helper.THEME_BLACK)
            iconMention.setColorFilter(ContextCompat.getColor(context, R.color.dark_icon), PorterDuff.Mode.SRC_IN);
        else
            iconMention.setColorFilter(ContextCompat.getColor(context, R.color.mastodonC4), PorterDuff.Mode.SRC_IN);

        if (theme == Helper.THEME_LIGHT) {
            iconMention.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            if( iconFav != null)
                iconFav.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            if( iconBoost != null)
                iconBoost.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            if( iconPoll != null)
                iconPoll.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            iconFollow.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
        } else {
            iconMention.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            if( iconFav != null)
                iconFav.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            if( iconBoost != null)
                iconBoost.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            if( iconPoll != null)
                iconPoll.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            iconFollow.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
        }

        viewPager = inflatedView.findViewById(R.id.viewpager);

        viewPager.setEnableSwipe(false);
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
                if( viewPager.getAdapter() != null) {
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tab.getPosition());
                    DisplayNotificationsFragment displayNotificationsFragment = ((DisplayNotificationsFragment) fragment);
                    displayNotificationsFragment.scrollToTop();
                }
            }
        });

        return inflatedView;
    }

    public ViewPager getViewPager(){
        return viewPager;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
            DisplayNotificationsFragment displayNotificationsFragment = new DisplayNotificationsFragment();
            DisplayNotificationsFragment.Type type = null;
            String tag = "";
            switch (position) {
                case 0:
                    if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU  && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                        type = DisplayNotificationsFragment.Type.ALL;
                    }else
                        type = DisplayNotificationsFragment.Type.MENTION;
                    break;
                case 1:
                    if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU  && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
                        type = DisplayNotificationsFragment.Type.MENTION;
                    else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                        type = DisplayNotificationsFragment.Type.BOOST;
                    else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
                        type = DisplayNotificationsFragment.Type.FOLLOW;
                    break;
                case 2:
                    if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU )
                        type = DisplayNotificationsFragment.Type.FAVORITE;
                    else
                        type = DisplayNotificationsFragment.Type.FOLLOW;
                    break;
                case 3:
                    type = DisplayNotificationsFragment.Type.BOOST;
                    break;
                case 4:
                    if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
                        type = DisplayNotificationsFragment.Type.POLL;
                    else
                        type = DisplayNotificationsFragment.Type.FOLLOW;
                    break;
                case 5:
                    if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
                        type = DisplayNotificationsFragment.Type.FOLLOW;
                default:

                    break;
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable("type", type);
            displayNotificationsFragment.setArguments(bundle);
            return displayNotificationsFragment;
        }


        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    public void refreshAll(){
        if( viewPager == null)
            return;
        FragmentStatePagerAdapter a = (FragmentStatePagerAdapter) viewPager.getAdapter();
        if( a != null) {
            DisplayNotificationsFragment notifAll = (DisplayNotificationsFragment) a.instantiateItem(viewPager, 0);
            notifAll.refreshAll();
        }
    }

    public void retrieveMissingNotifications(String sinceId){
        FragmentStatePagerAdapter a = (FragmentStatePagerAdapter) viewPager.getAdapter();
        if( a != null) {
            DisplayNotificationsFragment notifAll = (DisplayNotificationsFragment) a.instantiateItem(viewPager, 0);
            notifAll.retrieveMissingNotifications(sinceId);
        }
    }
}