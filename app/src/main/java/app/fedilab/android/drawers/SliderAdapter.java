package app.fedilab.android.drawers;
/* Copyright 2019 Thomas Schneider
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


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.MediaActivity;
import app.fedilab.android.activities.PixelfedComposeActivity;
import app.fedilab.android.asynctasks.UpdateDescriptionAttachmentAsyncTask;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveAttachmentInterface;

import static android.content.Context.MODE_PRIVATE;


public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> implements OnRetrieveAttachmentInterface {

    private ArrayList<Attachment> attachments;
    private WeakReference<Activity> contextWeakReference;
    private boolean canDelete;
    private SliderAdapter sliderAdapter;

    public SliderAdapter(WeakReference<Activity> contextWeakReference, boolean delete, ArrayList<Attachment> attachments) {
        this.attachments = attachments;
        this.contextWeakReference = contextWeakReference;
        this.canDelete = delete;
        this.sliderAdapter = this;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, parent, false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {

        if( attachments.size() > 1) {
            viewHolder.textViewDescription.setText(String.format("%s/%s", (position + 1), attachments.size()));
        }

        Log.v(Helper.TAG,"url: " + attachments.get(position).getPreview_url() );
        Glide.with(viewHolder.imageViewBackground.getContext())
                .load(attachments.get(position).getPreview_url())
                .into(viewHolder.imageViewBackground);
        viewHolder.imageViewBackground.setContentDescription(attachments.get(position).getDescription());
        if( !this.canDelete) {
            viewHolder.imageViewBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(contextWeakReference.get(), MediaActivity.class);
                    Bundle b = new Bundle();

                    intent.putParcelableArrayListExtra("mediaArray", attachments);
                    b.putInt("position", (position + 1));
                    intent.putExtras(b);
                    contextWeakReference.get().startActivity(intent);
                }
            });
        }else{
            viewHolder.imageViewBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddDescription(attachments.get(position));
                }
            });
        }

    }


    private void showAddDescription(final Attachment attachment) {
        SharedPreferences sharedpreferences = contextWeakReference.get().getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }


        AlertDialog.Builder builderInner = new AlertDialog.Builder(contextWeakReference.get(), style);
        builderInner.setTitle(R.string.upload_form_description);

        View popup_media_description = contextWeakReference.get().getLayoutInflater().inflate(R.layout.popup_media_description, new LinearLayout(contextWeakReference.get()), false);
        builderInner.setView(popup_media_description);

        //Text for report
        final EditText input = popup_media_description.findViewById(R.id.media_description);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(420)});
        final ImageView media_picture = popup_media_description.findViewById(R.id.media_picture);
        Glide.with(contextWeakReference.get())
                .asBitmap()
                .load(attachment.getUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        media_picture.setImageBitmap(resource);
                        media_picture.setImageAlpha(60);
                    }
                });

        builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (attachment.getDescription() != null && !attachment.getDescription().equals("null")) {
            input.setText(attachment.getDescription());
            input.setSelection(input.getText().length());
        }
        builderInner.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new UpdateDescriptionAttachmentAsyncTask(contextWeakReference.get(), attachment.getId(), input.getText().toString(), null, SliderAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                attachment.setDescription(input.getText().toString());
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builderInner.create();
        alertDialog.show();
    }



    /**
     * Removes a media
     *
     * @param position int
     */
    private void showRemove(final int position) {

        SharedPreferences sharedpreferences = contextWeakReference.get().getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(contextWeakReference.get(), style);

        dialog.setMessage(R.string.toot_delete_media);
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attachments.remove(attachments.get(position));
                sliderAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return attachments.size();
    }

    @Override
    public void onRetrieveAttachment(Attachment attachment, String fileName, Error error) {

    }

    @Override
    public void onUpdateProgress(int progress) {

    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        ImageView imageViewBackground;
        TextView textViewDescription;

        SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
        }
    }
}