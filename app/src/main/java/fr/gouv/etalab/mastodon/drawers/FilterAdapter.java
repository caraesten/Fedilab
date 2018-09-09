package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2018 Thomas Schneider
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


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.BaseMainActivity;
import fr.gouv.etalab.mastodon.asynctasks.ManageFiltersAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Filters;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnFilterActionInterface;


/**
 * Created by Thomas on 05/09/2018.
 * Adapter for filters
 */
public class FilterAdapter extends BaseAdapter implements OnFilterActionInterface {

    private List<fr.gouv.etalab.mastodon.client.Entities.Filters> filters;
    private LayoutInflater layoutInflater;
    private Context context;
    private FilterAdapter filterAdapter;
    private RelativeLayout textviewNoAction;

    public FilterAdapter(Context context, List<fr.gouv.etalab.mastodon.client.Entities.Filters> filters, RelativeLayout textviewNoAction){
        this.filters = filters;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.filterAdapter = this;
        this.textviewNoAction = textviewNoAction;
    }

    @Override
    public int getCount() {
        return filters.size();
    }

    @Override
    public Object getItem(int position) {
        return filters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final fr.gouv.etalab.mastodon.client.Entities.Filters filter = filters.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_filters, parent, false);
            holder = new ViewHolder();
            holder.filter_word = convertView.findViewById(R.id.filter_word);
            holder.filter_context = convertView.findViewById(R.id.filter_context);
            holder.edit_filter = convertView.findViewById(R.id.edit_filter);
            holder.delete_filter = convertView.findViewById(R.id.delete_filter);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.filter_word.setText(filter.getPhrase());
        StringBuilder contextString = new StringBuilder();
        if( filter.getContext() != null)
            for(String ct: filter.getContext())
                contextString.append(ct).append(" ");
        holder.filter_context.setText(contextString.toString());

        holder.edit_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = ((BaseMainActivity)context).getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.add_filter, null);
                dialogBuilder.setView(dialogView);

                EditText add_phrase = dialogView.findViewById(R.id.add_phrase);
                CheckBox context_home = dialogView.findViewById(R.id.context_home);
                CheckBox context_public = dialogView.findViewById(R.id.context_public);
                CheckBox context_notification = dialogView.findViewById(R.id.context_notification);
                CheckBox context_conversation = dialogView.findViewById(R.id.context_conversation);
                CheckBox context_whole_word = dialogView.findViewById(R.id.context_whole_word);
                CheckBox context_drop = dialogView.findViewById(R.id.context_drop);
                Spinner filter_expire = dialogView.findViewById(R.id.filter_expire);
                ArrayAdapter<CharSequence> adapterResize = ArrayAdapter.createFromResource(context,
                        R.array.filter_expire, android.R.layout.simple_spinner_item);
                filter_expire.setAdapter(adapterResize);
                final int[] expire = {-1};
                filter_expire.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0:
                                expire[0] = -1;
                                break;
                            case 1:
                                expire[0] = 3600;
                                break;
                            case 2:
                                expire[0] = 21600;
                                break;
                            case 3:
                                expire[0] = 43200;
                                break;
                            case 4:
                                expire[0] = 86400;
                                break;
                            case 5:
                                expire[0] = 604800;
                                break;
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                add_phrase.setText(filter.getPhrase());
                if( filter.getContext() != null)
                    for(String val: filter.getContext()){
                        switch (val) {
                            case "home":
                                context_home.setChecked(true);
                                break;
                            case "public":
                                context_public.setChecked(true);
                                break;
                            case "notifications":
                                context_notification.setChecked(true);
                                break;
                            case "thread":
                                context_conversation.setChecked(true);
                                break;
                        }
                    }
                context_whole_word.setChecked(filter.isWhole_word());
                context_drop.setChecked(filter.isIrreversible());

                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if( add_phrase.getText() != null && add_phrase.getText().toString().trim().length() > 0 ) {
                            Filters filterSent = new Filters();
                            filterSent.setId(filter.getId());
                            ArrayList<String> contextFilter = new ArrayList<>();
                            if( context_home.isChecked())
                                contextFilter.add("home");
                            if( context_public.isChecked())
                                contextFilter.add("public");
                            if( context_notification.isChecked())
                                contextFilter.add("notifications");
                            if( context_conversation.isChecked())
                                contextFilter.add("thread");
                            filterSent.setContext(contextFilter);
                            filterSent.setExpires_in(expire[0]);
                            filterSent.setPhrase(add_phrase.getText().toString());
                            filterSent.setWhole_word(context_whole_word.isChecked());
                            filterSent.setIrreversible(context_drop.isChecked());
                            new ManageFiltersAsyncTask(context, ManageFiltersAsyncTask.action.UPDATE_FILTER, filterSent, FilterAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });


                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setTitle(context.getString(R.string.action_update_filter));
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //Hide keyboard
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(add_phrase.getWindowToken(), 0);
                    }
                });
                if( alertDialog.getWindow() != null )
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();
            }
        });
        holder.delete_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.action_filter_delete) );
                builder.setMessage(context.getString(R.string.action_lists_confirm_delete) );
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                filters.remove(filter);
                                filterAdapter.notifyDataSetChanged();
                                new ManageFiltersAsyncTask(context, ManageFiltersAsyncTask.action.DELETE_FILTER,filter, FilterAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                if( filters.size() == 0 && textviewNoAction != null && textviewNoAction.getVisibility() == View.GONE)
                                    textviewNoAction.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        return convertView;
    }


    private Filters getItemAt(int position){
        if( filters.size() > position)
            return filters.get(position);
        else
            return null;
    }

    @Override
    public void onActionDone(ManageFiltersAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        if( apiResponse != null) {
            if (apiResponse.getError() != null) {
                Toast.makeText(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            }
            List<Filters> filtersRes = apiResponse.getFilters();
            if (filtersRes != null && filtersRes.size() > 0) {
                notifyStatusChanged(filtersRes.get(0));
            }
        }
    }

    private void notifyStatusChanged(Filters filter){
        for (int i = 0; i < filterAdapter.getCount(); i++) {
            //noinspection ConstantConditions
            if (filterAdapter.getItemAt(i) != null && filterAdapter.getItemAt(i).getId().equals(filter.getId())) {
                filters.set(i, filter);
                try {
                    filterAdapter.notifyDataSetChanged();
                } catch (Exception ignored) {
                }
                return;
            }
        }
    }

    private class ViewHolder {
        TextView filter_word;
        TextView filter_context;
        FloatingActionButton edit_filter;
        FloatingActionButton delete_filter;
    }


}