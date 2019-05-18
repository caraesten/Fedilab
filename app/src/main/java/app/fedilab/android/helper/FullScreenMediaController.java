package app.fedilab.android.helper;
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
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;

import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.R;
import app.fedilab.android.activities.PeertubeActivity;


/**
 * Created by Thomas on 14/10/2018.
 * FullScreenMediaController. Inspired from the work at http://www.zoftino.com/android-videoview-playing-videos-full-screen
 */
public class FullScreenMediaController extends MediaController {

    private ImageButton fullScreen;
    private Button resolution;
    private Context context;
    private Peertube peertube;
    private String resolutionVal;

    public enum fullscreen{
        OFF,
        ON
    }
    public FullScreenMediaController(Context context) {
        super(context);
        this.context = context;
    }

    public FullScreenMediaController(Context context, Peertube peertube) {
        super(context);
        this.peertube = peertube;
        this.context = context;
    }

    @Override
    public void setAnchorView(View view) {

        super.setAnchorView(view);

        //image button for full screen to be added to media controller
        fullScreen = new ImageButton(context);
        LayoutParams params =
                new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.rightMargin = 80;
        params.topMargin = 22;
        addView(fullScreen, params);

        if( resolutionVal == null)
            resolutionVal = peertube.getResolution().get(0) +"p";
        resolution = new Button(context);
        resolution.setAllCaps(false);
        resolution.setBackgroundColor(Color.TRANSPARENT);
        resolution.setText(resolutionVal);
        resolution.setPadding(0,0,0,0);
        LayoutParams paramsButton =
                new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        paramsButton.gravity = Gravity.START;
        paramsButton.rightMargin = 80;
        paramsButton.topMargin = 22;
        resolution.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PeertubeActivity)getContext()).displayResolution();
            }
        });
        addView(resolution, paramsButton);

        if(((PeertubeActivity)getContext()).getFullscreen() == fullscreen.ON){
            Resources resources = getResources();
            fullScreen.setImageDrawable(resources.getDrawable(R.drawable.ic_fullscreen_exit));
        }else{
            Resources resources = getResources();
            fullScreen.setImageDrawable(resources.getDrawable(R.drawable.ic_fullscreen));
        }

        //add listener to image button to handle full screen and exit full screen events
        fullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((PeertubeActivity)getContext()).getFullscreen() == fullscreen.ON){
                    ((PeertubeActivity)getContext()).setFullscreen(fullscreen.OFF);
                }else{
                    ((PeertubeActivity)getContext()).setFullscreen(fullscreen.ON);
                }
                ((PeertubeActivity)getContext()).change();
                changeIcon();
            }
        });
    }

    public void setResolutionVal(String resolutionVal){
        this.resolutionVal = resolutionVal;
        if( resolution != null)
            resolution.setText(String.format("%sp",resolutionVal));
    }

    public void changeIcon(){
        //fullscreen indicator from intent
        if(((PeertubeActivity)getContext()).getFullscreen() == fullscreen.ON){
            Resources resources = getResources();
            fullScreen.setImageDrawable(resources.getDrawable(R.drawable.ic_fullscreen_exit));
        }else{
            Resources resources = getResources();
            fullScreen.setImageDrawable(resources.getDrawable(R.drawable.ic_fullscreen));
        }
    }
}