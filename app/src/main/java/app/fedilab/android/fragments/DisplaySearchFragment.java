package app.fedilab.android.fragments;
/* Copyright 2017 Thomas Schneider
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.drawers.SearchTootsListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.SearchDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.R;

import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 22/11/2017.
 * Fragment to display search with keywords
 */
public class DisplaySearchFragment extends Fragment {


    private Context context;
    private SearchTootsListAdapter searchTootsListAdapter;
    private List<String> searches;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        final ListView lv_search_toots = rootView.findViewById(R.id.lv_search_toots);

        RelativeLayout mainLoader = rootView.findViewById(R.id.loader);
        final RelativeLayout textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        searches = new SearchDAO(context, db).getAllSearch();
        if( searches == null)
            searches = new ArrayList<>();
        searchTootsListAdapter = new SearchTootsListAdapter(context, searches, textviewNoAction);
        lv_search_toots.setAdapter(searchTootsListAdapter);
        searchTootsListAdapter.notifyDataSetChanged();
        if( searches.size() == 0) {
            textviewNoAction.setVisibility(View.VISIBLE);
        }
        mainLoader.setVisibility(View.GONE);
        FloatingActionButton add_new = null;
        try {
            add_new = ((MainActivity) context).findViewById(R.id.add_new);
        }catch (Exception ignored){}
        if( add_new != null)
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
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.search_toot,   new LinearLayout(context), false);
                    dialogBuilder.setView(dialogView);
                    final EditText editText = dialogView.findViewById(R.id.search_toot);
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                    dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String keyword = editText.getText().toString().trim();
                            keyword= keyword.replaceAll("^#+", "");
                            //Empty
                            if( keyword.length() == 0)
                                return;
                            //Already in db
                            List<String> s_ = new SearchDAO(context, db).getSearchByKeyword(keyword);
                            if( s_ == null)
                                s_ = new ArrayList<>();
                            if( s_.size() > 0){
                                return;
                            }
                            new SearchDAO(context, db).insertSearch(keyword);
                            if( getActivity() != null)
                                getActivity().recreate();
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra(Helper.INTENT_ACTION, Helper.SEARCH_TAG);
                            intent.putExtra(Helper.SEARCH_KEYWORD, keyword);
                            startActivity(intent);
                        }
                    });
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            //Hide keyboard
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            assert imm != null;
                            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        }
                    });
                    if( alertDialog.getWindow() != null )
                        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();
                }
            });
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


}
