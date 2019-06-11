package app.fedilab.android.helper;

/* Copyright 2018 Thomas Schneider
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
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Paint;
import androidx.annotation.DimenRes;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;

import com.vanniktech.emoji.EmojiManager;

import app.fedilab.android.R;


/**
 * Created by Thomas on 12/05/2018.
 * Allows to fix crashes with selection see: https://stackoverflow.com/a/36740247
 */

public class CustomTextView extends AppCompatTextView {

    private float emojiSize;
    private boolean emoji;
    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        emoji = sharedpreferences.getBoolean(Helper.SET_DISPLAY_EMOJI, true);

        final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
        final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;

        if (attrs == null) {
            emojiSize = defaultEmojiSize;
        } else {
            @SuppressLint("CustomViewStyleable") final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EmojiTextView);

            try {
                emojiSize = a.getDimension(R.styleable.EmojiTextView_emojiSize, defaultEmojiSize);
            } finally {
                a.recycle();
            }
        }
        setText(getText());
    }

    @Override
    public void setText(final CharSequence rawText, final BufferType type) {
        if( emoji) {
            final CharSequence text = rawText == null ? "" : rawText;
            final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
            final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
            final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
            EmojiManager.getInstance().replaceWithImages(getContext(), spannableStringBuilder, emojiSize, defaultEmojiSize);
            super.setText(spannableStringBuilder, type);
        }else {
            super.setText(rawText, type);
        }

    }

    /** sets the emoji size in pixels and automatically invalidates the text and renders it with the new size */
    public final void setEmojiSize(@Px final int pixels) {
        setEmojiSize(pixels, true);
    }

    /** sets the emoji size in pixels and automatically invalidates the text and renders it with the new size when {@code shouldInvalidate} is true */
    public final void setEmojiSize(@Px final int pixels, final boolean shouldInvalidate) {
        emojiSize = pixels;

        if (shouldInvalidate) {
            setText(getText());
        }
    }

    /** sets the emoji size in pixels with the provided resource and automatically invalidates the text and renders it with the new size */
    public final void setEmojiSizeRes(@DimenRes final int res) {
        setEmojiSizeRes(res, true);
    }

    /** sets the emoji size in pixels with the provided resource and invalidates the text and renders it with the new size when {@code shouldInvalidate} is true */
    public final void setEmojiSizeRes(@DimenRes final int res, final boolean shouldInvalidate) {
        setEmojiSize(getResources().getDimensionPixelSize(res), shouldInvalidate);
    }

}
