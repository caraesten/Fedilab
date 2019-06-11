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
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.HowToVideo;
import app.fedilab.android.drawers.HowToVideosAdapter;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveHowToAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveHowToInterface;


/**
 * Created by Thomas on 29/09/2018.
 * Fragment to display how to videos
 */
public class DisplayHowToFragment extends Fragment implements OnRetrieveHowToInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;

    private RelativeLayout mainLoader;
    private ListView lv_howto;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View for fragment is the same that fragment accounts
        View rootView = inflater.inflate(R.layout.fragment_how_to, container, false);

        context = getContext();
        lv_howto = rootView.findViewById(R.id.lv_howto);
        mainLoader = rootView.findViewById(R.id.loader);
        mainLoader.setVisibility(View.VISIBLE);



        asyncTask = new RetrieveHowToAsyncTask(context,  DisplayHowToFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


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
    public void onRetrieveHowTo(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<HowToVideo> howToVideos = apiResponse.getHowToVideos();
        if( howToVideos == null || howToVideos.size() == 0 ){
            Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            return;
        }
        HowToVideosAdapter howToVideosAdapter = new HowToVideosAdapter(context, howToVideos);
        lv_howto.setAdapter(howToVideosAdapter);
    }
}
