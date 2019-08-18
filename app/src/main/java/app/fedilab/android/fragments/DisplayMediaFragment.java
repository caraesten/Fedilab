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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.ImageAdapter;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;



/**
 * Created by Thomas on 05/09/2018.
 * Fragment to display media related to status
 */
public class DisplayMediaFragment extends Fragment implements OnRetrieveFeedsInterface {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private String max_id;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private String targetedId;
    private boolean showMediaOnly, showPinned, showReply;
    boolean firstTootsLoaded;
    private SharedPreferences sharedpreferences;
    private ArrayList<Status> statuses;
    private ImageAdapter gridAdaper;
    private RecyclerView gridview;

    public DisplayMediaFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status_media, container, false);

        context = getContext();
        Bundle bundle = this.getArguments();
        showMediaOnly = true;
        //Will allow to load first toots if bookmark != null
        firstTootsLoaded = true;
        showPinned = false;
        max_id = null;
        flag_loading = true;
        showReply = false;
        firstLoad = true;
        assert context != null;
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        mainLoader =  rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_status);
        textviewNoAction =  rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        if (bundle != null) {
            targetedId = bundle.getString("targetedid", null);
        }

        statuses = new ArrayList<>();
        gridAdaper = new ImageAdapter(context, statuses);
        gridview = rootView.findViewById(R.id.gridview_media);

        gridview.setAdapter(gridAdaper);

        GridLayoutManager gvLayout = new GridLayoutManager(context, 3);
        gridview.setLayoutManager(gvLayout);


        gridview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = gvLayout.findFirstVisibleItemPosition();
                if (dy > 0) {
                    int visibleItemCount = gvLayout.getChildCount();
                    int totalItemCount = gvLayout.getItemCount();


                    if (firstVisibleItem + visibleItemCount == totalItemCount && context != null) {
                        if (!flag_loading) {
                            flag_loading = true;
                            asyncTask = new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.USER, targetedId, max_id, showMediaOnly, showPinned, showReply, DisplayMediaFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });


        if( context != null) {
            asyncTask = new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.USER, targetedId, max_id, showMediaOnly, showPinned, showReply,DisplayMediaFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if( context != null){
                        asyncTask = new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.USER, targetedId, max_id, showMediaOnly, showPinned, showReply,DisplayMediaFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }, 500);
        }

        return rootView;
    }


    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }



    @Override
    public void onDestroyView() {
        if(gridview  != null) {
            gridview.setAdapter(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy (){
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }



    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        if(apiResponse == null || apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404 ){
            if( apiResponse != null && apiResponse.getError() != null && apiResponse.getError().getError() != null)
                Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            else
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            flag_loading = false;
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        max_id = apiResponse.getMax_id();
        if( this.statuses == null)
            this.statuses = new ArrayList<>();
        int previousPosition = this.statuses.size();
        flag_loading = (max_id == null );
        if( firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        List<Status> convertedStatuses = new ArrayList<>();
        if( apiResponse.getStatuses() != null && apiResponse.getStatuses().size() > 0){
            for( Status status: apiResponse.getStatuses()){
                if( status.getMedia_attachments() != null ) {
                    String statusSerialized = Helper.statusToStringStorage(status);
                    for (Attachment attachment : status.getMedia_attachments()) {
                        Status newStatus = Helper.restoreStatusFromString(statusSerialized);
                        if (newStatus == null)
                            break;
                        newStatus.setArt_attachment(attachment);
                        convertedStatuses.add(newStatus);
                    }
                }
            }
        }
        if(  convertedStatuses.size() > 0) {
            this.statuses.addAll(convertedStatuses);
            gridAdaper.notifyItemRangeInserted(previousPosition, this.statuses.size());
        }
        firstLoad = false;
    }


}
