package app.fedilab.android.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;

import androidx.annotation.DrawableRes;
import com.github.pengfeizhou.animation.apng.APNGDrawable;
import org.jetbrains.annotations.NotNull;

public class EmojiDrawableSpan extends DynamicDrawableSpan {


    private APNGDrawable mDrawable;
    private int size;
    public EmojiDrawableSpan(Context context, APNGDrawable apngDrawable) {
        mDrawable = apngDrawable;
        size = (int) Helper.convertDpToPixel(20, context);
    }



    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }

    @Override
    public void draw(@NotNull Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, @NotNull Paint paint) {
        Drawable b = mDrawable;
        canvas.save();
        int transY = bottom - b.getBounds().bottom;
        canvas.translate(x, transY);

        mDrawable.setBounds(0, 0, size, size);
        b.draw(canvas);
        canvas.restore();
        mDrawable.start();
    }
}