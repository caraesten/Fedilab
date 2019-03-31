package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2019 Thomas Schneider
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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.drawers.SearchTagsAdapter;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchInterface;



/**
 * Created by Thomas on 31/03/2019.
 * Fragment to display tags
 */
public class DisplaySearchTagsFragment extends Fragment implements OnRetrieveSearchInterface {


    private Context context;
    private SearchTagsAdapter searchTagsAdapter;
    private List<String> tags;
    private String search;
    private RecyclerView lv_search_tags;
    private RelativeLayout loader;
    private RelativeLayout textviewNoAction;
    private RelativeLayout loading_next_tags;
    private LinearLayoutManager mLayoutManager;
    private boolean flag_loading;
    private String max_id;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        lv_search_tags = rootView.findViewById(R.id.lv_search_toots);
        loader = rootView.findViewById(R.id.loader);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        loader.setVisibility(View.VISIBLE);
        loading_next_tags = rootView.findViewById(R.id.loading_next_tags);
        flag_loading = true;
        if (tags == null)
            tags = new ArrayList<>();
        max_id = null;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            search = bundle.getString("search");
            if (search != null)
                new RetrieveSearchAsyncTask(context, search.trim(), DisplaySearchTagsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                Toasty.error(context, getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
        } else {
            Toasty.error(context, getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
        }

        mLayoutManager = new LinearLayoutManager(context);
        lv_search_tags.setLayoutManager(mLayoutManager);

        lv_search_tags.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
            if(dy > 0){
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                if(firstVisibleItem + visibleItemCount == totalItemCount && context != null) {
                    if(!flag_loading ) {
                        flag_loading = true;
                        new RetrieveSearchAsyncTask(context, search, API.searchType.TAGS, max_id, DisplaySearchTagsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        loading_next_tags.setVisibility(View.VISIBLE);
                    }
                } else {
                    loading_next_tags.setVisibility(View.GONE);
                }
            }
            }
        });
        new RetrieveSearchAsyncTask(context, search, API.searchType.TAGS, null,DisplaySearchTagsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }


    public void scrollToTop(){
        if( lv_search_tags != null) {
            lv_search_tags.setAdapter(searchTagsAdapter);
        }
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public void onRetrieveSearch(APIResponse apiResponse) {

        searchTagsAdapter = new SearchTagsAdapter(context, tags);
        this.max_id = apiResponse.getMax_id();
        loader.setVisibility(View.GONE);
        if (apiResponse.getError() != null) {
            if( apiResponse.getError().getError() != null)
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        lv_search_tags.setVisibility(View.VISIBLE);
        List<String> newTags = apiResponse.getResults().getHashtags();
        tags.addAll(newTags);
        SearchTagsAdapter searchTagsAdapter = new SearchTagsAdapter(context, tags);
        lv_search_tags.setAdapter(searchTagsAdapter);
        searchTagsAdapter.notifyDataSetChanged();

    }

}
