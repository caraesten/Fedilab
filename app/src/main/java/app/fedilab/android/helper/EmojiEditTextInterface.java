package app.fedilab.android.helper;

import android.support.annotation.DimenRes;
import android.support.annotation.Px;

import com.vanniktech.emoji.emoji.Emoji;

public interface EmojiEditTextInterface {
    void backspace();

    void input(Emoji emoji);

    float getEmojiSize();

    /** sets the emoji size in pixels and automatically invalidates the text and renders it with the new size */
    void setEmojiSize(@Px int pixels);

    /** sets the emoji size in pixels and automatically invalidates the text and renders it with the new size when {@code shouldInvalidate} is true */
    void setEmojiSize(@Px int pixels, boolean shouldInvalidate);

    /** sets the emoji size in pixels with the provided resource and automatically invalidates the text and renders it with the new size */
    void setEmojiSizeRes(@DimenRes int res);

    /** sets the emoji size in pixels with the provided resource and invalidates the text and renders it with the new size when {@code shouldInvalidate} is true */
    void setEmojiSizeRes(@DimenRes int res, boolean shouldInvalidate);
}
