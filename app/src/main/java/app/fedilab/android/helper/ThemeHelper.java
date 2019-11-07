package app.fedilab.android.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;

import com.afollestad.aesthetic.Aesthetic;


import app.fedilab.android.R;


import static android.content.Context.MODE_PRIVATE;

public class ThemeHelper {


    public static void changeTheme(Context context, int theme){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);


        Aesthetic.get()
                .activityTheme(theme)
                .attribute(R.attr.cardviewColor,null, R.color.mastodonC1, true)
                .colorAccent(null, R.color.mastodonC4)
                .colorPrimary(null,R.color.mastodonC1)
                .colorPrimaryDark(null,R.color.mastodonC1)
                .colorNavigationBar(null,R.color.mastodonC1)
                .colorStatusBar(null,R.color.mastodonC1)
                .toolbarIconColor(null,R.color.white)
                .colorWindowBackground(null,R.color.mastodonC1)



        .apply();
    }

    public static int getAttColor(Context context, int attColor){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attColor, typedValue, true);
        return typedValue.data;
    }
}
