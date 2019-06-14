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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import app.fedilab.android.R;
import app.fedilab.android.client.Entities.InstanceReg;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 14/06/2019.
 * Adapter to display instances
 */

public class InstanceRegAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<InstanceReg> instanceRegs;
    private LayoutInflater layoutInflater;

    public InstanceRegAdapter(Context context, List<InstanceReg> instanceRegs) {
        this.context = context;
        this.instanceRegs = instanceRegs;
        this.layoutInflater = LayoutInflater.from(this.context);
    }

    public int getCount() {
        return instanceRegs.size();
    }

    public InstanceReg getItem(int position) {
        return instanceRegs.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_instance_reg, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        InstanceReg instanceReg = instanceRegs.get(position);

        final InstanceRegAdapter.ViewHolder holder = (InstanceRegAdapter.ViewHolder) viewHolder;

        holder.instance_choose.setOnClickListener(v -> {

        });

        holder.instance_count_user.setText(context.getString(R.string.users,Helper.withSuffix(instanceReg.getTotal_users())));
        holder.instance_description.setText(instanceReg.getDescription());
        holder.instance_host.setText(instanceReg.getDomain());
        holder.instance_version.setText(String.format("%s - %s", instanceReg.getCategory(),instanceReg.getVersion()));
        Log.v(Helper.TAG,"p: " + instanceReg.getProxied_thumbnail());
        Glide.with(context)
                .load(instanceReg.getProxied_thumbnail())
                .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(10)))
                .into(holder.instance_pp);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return instanceRegs.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView instance_pp;
        TextView instance_host, instance_version, instance_description, instance_count_user;
        ImageButton instance_choose;

        public ViewHolder(View itemView) {
            super(itemView);
            instance_pp = itemView.findViewById(R.id.instance_pp);
            instance_host = itemView.findViewById(R.id.instance_host);
            instance_version = itemView.findViewById(R.id.instance_version);
            instance_description = itemView.findViewById(R.id.instance_description);
            instance_count_user = itemView.findViewById(R.id.instance_count_user);
            instance_choose = itemView.findViewById(R.id.instance_choose);
        }
    }
}