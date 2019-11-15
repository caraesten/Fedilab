package app.fedilab.android.helper;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;

import static android.content.Context.MODE_PRIVATE;

public class ThemeHelper {


    public static void changeTheme(Context context, int theme) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

    }

    public static int getAttColor(Context context, int attColor) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attColor, typedValue, true);
        return typedValue.data;
    }
}
