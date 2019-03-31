package fr.gouv.etalab.mastodon.helper;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class CustomPhotoView extends PhotoView {
    private PhotoViewAttacher mAttacher;

    public CustomPhotoView(Context context) {
        super(context);
    }

    public CustomPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public CustomPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Log.v("layoutchanged","yes!" );
    }
}
