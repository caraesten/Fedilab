package app.fedilab.android.drawers;
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import app.fedilab.android.R;
import app.fedilab.android.client.Entities.Suggestion;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 19/07/2019.
 * Adapter for suggestions results
 */
public class SuggestionsAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Suggestion> suggestions;

    private LayoutInflater layoutInflater;

    public SuggestionsAdapter(Context context, List<Suggestion> suggestions){
        this.context = context;
        this.suggestions = suggestions;
        layoutInflater = LayoutInflater.from(context);
    }


    public Suggestion getItem(int position) {
       return suggestions.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_suggestions, parent, false));
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView suggestion_content;
        private ImageView suggestion_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestion_content = itemView.findViewById(R.id.suggestion_content);
            suggestion_image = itemView.findViewById(R.id.suggestion_image);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final ViewHolder holder = (ViewHolder) viewHolder;
        final Suggestion suggestion = getItem(i);

        if( suggestion.getType() == Suggestion.suggestionType.TAG) {
            holder.suggestion_content.setText(String.format("#%s", suggestion.getContent()));
            holder.suggestion_image.setVisibility(View.GONE);
        }else{
            holder.suggestion_content.setText(suggestion.getContent());
            Helper.loadGiF(context, suggestion.getImageUrl(), holder.suggestion_image);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }



}