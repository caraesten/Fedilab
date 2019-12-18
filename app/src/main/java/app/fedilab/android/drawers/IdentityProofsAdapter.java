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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import app.fedilab.android.R;
import app.fedilab.android.client.Entities.IdentityProof;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 19/12/2019.
 * Adapter for identity proofs
 */
public class IdentityProofsAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<IdentityProof> identityProofs;

    public IdentityProofsAdapter(List<IdentityProof> identityProofs) {
        this.identityProofs = identityProofs;
    }

    public IdentityProof getItem(int position) {
        return identityProofs.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_identity_proofs, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final ViewHolder holder = (ViewHolder) viewHolder;
        final IdentityProof identityProof = getItem(i);
        holder.proof_name.setText(String.format("@%s", identityProof.getProvider_username()));
        holder.proof_name.setOnClickListener(v -> {
            Helper.openBrowser(context, identityProof.getProfile_url());
        });
        holder.proof_name_network.setText(context.getString(R.string.verified_by, identityProof.getProvider(),Helper.shortDateToString(identityProof.getUpdated_at())));
        holder.proof_container.setOnClickListener(v -> {
            Helper.openBrowser(context, identityProof.getProof_url());
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return identityProofs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView proof_name, proof_name_network;
        private ConstraintLayout proof_container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            proof_name = itemView.findViewById(R.id.proof_name);
            proof_name_network = itemView.findViewById(R.id.proof_name_network);
            proof_container = itemView.findViewById(R.id.proof_container);
        }
    }


}