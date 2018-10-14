package fr.gouv.etalab.mastodon.helper;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.PeertubeActivity;

public class FullScreenMediaController extends MediaController {

    private ImageButton fullScreen;

    public enum fullscreen{
        OFF,
        ON
    }
    public FullScreenMediaController(Context context) {
        super(context);
    }


    @Override
    public void setAnchorView(View view) {

        super.setAnchorView(view);

        //image button for full screen to be added to media controller
        fullScreen = new ImageButton(super.getContext());

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        params.rightMargin = 80;
        addView(fullScreen, params);

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

    private void changeIcon(){
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