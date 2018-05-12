package fr.gouv.etalab.mastodon.helper;

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
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Created by Thomas on 12/05/2018.
 * Allows to fix crashes with selection see: https://stackoverflow.com/a/36740247
 */

public class CustomTextView extends android.support.v7.widget.AppCompatTextView {


    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        int startSelection = getSelectionStart();
        int endSelection = getSelectionEnd();
        if (startSelection < 0 || endSelection < 0){
            Selection.setSelection((Spannable) getText(), getText().length());
        } else if (startSelection != endSelection) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                final CharSequence text = getText();
                setText(null);
                setText(text);
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
