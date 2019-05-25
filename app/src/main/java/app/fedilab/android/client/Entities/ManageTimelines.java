package app.fedilab.android.client.Entities;
/* Copyright 2019 Thomas Schneider
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import app.fedilab.android.sqlite.InstancesDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.BaseMainActivity;
import app.fedilab.android.activities.ListActivity;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.fragments.DisplayStatusFragment;
import app.fedilab.android.fragments.TabLayoutNotificationsFragment;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.SearchDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TimelinesDAO;

import static app.fedilab.android.activities.BaseMainActivity.mPageReferenceMap;
import static app.fedilab.android.helper.Helper.THEME_LIGHT;
import static app.fedilab.android.sqlite.Sqlite.DB_NAME;


public class ManageTimelines {

    private int position;
    private int id;
    private boolean displayed;
    private Type type;
    private static String userId;
    private static String instance;
    private RemoteInstance remoteInstance;
    private TagTimeline tagTimeline;
    private List listTimeline;
    private String currentFilter;


    private boolean notif_follow, notif_add, notif_mention, notif_share, notif_poll;


    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public ManageTimelines.Type getType() {
        return type;
    }

    public void setType(ManageTimelines.Type type) {
        this.type = type;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }


    public String getInstance() {
        return instance;
    }


    public RemoteInstance getRemoteInstance() {
        return remoteInstance;
    }

    public void setRemoteInstance(RemoteInstance remoteInstance) {
        this.remoteInstance = remoteInstance;
    }

    public TagTimeline getTagTimeline() {
        return tagTimeline;
    }

    public void setTagTimeline(TagTimeline tagTimeline) {
        this.tagTimeline = tagTimeline;
    }


    public List getListTimeline() {
        return listTimeline;
    }

    public void setListTimeline(List listTimeline) {
        this.listTimeline = listTimeline;
    }

    public enum Type{
        HOME,
        DIRECT,
        NOTIFICATION,
        LOCAL,
        PUBLIC,
        ART,
        PEERTUBE,
        TAG,
        LIST,
        INSTANCE
    }


    public static Type typeFromDb(String value){
        switch (value){
            case "HOME":
                return Type.HOME;
            case "DIRECT":
                return Type.DIRECT;
            case "NOTIFICATION":
                return Type.NOTIFICATION;
            case "LOCAL":
                return Type.LOCAL;
            case "PUBLIC":
                return Type.PUBLIC;
            case "ART":
                return Type.ART;
            case "PEERTUBE":
                return Type.PEERTUBE;
            case "TAG":
                return Type.TAG;
            case "LIST":
                return Type.LIST;
            case "INSTANCE":
                return Type.INSTANCE;
        }
        return null;
    }

    public static String typeToDb(Type type){
        switch (type){
            case HOME:
                return "HOME";
            case DIRECT:
                return "DIRECT";
            case NOTIFICATION:
                return "NOTIFICATION";
            case LOCAL:
                return "LOCAL";
            case PUBLIC:
                return "PUBLIC";
            case ART:
                return "ART";
            case PEERTUBE:
                return "PEERTUBE";
            case TAG:
                return "TAG";
            case LIST:
                return "LIST";
            case INSTANCE:
                return "INSTANCE";
        }
        return null;
    }


    public static RetrieveFeedsAsyncTask.Type transform(Context context, Type type){

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA ){
            switch (type){
                case HOME:
                    return RetrieveFeedsAsyncTask.Type.HOME;
                case DIRECT:
                    userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                    instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(context));
                    String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
                    if (instanceVersion != null) {
                        Version currentVersion = new Version(instanceVersion);
                        Version minVersion = new Version("2.6");
                        if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                            return RetrieveFeedsAsyncTask.Type.CONVERSATION;
                        } else {
                            return RetrieveFeedsAsyncTask.Type.DIRECT;
                        }
                    } else {
                        return RetrieveFeedsAsyncTask.Type.DIRECT;
                    }
                case NOTIFICATION:
                    return RetrieveFeedsAsyncTask.Type.NOTIFICATION;
                case PUBLIC:
                    return RetrieveFeedsAsyncTask.Type.PUBLIC;
                case LOCAL:
                    return RetrieveFeedsAsyncTask.Type.LOCAL;
                case ART:
                    return RetrieveFeedsAsyncTask.Type.ART;
                case PEERTUBE:
                    return RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE;
                case INSTANCE:
                    return RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE;
                case TAG:
                    return RetrieveFeedsAsyncTask.Type.TAG;
                case LIST:
                    return RetrieveFeedsAsyncTask.Type.LIST;
            }
            return null;
        }else if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
            switch (type) {
                case HOME:
                    return RetrieveFeedsAsyncTask.Type.GNU_HOME;
                case NOTIFICATION:
                    return RetrieveFeedsAsyncTask.Type.GNU_NOTIFICATION;
                case DIRECT:
                    return RetrieveFeedsAsyncTask.Type.GNU_DM;
                case LOCAL:
                    return RetrieveFeedsAsyncTask.Type.GNU_LOCAL;
            }
            return null;
        }else if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU){
            switch (type) {
                case HOME:
                    return RetrieveFeedsAsyncTask.Type.GNU_HOME;
                case NOTIFICATION:
                    return RetrieveFeedsAsyncTask.Type.GNU_NOTIFICATION;
                case DIRECT:
                    return RetrieveFeedsAsyncTask.Type.GNU_DM;
                case LOCAL:
                    return RetrieveFeedsAsyncTask.Type.GNU_LOCAL;
                case PUBLIC:
                    return RetrieveFeedsAsyncTask.Type.GNU_WHOLE;
                case TAG:
                    return RetrieveFeedsAsyncTask.Type.GNU_TAG;
            }
            return null;
        }
        return null;
    }


    public void createTabs(Context context, java.util.List<ManageTimelines> manageTimelines){

        TabLayout tabLayout = ((BaseMainActivity)context).findViewById(R.id.tabLayout);

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        tabLayout.removeAllTabs();
        int position = 0;
        for(ManageTimelines tl: manageTimelines){
            TabLayout.Tab tb = tabLayout.newTab();
            ImageView icon = null;
            if( tl.getType() != Type.TAG && tl.getType() != Type.INSTANCE && tl.getType() != Type.LIST) {
                tb.setCustomView(R.layout.tab_badge);
                if( tb.getCustomView() != null)
                    icon = tb.getCustomView().findViewById(R.id.tab_icon);
            }
            if( icon != null){
                if( tl.getPosition() == 0)
                    icon.setColorFilter(ContextCompat.getColor(context, R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                switch (tl.getType()){
                    case HOME:
                        icon.setImageResource(R.drawable.ic_home);
                        icon.setContentDescription(context.getString(R.string.home_menu));
                        break;
                    case NOTIFICATION:
                        icon.setImageResource(R.drawable.ic_notifications);
                        icon.setContentDescription(context.getString(R.string.notifications));
                        break;
                    case DIRECT:
                        icon.setImageResource(R.drawable.ic_direct_messages);
                        icon.setContentDescription(context.getString(R.string.direct_message));
                        break;
                    case LOCAL:
                        icon.setImageResource(R.drawable.ic_people);
                        icon.setContentDescription(context.getString(R.string.local_menu));
                        break;
                    case PUBLIC:
                        icon.setImageResource(R.drawable.ic_public);
                        icon.setContentDescription(context.getString(R.string.global_menu));
                        break;
                    case ART:
                        icon.setImageResource(R.drawable.ic_color_lens);
                        icon.setContentDescription(context.getString(R.string.art_menu));
                        break;
                    case PEERTUBE:
                        icon.setImageResource(R.drawable.ic_video_peertube);
                        icon.setContentDescription(context.getString(R.string.peertube_menu));
                        break;
                }
                if (theme == THEME_LIGHT) {
                    icon.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                } else {
                    icon.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
                }
                tabLayout.addTab(tb);
            }else{
                String name = "";
                if( tl.getType() == Type.TAG){
                    if( tl.getTagTimeline().getDisplayname() != null) {
                        name = tl.getTagTimeline().getDisplayname();
                    }else {
                        name = tl.getTagTimeline().getName();
                    }
                }else if( tl.getType() == Type.INSTANCE && (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)){
                    name = tl.getRemoteInstance().getHost();
                }else if( tl.getType() == Type.LIST){
                    name = tl.getListTimeline().getTitle();
                }
                TextView tv=(TextView)LayoutInflater.from(context).inflate(R.layout.custom_tab_instance,null);
                tv.setText(name);
                if (theme == THEME_LIGHT) {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.action_light_header));
                }
                tb.setCustomView(tv);
                if( tl.getPosition() == 0){
                    tv.setTextColor(ContextCompat.getColor(context, R.color.mastodonC4));
                }
                tabLayout.addTab(tb);
            }

            final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
            if( tl.getType() == Type.NOTIFICATION){
                notificationClik(context, tl, tabLayout);
            }else if( tl.getType() == Type.PUBLIC || tl.getType() == Type.LOCAL || tl.getType() == Type.ART  || tl.getType() == Type.HOME) {
                if( tabStrip != null && tabStrip.getChildCount() > position) {
                    int finalPosition1 = position;
                    tabStrip.getChildAt(position).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            manageFilters(context, tl, tabStrip, finalPosition1);
                            return true;
                        }
                    });
                }
            }else if( tl.getType() == Type.TAG) {
                if( tabStrip != null && tabStrip.getChildCount() > position) {
                    int finalPosition = position;
                    tabStrip.getChildAt(position).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            tagClick(context, tl, tabStrip, finalPosition);
                            return true;
                        }
                    });
                }
            }else if( tl.getType() == Type.INSTANCE && (tl.getRemoteInstance().getType().equals("MASTODON") || tl.getRemoteInstance().getType().equals("PEERTUBE") || tl.getRemoteInstance().getType().equals("PLEROMA")|| tl.getRemoteInstance().getType().equals("GNU"))) {
                if( tabStrip != null && tabStrip.getChildCount() > position) {
                    int finalPosition = position;
                    tabStrip.getChildAt(position).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            instanceClick(context, tl, tabStrip, finalPosition);
                            return true;
                        }
                    });
                }
            }else if (tl.getType() == Type.LIST){
                if( tabStrip != null && tabStrip.getChildCount() > position) {
                    tabStrip.getChildAt(position).setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Intent intent = new Intent(context, ListActivity.class);
                            Bundle b = new Bundle();
                            b.putString("id", tl.getListTimeline().getId());
                            b.putString("title", tl.getListTimeline().getTitle());
                            intent.putExtras(b);
                            context.startActivity(intent);
                            return true;
                        }
                    });
                }
            }
            position++;
        }
    }




    private void notificationClik(Context context, ManageTimelines tl,  TabLayout tabLayout){
        final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA )
            if( tabStrip != null && tabStrip.getChildCount() > tl.getPosition()){
                tabStrip.getChildAt( tl.getPosition()).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //Only shown if the tab has focus
                        PopupMenu popup = new PopupMenu(context, tabStrip.getChildAt(1));
                        popup.getMenuInflater()
                                .inflate(R.menu.option_filter_notifications, popup.getMenu());
                        Menu menu = popup.getMenu();
                        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                        final MenuItem itemFavourite = menu.findItem(R.id.action_favorite);
                        final MenuItem itemFollow = menu.findItem(R.id.action_follow);
                        final MenuItem itemMention = menu.findItem(R.id.action_mention);
                        final MenuItem itemBoost = menu.findItem(R.id.action_boost);
                        final MenuItem itemPoll = menu.findItem(R.id.action_poll);
                        notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, true);
                        notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD_FILTER, true);
                        notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION_FILTER, true);
                        notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE_FILTER, true);
                        notif_poll = sharedpreferences.getBoolean(Helper.SET_NOTIF_POLL_FILTER, true);
                        itemFavourite.setChecked(notif_add);
                        itemFollow.setChecked(notif_follow);
                        itemMention.setChecked(notif_mention);
                        itemBoost.setChecked(notif_share);
                        if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
                            itemPoll.setChecked(notif_poll);
                        else
                            itemPoll.setVisible(false);

                        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                            @Override
                            public void onDismiss(PopupMenu menu) {
                                TabLayoutNotificationsFragment tabLayoutNotificationsFragment = (TabLayoutNotificationsFragment) mPageReferenceMap.get(tl.getPosition());
                                assert tabLayoutNotificationsFragment != null;
                                tabLayoutNotificationsFragment.refreshAll();
                            }
                        });
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                                item.setActionView(new View(context));
                                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                                    @Override
                                    public boolean onMenuItemActionExpand(MenuItem item) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onMenuItemActionCollapse(MenuItem item) {
                                        return false;
                                    }
                                });
                                switch (item.getItemId()) {
                                    case R.id.action_favorite:
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        notif_add = !notif_add;
                                        editor.putBoolean(Helper.SET_NOTIF_ADD_FILTER, notif_add);
                                        itemFavourite.setChecked(notif_add);
                                        editor.apply();
                                        break;
                                    case R.id.action_follow:
                                        editor = sharedpreferences.edit();
                                        notif_follow = !notif_follow;
                                        editor.putBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, notif_follow);
                                        itemFollow.setChecked(notif_follow);
                                        editor.apply();
                                        break;
                                    case R.id.action_mention:
                                        editor = sharedpreferences.edit();
                                        notif_mention = !notif_mention;
                                        editor.putBoolean(Helper.SET_NOTIF_MENTION_FILTER, notif_mention);
                                        itemMention.setChecked(notif_mention);
                                        editor.apply();
                                        break;
                                    case R.id.action_boost:
                                        editor = sharedpreferences.edit();
                                        notif_share = !notif_share;
                                        editor.putBoolean(Helper.SET_NOTIF_SHARE_FILTER, notif_share);
                                        itemBoost.setChecked(notif_share);
                                        editor.apply();
                                        break;
                                    case R.id.action_poll:
                                        editor = sharedpreferences.edit();
                                        notif_poll = !notif_poll;
                                        editor.putBoolean(Helper.SET_NOTIF_POLL_FILTER, notif_poll);
                                        itemPoll.setChecked(notif_poll);
                                        editor.apply();
                                        break;
                                }
                                return false;
                            }
                        });
                        popup.show();
                        return true;
                    }
                });
            }

    }




    private void manageFilters(Context context, ManageTimelines tl, LinearLayout tabStrip, int position){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        //Only shown if the tab has focus
        PopupMenu popup = new PopupMenu(context, tabStrip.getChildAt(position));
        if( tl.getType() == Type.ART){
            popup.getMenuInflater()
                    .inflate(R.menu.option_tag_timeline, popup.getMenu());
            Menu menu = popup.getMenu();

            final boolean[] show_nsfw = {sharedpreferences.getBoolean(Helper.SET_ART_WITH_NSFW, false)};
            final MenuItem itemShowNSFW = menu.findItem(R.id.action_show_nsfw);
            final MenuItem itemMedia = menu.findItem(R.id.action_show_media_only);
            //final MenuItem itemDelete = menu.findItem(R.id.action_delete);

            final MenuItem itemAny = menu.findItem(R.id.action_any);
            final MenuItem itemAll = menu.findItem(R.id.action_all);
            final MenuItem itemNone = menu.findItem(R.id.action_none);
            final MenuItem action_displayname = menu.findItem(R.id.action_displayname);
            itemAny.setVisible(false);
            itemAll.setVisible(false);
            itemNone.setVisible(false);
            action_displayname.setVisible(false);
            itemMedia.setVisible(false);
           // itemDelete.setVisible(false);
            itemShowNSFW.setChecked(show_nsfw[0]);
            final boolean[] changes = {false};
            popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    if(changes[0]) {
                        FragmentTransaction fragTransaction = ((MainActivity)context).getSupportFragmentManager().beginTransaction();
                        DisplayStatusFragment displayStatusFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                        assert displayStatusFragment != null;
                        fragTransaction.detach(displayStatusFragment);
                        fragTransaction.attach(displayStatusFragment);
                        fragTransaction.commit();
                    }
                }
            });
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    changes[0] = true;
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                    item.setActionView(new View(context));
                    item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            return false;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            return false;
                        }
                    });
                    if (item.getItemId() == R.id.action_show_nsfw) {
                        show_nsfw[0] = !show_nsfw[0];
                        itemShowNSFW.setChecked(show_nsfw[0]);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(Helper.SET_ART_WITH_NSFW, show_nsfw[0]);
                        editor.apply();
                    }
                    return false;
                }
            });
            popup.show();
        }else{
            popup.getMenuInflater()
                    .inflate(R.menu.option_filter_toots, popup.getMenu());
            Menu menu = popup.getMenu();
            final MenuItem itemShowBoosts = menu.findItem(R.id.action_show_boosts);
            final MenuItem itemShowReplies = menu.findItem(R.id.action_show_replies);
            final MenuItem itemFilter = menu.findItem(R.id.action_filter);
            DisplayStatusFragment displayStatusFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());

            if(tl.getType() != Type.HOME){
                itemShowBoosts.setVisible(false);
                itemShowReplies.setVisible(false);
                itemFilter.setVisible(true);
            }else {
                itemShowBoosts.setVisible(true);
                itemShowReplies.setVisible(true);
                itemFilter.setVisible(true);
            }
            final boolean[] show_boosts = {sharedpreferences.getBoolean(Helper.SET_SHOW_BOOSTS, true)};
            final boolean[] show_replies = {sharedpreferences.getBoolean(Helper.SET_SHOW_REPLIES, true)};

            String show_filtered = null;
            if(displayStatusFragment != null && displayStatusFragment.getUserVisibleHint() && tl.getType() == Type.HOME)
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_HOME, null);
            if(displayStatusFragment != null && displayStatusFragment.getUserVisibleHint() && tl.getType() == Type.LOCAL)
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_LOCAL, null);
            if(displayStatusFragment != null && displayStatusFragment.getUserVisibleHint()  && tl.getType() == Type.PUBLIC)
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_PUBLIC, null);

            itemShowBoosts.setChecked(show_boosts[0]);
            itemShowReplies.setChecked(show_replies[0]);
            if( show_filtered != null && show_filtered.length() > 0){
                itemFilter.setTitle(show_filtered);
            }

            popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    if(displayStatusFragment != null && displayStatusFragment.getUserVisibleHint())
                        displayStatusFragment.refreshFilter();
                }
            });
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            int style;
            if (theme == Helper.THEME_DARK) {
                style = R.style.DialogDark;
            } else if (theme == Helper.THEME_BLACK){
                style = R.style.DialogBlack;
            }else {
                style = R.style.Dialog;
            }
            String finalShow_filtered = show_filtered;
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                    item.setActionView(new View(context));
                    item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            return false;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            return false;
                        }
                    });
                    final SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (item.getItemId()) {
                        case R.id.action_show_boosts:
                            show_boosts[0] = !show_boosts[0];
                            editor.putBoolean(Helper.SET_SHOW_BOOSTS, show_boosts[0]);
                            itemShowBoosts.setChecked(show_boosts[0]);
                            editor.apply();
                            break;
                        case R.id.action_show_replies:
                            show_replies[0] = !show_replies[0];
                            editor.putBoolean(Helper.SET_SHOW_REPLIES, show_replies[0]);
                            itemShowReplies.setChecked(show_replies[0]);
                            editor.apply();
                            break;
                        case R.id.action_filter:
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                            LayoutInflater inflater = ((MainActivity)context).getLayoutInflater();
                            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.filter_regex, null);
                            dialogBuilder.setView(dialogView);
                            final EditText editText = dialogView.findViewById(R.id.filter_regex);
                            Toast alertRegex = Toasty.warning(context, context.getString(R.string.alert_regex), Toast.LENGTH_LONG);
                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }
                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                }
                                @Override
                                public void afterTextChanged(Editable s) {
                                    try {
                                        Pattern.compile("(" + s.toString() + ")", Pattern.CASE_INSENSITIVE);
                                    }catch (Exception e){
                                        if( !alertRegex.getView().isShown()){
                                            alertRegex.show();
                                        }
                                    }

                                }
                            });
                            if( finalShow_filtered != null) {
                                editText.setText(finalShow_filtered);
                                editText.setSelection(editText.getText().toString().length());
                            }
                            dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    itemFilter.setTitle(editText.getText().toString().trim());
                                    if(displayStatusFragment != null && displayStatusFragment.getUserVisibleHint() && tl.getType() == Type.HOME)
                                        editor.putString(Helper.SET_FILTER_REGEX_HOME, editText.getText().toString().trim());
                                    if(displayStatusFragment != null && displayStatusFragment.getUserVisibleHint() && tl.getType() == Type.LOCAL)
                                        editor.putString(Helper.SET_FILTER_REGEX_LOCAL, editText.getText().toString().trim());
                                    if(displayStatusFragment != null && displayStatusFragment.getUserVisibleHint() && tl.getType() == Type.PUBLIC)
                                        editor.putString(Helper.SET_FILTER_REGEX_PUBLIC, editText.getText().toString().trim());
                                    editor.apply();
                                }
                            });
                            AlertDialog alertDialog = dialogBuilder.create();
                            alertDialog.show();
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }
    }


    private void tagClick(Context context, ManageTimelines tl, LinearLayout tabStrip, int position){


        PopupMenu popup = new PopupMenu(context, tabStrip.getChildAt(position));
        TabLayout tabLayout = ((MainActivity)context).findViewById(R.id.tabLayout);
        SQLiteDatabase db = Sqlite.getInstance(context, DB_NAME, null, Sqlite.DB_VERSION).open();
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        String tag;
        tagTimeline = tl.getTagTimeline();
        if( tagTimeline == null)
            return;
        if( tagTimeline.getDisplayname() != null)
            tag = tagTimeline.getDisplayname();
        else
            tag = tagTimeline.getName();
        popup.getMenuInflater()
                .inflate(R.menu.option_tag_timeline, popup.getMenu());
        Menu menu = popup.getMenu();


        final MenuItem itemMediaOnly = menu.findItem(R.id.action_show_media_only);
        final MenuItem itemShowNSFW = menu.findItem(R.id.action_show_nsfw);


        final boolean[] changes = {false};
        final boolean[] mediaOnly = {false};
        final boolean[] showNSFW = {false};
        mediaOnly[0] = tagTimeline.isART();
        showNSFW[0] = tagTimeline.isNSFW();
        itemMediaOnly.setChecked(mediaOnly[0]);
        itemShowNSFW.setChecked(showNSFW[0]);
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if(changes[0]) {
                    FragmentTransaction fragTransaction = ((MainActivity)context).getSupportFragmentManager().beginTransaction();
                    DisplayStatusFragment displayStatusFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                    if( displayStatusFragment == null)
                        return;
                    fragTransaction.detach(displayStatusFragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("tag", tl.getTagTimeline().getName());
                    bundle.putInt("timelineId", tl.getId());
                    bundle.putSerializable("type",  RetrieveFeedsAsyncTask.Type.TAG);
                    if( mediaOnly[0])
                        bundle.putString("instanceType","ART");
                    else
                        bundle.putString("instanceType","MASTODON");
                    displayStatusFragment.setArguments(bundle);
                    fragTransaction.attach(displayStatusFragment);
                    fragTransaction.commit();
                }
            }
        });


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(context));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                changes[0] = true;
                switch (item.getItemId()) {
                    case R.id.action_show_media_only:
                        mediaOnly[0] =!mediaOnly[0];
                        tagTimeline.setART(mediaOnly[0]);
                        new SearchDAO(context, db).updateSearch(tagTimeline);
                        tl.setTagTimeline(tagTimeline);
                        itemMediaOnly.setChecked(mediaOnly[0]);
                        new TimelinesDAO(context, db).updateTag(tl);
                        break;
                    case R.id.action_show_nsfw:
                        showNSFW[0] = !showNSFW[0];
                        tagTimeline.setNSFW(showNSFW[0]);
                        new SearchDAO(context, db).updateSearch(tagTimeline);
                        tl.setTagTimeline(tagTimeline);
                        itemShowNSFW.setChecked(showNSFW[0]);
                        new TimelinesDAO(context, db).updateTag(tl);
                        break;
                    case R.id.action_any:
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                        LayoutInflater inflater =  ((MainActivity)context).getLayoutInflater();
                        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.tags_any, null);
                        dialogBuilder.setView(dialogView);
                        final EditText editText = dialogView.findViewById(R.id.filter_any);
                        if(tagTimeline.getAny() != null) {
                            String valuesTag = "";
                            for(String val: tagTimeline.getAny())
                                valuesTag += val+" ";
                            editText.setText(valuesTag);
                            editText.setSelection(editText.getText().toString().length());
                        }
                        dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String[] values = editText.getText().toString().trim().split("\\s+");
                                java.util.List<String> any =
                                        new ArrayList<>(Arrays.asList(values));
                                tagTimeline.setAny(any);
                                new SearchDAO(context, db).updateSearch(tagTimeline);
                                tl.setTagTimeline(tagTimeline);
                                new TimelinesDAO(context, db).updateTag(tl);
                            }
                        });
                        AlertDialog alertDialog = dialogBuilder.create();
                        alertDialog.show();
                        break;
                    case R.id.action_all:
                        dialogBuilder = new AlertDialog.Builder(context, style);
                        inflater =  ((MainActivity)context).getLayoutInflater();
                        dialogView = inflater.inflate(R.layout.tags_all, null);
                        dialogBuilder.setView(dialogView);
                        final EditText editTextAll = dialogView.findViewById(R.id.filter_all);
                        if( tagTimeline.getAll() != null) {
                            String valuesTag = "";
                            for(String val: tagTimeline.getAll())
                                valuesTag += val+" ";
                            editTextAll.setText(valuesTag);
                            editTextAll.setSelection(editTextAll.getText().toString().length());
                        }
                        dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String[] values = editTextAll.getText().toString().trim().split("\\s+");
                                java.util.List<String> all =
                                        new ArrayList<>(Arrays.asList(values));
                                tagTimeline.setAll(all);
                                new SearchDAO(context, db).updateSearch(tagTimeline);
                                tl.setTagTimeline(tagTimeline);
                                new TimelinesDAO(context, db).updateTag(tl);
                            }
                        });
                        alertDialog = dialogBuilder.create();
                        alertDialog.show();
                        break;
                    case R.id.action_none:
                        dialogBuilder = new AlertDialog.Builder(context, style);
                        inflater = ((MainActivity)context). getLayoutInflater();
                        dialogView = inflater.inflate(R.layout.tags_all, null);
                        dialogBuilder.setView(dialogView);
                        final EditText editTextNone = dialogView.findViewById(R.id.filter_all);
                        if( tagTimeline.getNone() != null) {
                            String valuesTag = "";
                            for(String val: tagTimeline.getNone())
                                valuesTag += val+" ";
                            editTextNone.setText(valuesTag);
                            editTextNone.setSelection(editTextNone.getText().toString().length());
                        }
                        dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String[] values = editTextNone.getText().toString().trim().split("\\s+");
                                java.util.List<String> none =
                                        new ArrayList<>(Arrays.asList(values));
                                tagTimeline.setNone(none);
                                new SearchDAO(context, db).updateSearch(tagTimeline);
                                tl.setTagTimeline(tagTimeline);
                                new TimelinesDAO(context, db).updateTag(tl);
                            }
                        });
                        alertDialog = dialogBuilder.create();
                        alertDialog.show();
                        break;
                    case R.id.action_displayname:
                        dialogBuilder = new AlertDialog.Builder(context, style);
                        inflater =  ((MainActivity)context).getLayoutInflater();
                        dialogView = inflater.inflate(R.layout.tags_name, null);
                        dialogBuilder.setView(dialogView);
                        final EditText editTextName = dialogView.findViewById(R.id.column_name);
                        if( tagTimeline.getDisplayname() != null) {
                            editTextName.setText(tagTimeline.getDisplayname());
                            editTextName.setSelection(editTextName.getText().toString().length());
                        }
                        dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String values = editTextName.getText().toString();
                                if( values.trim().length() == 0)
                                    values = tag;
                                if( tabLayout.getTabAt(position) != null)
                                    tabLayout.getTabAt(position).setText(values);
                                tagTimeline.setDisplayname(values);
                                new SearchDAO(context, db).updateSearch(tagTimeline);
                                tl.setTagTimeline(tagTimeline);
                                new TimelinesDAO(context, db).updateTag(tl);
                            }
                        });
                        alertDialog = dialogBuilder.create();
                        alertDialog.show();
                        break;
                }
                return false;
            }
        });
        popup.show();

    }



    private void instanceClick(Context context, ManageTimelines tl, LinearLayout tabStrip, int position){


        PopupMenu popup = new PopupMenu(context, tabStrip.getChildAt(position));
        SQLiteDatabase db = Sqlite.getInstance(context, DB_NAME, null, Sqlite.DB_VERSION).open();
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        remoteInstance = tl.getRemoteInstance();
        if( remoteInstance == null)
            return;
        currentFilter = remoteInstance.getFilteredWith();

        final boolean[] changes = {false};

        String title;
        if( currentFilter == null) {
            title = "✔ " + context.getString(R.string.all);
        }else{
            title = context.getString(R.string.all);
        }

        MenuItem itemall = popup.getMenu().add(0, 0, Menu.NONE, title);

        itemall.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(context));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                changes[0] = true;
                FragmentTransaction fragTransaction = ((MainActivity)context).getSupportFragmentManager().beginTransaction();
                DisplayStatusFragment displayStatusFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                if( displayStatusFragment == null)
                    return false;
                tl.getRemoteInstance().setFilteredWith(null);
                remoteInstance.setFilteredWith(null);
                currentFilter = null;
                new InstancesDAO(context, db).updateInstance(remoteInstance);
                tl.setRemoteInstance(remoteInstance);
                new TimelinesDAO(context, db).updateRemoteInstance(tl);
                fragTransaction.detach(displayStatusFragment);
                Bundle bundle = new Bundle();
                bundle.putString("remote_instance", tl.getRemoteInstance().getHost()!=null?tl.getRemoteInstance().getHost():"");
                bundle.putString("instanceType", tl.getRemoteInstance().getType());
                bundle.putInt("timelineId", tl.getId());
                bundle.putSerializable("type",  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                displayStatusFragment.setArguments(bundle);
                fragTransaction.attach(displayStatusFragment);
                fragTransaction.commit();
                popup.getMenu().close();
                return false;
            }
        });


        java.util.List<String> tags = remoteInstance.getTags();
        if( tags != null && tags.size() > 0){
            java.util.Collections.sort(tags);
            for(String tag: tags){
                if( tag == null || tag.length() == 0 )
                    continue;
                if( currentFilter != null && currentFilter.equals(tag)) {
                    title = "✔ " + tag;
                }else{
                    title = tag;
                }
                MenuItem item = popup.getMenu().add(0, 0, Menu.NONE, title);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FragmentTransaction fragTransaction = ((MainActivity)context).getSupportFragmentManager().beginTransaction();
                        DisplayStatusFragment displayStatusFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                        if( displayStatusFragment == null)
                            return false;
                        tl.getRemoteInstance().setFilteredWith(tag);
                        remoteInstance.setFilteredWith(tag);
                        new InstancesDAO(context, db).updateInstance(remoteInstance);
                        tl.setRemoteInstance(remoteInstance);
                        new TimelinesDAO(context, db).updateRemoteInstance(tl);
                        currentFilter = tl.getRemoteInstance().getFilteredWith();
                        fragTransaction.detach(displayStatusFragment);
                        Bundle bundle = new Bundle();
                        bundle.putString("remote_instance", tl.getRemoteInstance().getHost()!=null?tl.getRemoteInstance().getHost():"");
                        bundle.putString("instanceType", tl.getRemoteInstance().getType());
                        bundle.putInt("timelineId", tl.getId());
                        bundle.putString("currentfilter", tl.getRemoteInstance().getFilteredWith());
                        bundle.putSerializable("type",  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE_FILTERED);
                        displayStatusFragment.setArguments(bundle);
                        fragTransaction.attach(displayStatusFragment);
                        fragTransaction.commit();
                        return false;
                    }
                });
            }
        }


        MenuItem itemadd = popup.getMenu().add(0, 0, Menu.NONE, context.getString(R.string.add_tags));
        itemadd.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(context));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                changes[0] = true;
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                LayoutInflater inflater =  ((MainActivity)context).getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.tags_instance, null);
                dialogBuilder.setView(dialogView);
                final EditText editText = dialogView.findViewById(R.id.filter_words);
                if(remoteInstance.getTags() != null) {
                    String valuesTag = "";
                    for(String val: remoteInstance.getTags())
                        valuesTag += val+" ";
                    editText.setText(valuesTag);
                    editText.setSelection(editText.getText().toString().length());
                }
                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String[] values = editText.getText().toString().trim().split("\\s+");
                        java.util.List<String> tags =
                                new ArrayList<>(Arrays.asList(values));
                        remoteInstance.setTags(tags);
                        new InstancesDAO(context, db).updateInstance(remoteInstance);
                        tl.setRemoteInstance(remoteInstance);
                        new TimelinesDAO(context, db).updateRemoteInstance(tl);
                        popup.getMenu().clear();
                        popup.getMenu().close();
                        instanceClick(context, tl, tabStrip, position);
                    }
                });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                return false;
            }
        });

        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if(changes[0]) {
                    FragmentTransaction fragTransaction = ((MainActivity)context).getSupportFragmentManager().beginTransaction();
                    DisplayStatusFragment displayStatusFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                    if( displayStatusFragment == null)
                        return;
                    fragTransaction.detach(displayStatusFragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("remote_instance", tl.getRemoteInstance().getHost()!=null?tl.getRemoteInstance().getHost():"");
                    bundle.putString("instanceType", tl.getRemoteInstance().getType());
                    bundle.putInt("timelineId", tl.getId());
                    if( currentFilter == null){
                        bundle.putSerializable("type",  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                    }else{
                        bundle.putString("currentfilter", tl.getRemoteInstance().getFilteredWith());
                        bundle.putSerializable("type",  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE_FILTERED);
                    }
                    displayStatusFragment.setArguments(bundle);
                    fragTransaction.attach(displayStatusFragment);
                    fragTransaction.commit();
                }
            }
        });

        popup.show();

    }

}
