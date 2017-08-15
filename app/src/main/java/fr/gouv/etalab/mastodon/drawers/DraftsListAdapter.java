package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2017 Thomas Schneider
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


import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 15/07/2017.
 * Adapter for toot drafts
 */
public class DraftsListAdapter extends BaseAdapter  {

    private List<StoredStatus> storedStatuses;
    private LayoutInflater layoutInflater;
    private Context context;
    private DraftsListAdapter draftsListAdapter;

    public DraftsListAdapter(Context context, List<StoredStatus> storedStatuses){
        this.storedStatuses = storedStatuses;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        draftsListAdapter = this;
    }

    @Override
    public int getCount() {
        return storedStatuses.size();
    }

    @Override
    public Object getItem(int position) {
        return storedStatuses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final StoredStatus draft = storedStatuses.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_draft, parent, false);
            holder = new ViewHolder();
            holder.draft_title = (TextView) convertView.findViewById(R.id.draft_title);
            holder.draft_date = (TextView) convertView.findViewById(R.id.draft_date);
            holder.draft_delete = (ImageView) convertView.findViewById(R.id.draft_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(context, R.drawable.ic_cancel,R.color.dark_text);
        }else {
            changeDrawableColor(context, R.drawable.ic_cancel,R.color.black);
        }
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if(draft.getStatus() != null && draft.getStatus().getContent() != null ) {
            if (draft.getStatus().getContent().length() > 20)
                holder.draft_title.setText(draft.getStatus().getContent().substring(0, 20));
            else
                holder.draft_title.setText(draft.getStatus().getContent());
        }
        holder.draft_date.setText(Helper.dateToString(context, draft.getCreation_date()));
        holder.draft_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(draft.getStatus().getContent() + '\n' + Helper.dateToString(context, draft.getCreation_date()));
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.remove_draft)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new StatusStoredDAO(context, db).remove(draft.getId());
                                storedStatuses.remove(draft);
                                draftsListAdapter.notifyDataSetChanged();
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


    private class ViewHolder {
        TextView draft_title;
        TextView draft_date;
        ImageView draft_delete;
    }


}