package app.fedilab.android.helper;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;

public class MenuFloating extends com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton {

    public static List<String> tags;

    public MenuFloating(Activity activity, LayoutParams layoutParams, int theme, Drawable backgroundDrawable, int position, View contentView, FrameLayout.LayoutParams contentParams, View attachedTo, String tag) {
        super(activity, layoutParams, theme, backgroundDrawable, position, null, contentParams);
        super.detach();
        setPosition(position, layoutParams);
        // If no custom backgroundDrawable is specified, use the background drawable of the theme.
        if(backgroundDrawable == null) {
            if(theme == THEME_LIGHT)
                backgroundDrawable = activity.getResources().getDrawable(R.drawable.button_action_selector);
            else
                backgroundDrawable = activity.getResources().getDrawable(R.drawable.button_action_dark_selector);
        }
        setBackgroundResource(backgroundDrawable);

        if(tags == null || !tags.contains(tag)) {
            if( tags == null)
                tags = new ArrayList<>();
            if (contentView != null) {
                setContentView(contentView, contentParams);
            }
            setClickable(true);
            attach(attachedTo, layoutParams);
            tags.add(tag);
        }
    }


    /**
     * Attaches it to the content view with specified LayoutParams.
     * @param layoutParams
     */
    public void attach(View view, ViewGroup.LayoutParams layoutParams) {
        if(this.getParent()!=null)
            ((ViewGroup)this.getParent()).removeView(this);
        ((ViewGroup)view).addView(this, layoutParams);
    }

    private void setBackgroundResource(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        }
        else {
            setBackgroundDrawable(drawable);
        }
    }

    /**
     * A builder for {@link MenuFloating} in conventional Java Builder format
     */
    public static class Builder {

        private Activity activity;
        private FloatingActionButton.LayoutParams layoutParams;
        private int theme;
        private Drawable backgroundDrawable;
        private int position;
        private View contentView;
        private LayoutParams contentParams;
        private View targetedView;
        private String tag;

        public Builder(Activity activity) {
            this.activity = activity;

            // Default MenuFloating settings
            int size = activity.getResources().getDimensionPixelSize(R.dimen.action_button_size);
            int margin = activity.getResources().getDimensionPixelSize(R.dimen.action_button_margin);
            FloatingActionButton.LayoutParams layoutParams = new LayoutParams(size, size, Gravity.BOTTOM | Gravity.RIGHT);
            layoutParams.setMargins(margin, margin, margin, margin);
            setLayoutParams(layoutParams);
            setTheme(FloatingActionButton.THEME_LIGHT);
            setPosition(FloatingActionButton.POSITION_BOTTOM_RIGHT);
        }

        public Builder intoView(View view){
            this.targetedView = view;
            return this;
        }

        public Builder setLayoutParams(FloatingActionButton.LayoutParams params) {
            this.layoutParams = params;
            return this;
        }

        public Builder setTheme(int theme) {
            this.theme = theme;
            return this;
        }

        public Builder setBackgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public Builder setBackgroundDrawable(int drawableId) {
            return setBackgroundDrawable(activity.getResources().getDrawable(drawableId));
        }

        public Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder setContentView(View contentView) {
            return setContentView(contentView, null);
        }

        public Builder setContentView(View contentView, LayoutParams contentParams) {
            this.contentView = contentView;
            this.contentParams = contentParams;
            return this;
        }
        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public MenuFloating build() {
            return new MenuFloating(activity,
                    layoutParams,
                    theme,
                    backgroundDrawable,
                    position,
                    contentView,
                    contentParams,
                    targetedView, tag);
        }
    }

}
