package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2017 Thomas Schneider
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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.drawers.ListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnListActionInterface;


/**
 * Created by Thomas on 13/12/2017.
 * Fragment to display Lists
 */
public class DisplayListsFragment extends Fragment implements OnListActionInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private ListAdapter listAdapter;
    private List<fr.gouv.etalab.mastodon.client.Entities.List> lists;
    private TextView no_action_text;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View for fragment is the same that fragment accounts
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        context = getContext();
        lists = new ArrayList<>();


        ListView lv_lists = rootView.findViewById(R.id.lv_lists);
        no_action_text = rootView.findViewById(R.id.no_action_text);
        RelativeLayout mainLoader = rootView.findViewById(R.id.loader);
        RelativeLayout nextElementLoader = rootView.findViewById(R.id.loading_next_items);
        RelativeLayout textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        listAdapter = new ListAdapter(context, this.lists, textviewNoAction);
        lv_lists.setAdapter(listAdapter);

        no_action_text.setVisibility(View.GONE);
        asyncTask = new ManageListsAsyncTask(context, ManageListsAsyncTask.action.GET_LIST, null, null, null, null, DisplayListsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }



    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }


    @Override
    public void onActionDone(APIResponse apiResponse, int statusCode) {
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        if( this.lists != null && this.lists.size() > 0) {
            this.lists = apiResponse.getLists();
            listAdapter.notifyDataSetChanged();
        }else {
            no_action_text.setVisibility(View.VISIBLE);
        }
    }
}
