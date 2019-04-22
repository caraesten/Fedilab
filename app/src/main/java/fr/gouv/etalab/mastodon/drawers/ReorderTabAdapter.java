package fr.gouv.etalab.mastodon.drawers;
/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.Entities.ManageTimelines;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.ItemTouchHelperAdapter;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.ItemTouchHelperViewHolder;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.OnStartDragListener;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TimelinesDAO;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Simple RecyclerView.Adapter that implements {@link ItemTouchHelperAdapter} to respond to move and
 * dismiss events from a {@link android.support.v7.widget.helper.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class ReorderTabAdapter extends RecyclerView.Adapter<ReorderTabAdapter.ItemViewHolder>  implements ItemTouchHelperAdapter {

    private List<ManageTimelines> mItems;

    private final OnStartDragListener mDragStartListener;

    private Context context;
    private SharedPreferences sharedpreferences;
    public ReorderTabAdapter(Context context, List<ManageTimelines> manageTimelines, OnStartDragListener dragStartListener) {
        this. mDragStartListener = dragStartListener;
        this.mItems = manageTimelines;
        this.context = context;
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
    }

    @NotNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_reorder, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final ItemViewHolder holder, int position) {


        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        ManageTimelines tl = mItems.get(position);
        switch (tl.getType()){
            case HOME:
                holder.iconView.setImageResource(R.drawable.ic_home);
                holder.textView.setText(context.getString(R.string.home_menu));
                break;
            case NOTIFICATION:
                holder.iconView.setImageResource(R.drawable.ic_notifications);
                holder.textView.setText(context.getString(R.string.notifications));
                break;
            case DIRECT:
                holder.iconView.setImageResource(R.drawable.ic_direct_messages);
                holder.textView.setText(context.getString(R.string.direct_message));
                break;
            case LOCAL:
                holder.iconView.setImageResource(R.drawable.ic_people);
                holder.textView.setText(context.getString(R.string.local_menu));
                break;
            case PUBLIC:
                holder.iconView.setImageResource(R.drawable.ic_public);
                holder.textView.setText(context.getString(R.string.global_menu));
                break;
            case ART:
                holder.iconView.setImageResource(R.drawable.ic_color_lens);
                holder.textView.setText(context.getString(R.string.art_menu));
                break;
            case PEERTUBE:
                holder.iconView.setImageResource(R.drawable.ic_video_peertube);
                holder.textView.setText(context.getString(R.string.peertube_menu));
                break;
            case INSTANCE:
                switch ( tl.getRemoteInstance().getType()){
                    case "PEERTUBE":
                        holder.iconView.setImageResource(R.drawable.peertube_icon);
                        break;
                    case "MASTODON":
                        holder.iconView.setImageResource(R.drawable.mastodon_icon);
                        break;
                    case "PIXELFED":
                        holder.iconView.setImageResource(R.drawable.pixelfed);
                        break;
                    case "MISSKEY":
                        holder.iconView.setImageResource(R.drawable.misskey);
                        break;
                }
                holder.textView.setText( tl.getRemoteInstance().getHost());
                break;
            case TAG:
                holder.iconView.setImageResource(R.drawable.ic_tag_timeline);
                if(  tl.getTagTimeline().getDisplayname() != null)
                    holder.textView.setText( tl.getTagTimeline().getDisplayname());
                else
                    holder.textView.setText( tl.getTagTimeline().getName());
                break;
            case LIST:
                holder.iconView.setImageResource(R.drawable.ic_list);
                holder.textView.setText( tl.getListTimeline().getTitle());
                break;
        }
        if( tl.getType() != ManageTimelines.Type.INSTANCE){
            if (theme == THEME_LIGHT) {
                holder.iconView.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            } else {
                holder.iconView.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            }
        }else{
            holder.iconView.setColorFilter(null);
        }

        if (theme == THEME_LIGHT) {
            holder.handleView.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            holder.hideView.setColorFilter(ContextCompat.getColor(context, R.color.action_light_header), PorterDuff.Mode.SRC_IN);
        } else {
            holder.handleView.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
            holder.hideView.setColorFilter(ContextCompat.getColor(context, R.color.dark_text), PorterDuff.Mode.SRC_IN);
        }

        if(tl.isDisplayed()){
            holder.hideView.setImageResource(R.drawable.ic_make_tab_visible);
        }else{
            holder.hideView.setImageResource(R.drawable.ic_make_tab_unvisible);
        }

        holder.hideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tl.setDisplayed(! tl.isDisplayed());
                if(tl.isDisplayed()){
                    holder.hideView.setImageResource(R.drawable.ic_make_tab_visible);
                }else{
                    holder.hideView.setImageResource(R.drawable.ic_make_tab_unvisible);
                }
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                new TimelinesDAO(context, db).update(tl);
            }
        });

        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        int i = 0;
        for(ManageTimelines timelines: mItems){
           timelines.setPosition(i);
           new TimelinesDAO(context, db).update(timelines);
           i++;
        }

       return true;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        final TextView textView;
        final ImageView handleView;
        final ImageView hideView;
        final ImageView iconView;

        ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            handleView = itemView.findViewById(R.id.handle);
            iconView = itemView.findViewById(R.id.icon);
            hideView = itemView.findViewById(R.id.hide);
        }

        @Override
        public void onItemSelected() {
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
