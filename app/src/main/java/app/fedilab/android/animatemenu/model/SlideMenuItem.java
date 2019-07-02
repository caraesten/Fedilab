package app.fedilab.android.animatemenu.model;


import app.fedilab.android.animatemenu.interfaces.Resourceble;
import app.fedilab.android.fragments.ContentSettingsFragment;

/**
 * Created by Konstantin on 23.12.2014.
 */
public class SlideMenuItem implements Resourceble {

    private ContentSettingsFragment.type type;
    private int imageRes;

    public SlideMenuItem(ContentSettingsFragment.type type, int imageRes) {
        this.type = type;
        this.imageRes = imageRes;
    }


    public int getImageRes() {
        return imageRes;
    }

    @Override
    public ContentSettingsFragment.type getType() {
        return  this.type;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }
}
