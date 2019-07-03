package app.fedilab.android.filelister;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;


import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;



/**
 * Created by S.Yogesh on 14-02-2016.
 */
class FilesListerView extends RecyclerView {

    private FileListerAdapter adapter;

    FilesListerView(Context context) {
        super(context);
        init();
    }

    FilesListerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    FilesListerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressLint("WrongConstant")
    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new FileListerAdapter(this);
    }

    void start() {
        setAdapter(adapter);
        adapter.start();
    }

    void setDefaultDir(File file) {
        adapter.setDefaultDir(file);
    }

    void setDefaultDir(String path) {
        setDefaultDir(new File(path));
    }

    File getSelected() {
        return adapter.getSelected();
    }

    void goToDefaultDir() {
        adapter.goToDefault();
    }

    void setFileFilter(FileListerDialog.FILE_FILTER fileFilter) {
        adapter.setFileFilter(fileFilter);
    }

    FileListerDialog.FILE_FILTER getFileFilter() {
        return adapter.getFileFilter();
    }
}
