/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.SavedOperation;
import io.multy.storage.RealmManager;
import io.multy.util.Constants;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class SavedOperationsAdapter extends RecyclerView.Adapter<SavedOperationsAdapter.SavedOperationHolder> implements RealmChangeListener {

    private RealmResults<SavedOperation> savedOperations;
    private OnSavedOperationClickListener onSavedOperationClickListener;

    public interface OnSavedOperationClickListener {
        void onSavedOperationClick(SavedOperation savedOperation);
    }

    @Override
    public void onChange(Object o) {
        notifyDataSetChanged();
    }

    public SavedOperationsAdapter(RealmResults<SavedOperation> savedOperations, OnSavedOperationClickListener onSavedOperationClickListener) {
        this.savedOperations = savedOperations;
        this.onSavedOperationClickListener = onSavedOperationClickListener;
        savedOperations.addChangeListener(this);
    }

    @NonNull
    @Override
    public SavedOperationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SavedOperationHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_operation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SavedOperationHolder holder, int position) {
        holder.transferedValue.setText(String.valueOf(savedOperations.get(position).getAmount()));
        holder.transferedValue.append(Constants.SPACE);
        holder.transferedValue.append(CurrencyCode.BTC.name());
        holder.operationName.setText(savedOperations.get(position).getName());
        holder.walletName.setText(savedOperations.get(position).getWalletName());
        holder.icDelete.setOnClickListener(v -> RealmManager.getAssetsDao().removeSavedOperation(savedOperations.get(position)));
    }

    @Override
    public int getItemCount() {
        return savedOperations.size();
    }

    class SavedOperationHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name)
        TextView operationName;
        @BindView(R.id.text_transfered_value)
        TextView transferedValue;
        @BindView(R.id.text_sended_from)
        TextView walletName;
        @BindView(R.id.ic_delete)
        ImageView icDelete;

        public SavedOperationHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                onSavedOperationClickListener.onSavedOperationClick(savedOperations.get(getAdapterPosition()));
            });
        }

    }

}
