package app.fedilab.android.fragments;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveStoriesAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.PixelFedStory;
import app.fedilab.android.drawers.PixelfedStoriesListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveStoriesInterface;
import es.dmoral.toasty.Toasty;



/**
 * Created by Thomas on 02/11/2019.
 * Fragment to display PixelfFed Stories
 */
public class DisplayStoriesFragment extends Fragment implements OnRetrieveStoriesInterface {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private PixelfedStoriesListAdapter pixelfedStoriesListAdapter;
    private String max_id;
    private List<PixelFedStory> pixelFedStories;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private RecyclerView lv_stories;
    LinearLayoutManager mLayoutManager;

    public DisplayStoriesFragment() {
    }




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stories, container, false);
        max_id = null;
        context = getContext();
        firstLoad = true;
        flag_loading = true;
        pixelFedStories = new ArrayList<>();
        swiped = false;
        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);

        lv_stories = rootView.findViewById(R.id.lv_stories);
        mainLoader = rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_stories);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        pixelfedStoriesListAdapter = new PixelfedStoriesListAdapter(this.pixelFedStories);
        lv_stories.setAdapter(pixelfedStoriesListAdapter);
        mLayoutManager = new LinearLayoutManager(context);
        lv_stories.setLayoutManager(mLayoutManager);
        lv_stories.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount && context != null) {
                        if (!flag_loading) {
                            flag_loading = true;
                            asyncTask = new RetrieveStoriesAsyncTask(context, max_id, DisplayStoriesFragment.this).execute();
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flag_loading = true;
                swiped = true;
                String sinceId = null;
                if (pixelFedStories != null && pixelFedStories.size() > 0)
                    sinceId = pixelFedStories.get(0).getId();
                if (context != null)
                    asyncTask = new RetrieveStoriesAsyncTask(context, null, DisplayStoriesFragment.this).execute();
            }
        });
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.white));
                break;
            case Helper.THEME_DARK:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4__,
                        R.color.mastodonC4,
                        R.color.mastodonC4);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.mastodonC1_));
                break;
            case Helper.THEME_BLACK:
                swipeRefreshLayout.setColorSchemeResources(R.color.dark_icon,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.black_3));
                break;
        }
        if (context != null)
            asyncTask = new RetrieveStoriesAsyncTask(context, max_id, DisplayStoriesFragment.this).execute();
        else
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (context != null)
                        asyncTask = new RetrieveStoriesAsyncTask(context, max_id, DisplayStoriesFragment.this).execute();
                }
            }, 500);
        return rootView;
    }


    @Override
    public void onRetrieveStories(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);

        if (apiResponse.getError() != null) {

            if(apiResponse.getError().getError().length() < 100) {
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            }else{
                Toasty.error(context, getString(R.string.long_api_error,"\ud83d\ude05"), Toast.LENGTH_LONG).show();
            }
            flag_loading = false;
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            return;
        }

        int previousPosition = pixelFedStories.size();
        max_id = apiResponse.getMax_id();
        List<PixelFedStory> pixelFedStories = apiResponse.getPixelFedStories();

        if (!swiped && firstLoad && (pixelFedStories == null || pixelFedStories.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if (swiped) {
            if (previousPosition > 0) {
                for (int i = 0; i < previousPosition; i++) {
                    this.pixelFedStories.remove(0);
                }
                pixelfedStoriesListAdapter.notifyItemRangeRemoved(0, previousPosition);
            }
            swiped = false;
        }
        if (pixelFedStories != null && pixelFedStories.size() > 0) {
            this.pixelFedStories.addAll(pixelFedStories);
            pixelfedStoriesListAdapter.notifyItemRangeInserted(previousPosition, pixelFedStories.size());
        } else {
            if (firstLoad)
                textviewNoAction.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        //The initial call comes from a classic tab refresh
        flag_loading = (max_id == null);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);

    }

    @Override
    public void onDestroyView() {
        if (lv_stories != null) {
            lv_stories.setAdapter(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefreshLayout.setEnabled(true);
        if (context == null)
            return;
        if (getUserVisibleHint() && pixelFedStories != null && pixelFedStories.size() > 0) {
            retrieveMissingNotifications(pixelFedStories.get(0).getId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(false);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.clearAnimation();
        }
    }


    /**
     * Called from main activity in onResume to retrieve missing notifications
     *
     * @param sinceId String
     */
    void retrieveMissingNotifications(String sinceId) {
        asyncTask = new RetrieveStoriesAsyncTask(context, null, DisplayStoriesFragment.this).execute();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (context == null)
            return;
        //Store last notification id to avoid to notify for those that have been already seen
        if (visible && pixelFedStories != null && pixelFedStories.size() > 0) {
            retrieveMissingNotifications(pixelFedStories.get(0).getId());
        }
    }

    public void scrollToTop() {
        if (lv_stories != null)
            lv_stories.setAdapter(pixelfedStoriesListAdapter);
    }




}
