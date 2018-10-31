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


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 08/09/2018.
 * Adapter for thread decoration
 */
public class ConversationDecoration extends RecyclerView.ItemDecoration{

    private Drawable divider;
    private Context context;

    public ConversationDecoration(Context context){
        divider = ContextCompat.getDrawable(context,R.drawable.line_divider);
        this.context = context;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int left = parent.getPaddingLeft() + (int)Helper.convertDpToPixel(12, context);
        int right = left + (int)Helper.convertDpToPixel(4, context);
        int childCount = parent.getChildCount();


        int offSet = (int) Helper.convertDpToPixel(30, context);

        for (int i = 0; i < childCount; i++) {

            View child = parent.getChildAt(i);
            StatusListAdapter adapter = (StatusListAdapter) parent.getAdapter();
            int position = parent.getChildAdapterPosition(child);

            assert adapter != null;
            Status status = adapter.getItem(position);

            int top, bottom;
            if( status != null){
                Status statusBefore = null;
                if( position > 0)
                    statusBefore = adapter.getItem(position - 1);
                top = (statusBefore != null && statusBefore.getId().equals(status.getIn_reply_to_id()))?
                        child.getTop(): (child.getTop() + offSet);
                Status statusAfter = null;
                if( adapter.getItemCount() > position+1)
                    statusAfter = adapter.getItem(position + 1);
                bottom =  (statusAfter != null && status.getId().equals(statusAfter.getIn_reply_to_id()) )?
                    child.getBottom():child.getTop()+offSet;
                if( position == 0 && childCount > 1)
                    top = bottom - (int)Helper.convertDpToPixel(14, context);
                if( position == 0 && childCount  <= 1 )
                    top = bottom;
                divider.setBounds(left, top, right, bottom);
                divider.draw(canvas);
            }

        }

    }

}
