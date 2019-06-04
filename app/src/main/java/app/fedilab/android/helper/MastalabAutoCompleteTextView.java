package app.fedilab.android.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.annotation.CallSuper;
import android.support.annotation.DimenRes;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.emoji.Emoji;

import app.fedilab.android.R;

import static app.fedilab.android.activities.TootActivity.autocomplete;

public class MastalabAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView implements EmojiEditTextInterface {

    private float emojiSize;
    private boolean emoji;

    public MastalabAutoCompleteTextView(Context context) {
        super(context);
    }

    public MastalabAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
        final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        emoji = sharedpreferences.getBoolean(Helper.SET_DISPLAY_EMOJI, true);
        if (attrs == null) {
            emojiSize = defaultEmojiSize;
        } else {
            @SuppressLint("CustomViewStyleable") final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EmojiMultiAutoCompleteTextView);

            try {
                emojiSize = a.getDimension(R.styleable.EmojiMultiAutoCompleteTextView_emojiSize, defaultEmojiSize);
            } finally {
                a.recycle();
            }
        }

        setText(getText());
    }

    public MastalabAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    @CallSuper
    protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter) {
        final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
        final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
        if( emoji && !autocomplete) {
            EmojiManager.getInstance().replaceWithImages(getContext(), getText(), emojiSize, defaultEmojiSize);
        }
    }

    @Override
    public void backspace() {
        final KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        dispatchKeyEvent(event);
    }


    @Override
    public float getEmojiSize() {
        return emojiSize;
    }

    @Override @CallSuper public void input(final Emoji emoji) {
        if (emoji != null && !autocomplete) {
            final int start = getSelectionStart();
            final int end = getSelectionEnd();

            if (start < 0) {
                append(emoji.getUnicode());
            } else {
                getText().replace(Math.min(start, end), Math.max(start, end), emoji.getUnicode(), 0, emoji.getUnicode().length());
            }
        }
    }

    @Override
    public final void setEmojiSize(@Px final int pixels) {
        setEmojiSize(pixels, true);
    }

    @Override
    public final void setEmojiSize(@Px final int pixels, final boolean shouldInvalidate) {
        emojiSize = pixels;

        if (shouldInvalidate && !autocomplete) {
            setText(getText());
        }
    }

    @Override
    public final void setEmojiSizeRes(@DimenRes final int res) {
        setEmojiSizeRes(res, true);
    }

    @Override
    public final void setEmojiSizeRes(@DimenRes final int res, final boolean shouldInvalidate) {
        setEmojiSize(getResources().getDimensionPixelSize(res), shouldInvalidate);
    }
}
