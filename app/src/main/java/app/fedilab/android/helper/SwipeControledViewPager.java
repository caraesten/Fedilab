package app.fedilab.android.helper;

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
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;


public class SwipeControledViewPager extends ViewPager {
    private boolean enableSwipe;

    public SwipeControledViewPager(Context context) {
        super(context);
        init();
    }

    public SwipeControledViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        enableSwipe = true;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        //Disable horizontal scrolling
        if (enableSwipe) {
            return super.canScrollHorizontally(direction);
        } else {
            return false;
        }
    }



    public void setEnableSwipe(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
    }
}
