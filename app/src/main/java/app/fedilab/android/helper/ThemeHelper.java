package app.fedilab.android.helper;

import android.app.Activity;

import com.jaredrummler.cyanea.Cyanea;

import app.fedilab.android.R;

public class ThemeHelper {


    public static void changeTheme(Activity activity){


        Cyanea.Editor editor = Cyanea.getInstance().edit();
        editor.accent(R.color.colorAccent);
        editor.background(R.color.black);
        editor.backgroundDarkResource(R.color.black);
        editor.apply();

        if( activity != null){
            activity.recreate();
        }
    }
}
