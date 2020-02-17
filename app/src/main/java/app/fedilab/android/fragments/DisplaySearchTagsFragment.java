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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveSearchAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Trends;
import app.fedilab.android.drawers.TrendsAdapter;
import app.fedilab.android.interfaces.OnRetrieveSearchInterface;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 31/03/2019.
 * Fragment to display tags
 */
public class DisplaySearchTagsFragment extends Fragment implements OnRetrieveSearchInterface {


    private Context context;
    private TrendsAdapter trendsAdapter;
    private List<Trends> tags;
    private String search;
    private ListView lv_search_tags;
    private RelativeLayout loader;
    private RelativeLayout textviewNoAction;
    private RelativeLayout loading_next_tags;
    private boolean flag_loading;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String max_id;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search_tag, container, false);
        context = getContext();

        lv_search_tags = rootView.findViewById(R.id.lv_search_tags);
        loader = rootView.findViewById(R.id.loader);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        loader.setVisibility(View.VISIBLE);
        loading_next_tags = rootView.findViewById(R.id.loading_next_tags);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        flag_loading = true;
        if (tags == null)
            tags = new ArrayList<>();
        max_id = null;

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            search = bundle.getString("search");
            if (search == null) {
                Toasty.error(context, getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
            }
        } else {
            Toasty.error(context, getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
        }

        int c1 = getResources().getColor(R.color.cyanea_accent);
        int c2 = getResources().getColor(R.color.cyanea_primary_dark);
        int c3 = getResources().getColor(R.color.cyanea_primary);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(c3);
        swipeRefreshLayout.setColorSchemeColors(
                c1, c2, c1
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tags.clear();
                tags = new ArrayList<>();
                max_id = "0";
                trendsAdapter.notifyDataSetChanged();
                if (search != null) {
                    new RetrieveSearchAsyncTask(context, search, API.searchType.TAGS, null, DisplaySearchTagsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        lv_search_tags.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && context != null) {
                    if (!flag_loading) {
                        flag_loading = true;
                        if (search != null) {
                            new RetrieveSearchAsyncTask(context, search, API.searchType.TAGS, max_id, DisplaySearchTagsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        loading_next_tags.setVisibility(View.VISIBLE);
                    }
                } else {
                    loading_next_tags.setVisibility(View.GONE);
                }
            }
        });

        if (search != null) {
            new RetrieveSearchAsyncTask(context, search, API.searchType.TAGS, null, DisplaySearchTagsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return rootView;
    }


    public void scrollToTop() {
        if (lv_search_tags != null && trendsAdapter != null) {
            lv_search_tags.setAdapter(trendsAdapter);
        }
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }


    @Override
    public void onDestroyView() {
        if (lv_search_tags != null) {
            lv_search_tags.setAdapter(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public void onRetrieveSearch(APIResponse apiResponse) {

        trendsAdapter = new TrendsAdapter(context, tags);
        loader.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        if (apiResponse.getError() != null) {
            if (apiResponse.getError().getError() != null)
                if (apiResponse.getError().getError().length() < 100) {
                    Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
                } else {
                    Toasty.error(context, getString(R.string.long_api_error, "\ud83d\ude05"), Toast.LENGTH_LONG).show();
                }
            else
                Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        if (max_id == null)
            max_id = "0";
        max_id = String.valueOf(Integer.valueOf(max_id) + 20);
        lv_search_tags.setVisibility(View.VISIBLE);
        List<Trends> newTags = new ArrayList<>();
        if (apiResponse.getResults() != null) {
            newTags = apiResponse.getResults().getTrends();
        }
        tags.addAll(newTags);
        TrendsAdapter trendsAdapter = new TrendsAdapter(context, tags);
        lv_search_tags.setAdapter(trendsAdapter);
        trendsAdapter.notifyDataSetChanged();
        if (newTags.size() == 0 && tags.size() == 0)
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
    }

}
