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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.PlaylistsActivity;
import app.fedilab.android.asynctasks.ManagePlaylistsAsyncTask;
import app.fedilab.android.asynctasks.RetrievePeertubeChannelsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Playlist;
import app.fedilab.android.drawers.PlaylistAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPlaylistActionInterface;
import app.fedilab.android.interfaces.OnRetrievePeertubeInterface;
import es.dmoral.toasty.Toasty;

import static app.fedilab.android.asynctasks.RetrievePeertubeInformationAsyncTask.peertubeInformation;


/**
 * Created by Thomas on 26/05/2019.
 * Fragment to display Playlists
 */
public class DisplayPlaylistsFragment extends Fragment implements OnPlaylistActionInterface, OnRetrievePeertubeInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private List<Playlist> playlists;
    private RelativeLayout mainLoader;
    private FloatingActionButton add_new;
    private PlaylistAdapter playlistAdapter;
    private RelativeLayout textviewNoAction;
    private HashMap<Integer, String> privacyToSend;
    private HashMap<String, String> channelToSend;
    private MaterialSpinner set_upload_channel;
    private MaterialSpinner set_upload_privacy;
    private HashMap<String, String> channels;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View for fragment is the same that fragment accounts
        View rootView = inflater.inflate(R.layout.fragment_playlists, container, false);

        context = getContext();
        playlists = new ArrayList<>();


        ListView lv_playlist = rootView.findViewById(R.id.lv_playlist);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader = rootView.findViewById(R.id.loader);
        RelativeLayout nextElementLoader = rootView.findViewById(R.id.loading_next_items);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        playlists = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(context, playlists, textviewNoAction);
        lv_playlist.setAdapter(playlistAdapter);
        asyncTask = new ManagePlaylistsAsyncTask(context, ManagePlaylistsAsyncTask.action.GET_PLAYLIST, null, null, null,DisplayPlaylistsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            add_new = ((MainActivity) context).findViewById(R.id.add_new);
        }catch (Exception ignored){}


        LinkedHashMap<String, String> translations = null;
        if( peertubeInformation != null && peertubeInformation.getTranslations() != null)
            translations = new LinkedHashMap<>(peertubeInformation.getTranslations());

        LinkedHashMap<Integer, String> privaciesInit = new LinkedHashMap<>(peertubeInformation.getPrivacies());
        Map.Entry<Integer,String> entryInt = privaciesInit.entrySet().iterator().next();
        privacyToSend = new HashMap<>();
        privacyToSend.put(entryInt.getKey(), entryInt.getValue());
        LinkedHashMap<Integer, String> privacies = new LinkedHashMap<>(peertubeInformation.getPrivacies());
        //Populate privacies
        String[] privaciesA = new String[privacies.size()];
        Iterator it = privacies.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if( translations == null || translations.size() == 0 || !translations.containsKey((String)pair.getValue()))
                privaciesA[i] =  (String)pair.getValue();
            else
                privaciesA[i] =  translations.get((String)pair.getValue());
            it.remove();
            i++;
        }




        if( add_new != null){
            add_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                    int style;
                    if (theme == Helper.THEME_DARK) {
                        style = R.style.DialogDark;
                    } else if (theme == Helper.THEME_BLACK){
                        style = R.style.DialogBlack;
                    }else {
                        style = R.style.Dialog;
                    }
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                    LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.add_playlist,   new LinearLayout(context), false);
                    dialogBuilder.setView(dialogView);
                    EditText display_name = dialogView.findViewById(R.id.display_name);
                    EditText description = dialogView.findViewById(R.id.description);
                    set_upload_channel = dialogView.findViewById(R.id.set_upload_channel);
                    set_upload_privacy = dialogView.findViewById(R.id.set_upload_privacy);

                    Helper.changeMaterialSpinnerColor(context, set_upload_privacy);
                    Helper.changeMaterialSpinnerColor(context, set_upload_channel);

                    new RetrievePeertubeChannelsAsyncTask(context, DisplayPlaylistsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    display_name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
                    description.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});

                    dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            if( display_name.getText() != null && display_name.getText().toString().trim().length() > 0 ) {

                                Playlist playlist = new Playlist();
                                playlist.setDisplayName(display_name.getText().toString().trim());
                                if( description.getText() != null && description.getText().toString().trim().length() > 0  ){
                                    playlist.setDescription(description.getText().toString().trim());
                                }
                                String idChannel = null;
                                if( channelToSend != null ) {
                                    Map.Entry<String, String> channelM = channelToSend.entrySet().iterator().next();
                                    idChannel = channelM.getValue();
                                    if( idChannel.length() > 0)
                                        playlist.setVideoChannelId(idChannel);
                                }
                                Map.Entry<Integer, String> privacyM =  privacyToSend.entrySet().iterator().next();
                                String label = privacyM.getValue();
                                String idPrivacy = String.valueOf(privacyM.getKey());
                                if( label.equals("Public") && (playlist.getVideoChannelId() == null || playlist.getVideoChannelId().equals(""))){
                                    Toasty.error(context, context.getString(R.string.error_channel_mandatory),Toast.LENGTH_LONG).show();
                                }else{
                                    if( privacyToSend != null){
                                        playlist.setPrivacy(privacyToSend);
                                    }
                                    //new ManagePlaylistsAsyncTask(context, ManagePlaylistsAsyncTask.action.CREATE_PLAYLIST, playlist, null, null, DisplayPlaylistsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    UploadNotificationConfig uploadConfig = new UploadNotificationConfig();
                                    uploadConfig.getCompleted().autoClear = true;
                                    try {
                                        String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
                                        new MultipartUploadRequest(context, "https://" + Helper.getLiveInstance(context) + "/api/v1/video-playlists/")
                                                //.addFileToUpload(uri.toString().replace("file://",""), "videofile")
                                                .addHeader("Authorization", "Bearer " + token)
                                                .setNotificationConfig(uploadConfig)
                                               // .addParameter("name", filename)
                                                .addParameter("videoChannelId", idChannel)
                                                .addParameter("privacy", idPrivacy)
                                                .addParameter("displayName", playlist.getDisplayName())
                                                .addParameter("description", playlist.getDescription())
                                                .setMaxRetries(1)
                                                .setDelegate(new UploadStatusDelegate() {
                                                    @Override
                                                    public void onProgress(Context context, UploadInfo uploadInfo) {
                                                        // your code here
                                                    }

                                                    @Override
                                                    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                                                                        Exception exception) {
                                                        // your code here
                                                        exception.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                                                        DisplayPlaylistsFragment displayPlaylistsFragment;
                                                        if( getActivity() == null)
                                                            return;
                                                        displayPlaylistsFragment = (DisplayPlaylistsFragment) getActivity().getSupportFragmentManager().findFragmentByTag("PLAYLISTS");
                                                        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                        if( displayPlaylistsFragment != null) {
                                                            ft.detach(displayPlaylistsFragment);
                                                            ft.attach(displayPlaylistsFragment);
                                                            ft.commit();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(Context context, UploadInfo uploadInfo) {
                                                        // your code here
                                                    }
                                                })
                                                .startUpload();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }

                                    dialog.dismiss();
                                    add_new.setEnabled(false);
                                }
                            }else{
                                Toasty.error(context, context.getString(R.string.error_display_name),Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });


                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.setTitle(getString(R.string.action_playlist_create));
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            //Hide keyboard
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            assert imm != null;
                            imm.hideSoftInputFromWindow(display_name.getWindowToken(), 0);
                        }
                    });
                    if( alertDialog.getWindow() != null )
                        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();
                }
            });
        }
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
    public void onActionDone(ManagePlaylistsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        mainLoader.setVisibility(View.GONE);
        add_new.setEnabled(true);
        if( apiResponse.getError() != null){
            Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }

        if( actionType == ManagePlaylistsAsyncTask.action.GET_PLAYLIST) {
            if (apiResponse.getPlaylists() != null && apiResponse.getPlaylists().size() > 0) {
                this.playlists.addAll(apiResponse.getPlaylists());
                playlistAdapter.notifyDataSetChanged();
                textviewNoAction.setVisibility(View.GONE);
            } else {
                textviewNoAction.setVisibility(View.VISIBLE);
            }
        }else if( actionType == ManagePlaylistsAsyncTask.action.CREATE_PLAYLIST){
            if (apiResponse.getPlaylists() != null && apiResponse.getPlaylists().size() > 0) {
                Intent intent = new Intent(context, PlaylistsActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("playlist", apiResponse.getPlaylists().get(0));
                intent.putExtras(b);
                context.startActivity(intent);
                this.playlists.add(0, apiResponse.getPlaylists().get(0));
                playlistAdapter.notifyDataSetChanged();
                textviewNoAction.setVisibility(View.GONE);
            }else{
                Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            }
        }else if( actionType == ManagePlaylistsAsyncTask.action.DELETE_PLAYLIST){
            if( this.playlists.size() == 0)
                textviewNoAction.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onRetrievePeertube(APIResponse apiResponse) {

    }

    @Override
    public void onRetrievePeertubeComments(APIResponse apiResponse) {

    }

    @Override
    public void onRetrievePeertubeChannels(APIResponse apiResponse) {
        if (apiResponse.getError() != null || apiResponse.getAccounts() == null || apiResponse.getAccounts().size() == 0) {
            if (apiResponse.getError() != null && apiResponse.getError().getError() != null)
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(context, getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }

        //Populate channels
        List<Account> accounts = apiResponse.getAccounts();
        String[] channelName = new String[accounts.size()+1];
        String[] channelId= new String[accounts.size()+1];
        int i = 1;
        channelName[0] = "";
        channelId[0] = "";
        channels = new HashMap<>();
        for(Account account: accounts){
            channels.put(account.getUsername(),account.getId());
            channelName[i] = account.getUsername();
            channelId[i] = account.getId();
            i++;
        }

        channelToSend = new HashMap<>();
        channelToSend.put(channelName[0], channelId[0]);
        ArrayAdapter<String> adapterChannel = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, channelName);
        set_upload_channel.setAdapter(adapterChannel);

        LinkedHashMap<String, String> translations = null;
        if (peertubeInformation.getTranslations() != null)
            translations = new LinkedHashMap<>(peertubeInformation.getTranslations());

        LinkedHashMap<Integer, String> privaciesInit = new LinkedHashMap<>(peertubeInformation.getPlaylistPrivacies());
        Map.Entry<Integer, String> entryInt = privaciesInit.entrySet().iterator().next();
        privacyToSend = new HashMap<>();
        privacyToSend.put(entryInt.getKey(), entryInt.getValue());
        LinkedHashMap<Integer, String> privacies = new LinkedHashMap<>(peertubeInformation.getPlaylistPrivacies());
        //Populate privacies
        String[] privaciesA = new String[privacies.size()];
        Iterator it = privacies.entrySet().iterator();
        i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (translations == null || translations.size() == 0 || !translations.containsKey((String) pair.getValue()))
                privaciesA[i] = (String) pair.getValue();
            else
                privaciesA[i] = translations.get((String) pair.getValue());
            it.remove();
            i++;
        }

        ArrayAdapter<String> adapterPrivacies = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, privaciesA);
        set_upload_privacy.setAdapter(adapterPrivacies);

        //Manage privacies
        set_upload_privacy.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                LinkedHashMap<Integer, String> privaciesCheck = new LinkedHashMap<>(peertubeInformation.getPrivacies());
                Iterator it = privaciesCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (i == position) {
                        privacyToSend = new HashMap<>();
                        privacyToSend.put((Integer) pair.getKey(), (String) pair.getValue());
                        break;
                    }
                    it.remove();
                    i++;
                }
            }
        });
        //Manage languages
        set_upload_channel.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                LinkedHashMap<String, String> channelsCheck = new LinkedHashMap<>(channels);
                Iterator it = channelsCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    if( i == position){
                        channelToSend = new HashMap<>();
                        channelToSend.put((String)pair.getKey(), (String)pair.getValue());
                        break;
                    }
                    it.remove();
                    i++;
                }
            }
        });
    }
}
