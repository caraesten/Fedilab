package app.fedilab.android.helper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;

import org.jetbrains.annotations.NotNull;

/**
 * Original work from @heath-borders: https://stackoverflow.com/a/29114976/3197259
 */
public class CustomQuoteSpan implements LeadingMarginSpan, LineBackgroundSpan {
    private final int backgroundColor;
    private final int stripeColor;
    private final float stripeWidth;
    private final float gap;

    public CustomQuoteSpan(int backgroundColor, int stripeColor, float stripeWidth, float gap) {
        this.backgroundColor = backgroundColor;
        this.stripeColor = stripeColor;
        this.stripeWidth = stripeWidth;
        this.gap = gap;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return (int) (stripeWidth + gap);
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int paintColor = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(stripeColor);

        c.drawRect(x, top, x + dir * stripeWidth, bottom, p);

        p.setStyle(style);
        p.setColor(paintColor);
    }

    @Override
    public void drawBackground(@NotNull Canvas c, @NotNull Paint p, int left, int right, int top, int baseline, int bottom, @NotNull CharSequence text, int start, int end, int lnum) {
        int paintColor = p.getColor();
        p.setColor(backgroundColor);
        c.drawRect(left, top, right, bottom, p);
        p.setColor(paintColor);
    }
}
