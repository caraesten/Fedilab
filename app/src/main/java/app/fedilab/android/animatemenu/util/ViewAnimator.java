package app.fedilab.android.animatemenu.util;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.ManageAccountsInListActivity;
import app.fedilab.android.activities.SettingsActivity;
import app.fedilab.android.animatemenu.animation.FlipAnimation;
import app.fedilab.android.animatemenu.interfaces.Resourceble;
import app.fedilab.android.animatemenu.interfaces.ScreenShotable;
import app.fedilab.android.helper.Helper;

import static android.content.Context.MODE_PRIVATE;
import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Konstantin on 12.01.2015.
 */
public class ViewAnimator<T extends Resourceble> {
    private final int ANIMATION_DURATION = 175;
    public static final int CIRCULAR_REVEAL_ANIMATION_DURATION = 500;

    private AppCompatActivity appCompatActivity;
  
    private List<T> list;

    private List<View> viewList = new ArrayList<>();
    private ScreenShotable screenShotable;
    private DrawerLayout drawerLayout;
    private ViewAnimatorListener animatorListener;


    public ViewAnimator(AppCompatActivity activity,
                        List<T> items,
                        ScreenShotable screenShotable,
                        final DrawerLayout drawerLayout,
                        ViewAnimatorListener animatorListener) {
        this.appCompatActivity = activity;

        this.list = items;
        this.screenShotable = screenShotable;
        this.drawerLayout = drawerLayout;
        this.animatorListener = animatorListener;
    }



    public void showMenuContent() {
        setViewsClickable(false);
        viewList.clear();
        double size = list.size();
        for (int i = 0; i < size; i++) {
            @SuppressLint("InflateParams") View viewMenu = appCompatActivity.getLayoutInflater().inflate(R.layout.menu_list_item, null);

            if( i == SettingsActivity.position){
                viewMenu.setBackgroundColor(ContextCompat.getColor(appCompatActivity, R.color.mastodonC2));
            }else {
                SharedPreferences sharedpreferences = appCompatActivity.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

                if (theme == Helper.THEME_DARK) {
                    viewMenu.setBackgroundResource(R.drawable.menu_item_selector);
                } else if (theme == Helper.THEME_BLACK){
                    viewMenu.setBackgroundResource(R.drawable.menu_item_selector_black);
                }else {
                    viewMenu.setBackgroundResource(R.drawable.menu_item_selector_light);
                    ImageView imageView = viewMenu.findViewById(R.id.menu_item_image);
                    if( imageView != null){
                        changeDrawableColor(appCompatActivity, imageView, R.color.black);
                    }
                }

            }

            final int finalI = i;
            viewMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = {0, 0};
                    SettingsActivity.position = finalI;
                    v.getLocationOnScreen(location);
                    switchItem(list.get(finalI), location[1] + v.getHeight() / 2);
                }
            });
            ((ImageView) viewMenu.findViewById(R.id.menu_item_image)).setImageResource(list.get(i).getImageRes());
            viewMenu.setVisibility(View.GONE);
            viewMenu.setEnabled(false);
            viewList.add(viewMenu);
            animatorListener.addViewToContainer(viewMenu);
            final double position = i;
            final double delay = 3 * ANIMATION_DURATION * (position / size);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (position < viewList.size()) {
                        animateView((int) position);
                    }
                    if (position == viewList.size() - 1) {
                        screenShotable.takeScreenShot();
                        setViewsClickable(true);
                    }
                }
            }, (long) delay);
        }

    }

    private void hideMenuContent() {
        setViewsClickable(false);
        double size = list.size();
        for (int i = list.size(); i >= 0; i--) {
            final double position = i;
            final double delay = 3 * ANIMATION_DURATION * (position / size);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (position < viewList.size()) {
                        animateHideView((int) position);
                    }
                }
            }, (long) delay);
        }

    }

    private void setViewsClickable(boolean clickable) {
        animatorListener.disableHomeButton();
        for (View view : viewList) {
            view.setEnabled(clickable);
        }
    }

    private void animateView(int position) {
        final View view = viewList.get(position);
        view.setVisibility(View.VISIBLE);
        FlipAnimation rotation =
                new FlipAnimation(90, 0, 0.0f, view.getHeight() / 2.0f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(rotation);
    }

    private void animateHideView(final int position) {
        final View view = viewList.get(position);
        FlipAnimation rotation =
                new FlipAnimation(0, 90, 0.0f, view.getHeight() / 2.0f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.INVISIBLE);

                animatorListener.enableHomeButton();
                drawerLayout.closeDrawers();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(rotation);
    }

    private void switchItem(Resourceble slideMenuItem, int topPosition) {
        this.screenShotable = animatorListener.onSwitch(slideMenuItem, screenShotable, topPosition);
        hideMenuContent();
    }

    public interface ViewAnimatorListener {

        ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position);

        void disableHomeButton();

        void enableHomeButton();

        void addViewToContainer(View view);

    }
}
