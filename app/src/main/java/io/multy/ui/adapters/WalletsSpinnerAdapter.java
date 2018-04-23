/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;

public class WalletsSpinnerAdapter extends ArrayAdapter<Wallet> {

    private LayoutInflater inflater;
    private List<Wallet> wallets;

    public WalletsSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<Wallet> objects) {
        super(context, resource, objects);
        this.wallets = objects;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getBaseRowView(position, inflater.inflate(R.layout.item_wallet_spinner, parent, false));
    }

    @Override
    public int getCount() {
//        return wallets.size();
        return wallets.size();
    }

    private View getBaseRowView(int position, View rowView) {
        TextView name = rowView.findViewById(R.id.text_name);
        name.setText(wallets.get(position).getWalletName());

        TextView amount = rowView.findViewById(R.id.text_amount);
        amount.setText(wallets.get(position).getBalanceLabel());

        TextView amountFiat = rowView.findViewById(R.id.text_amount_fiat);
        amountFiat.setText(wallets.get(position).getFiatBalanceLabel());

        TextView currency = rowView.findViewById(R.id.text_currency);

        ImageView imageChain = rowView.findViewById(R.id.image_chain);
        imageChain.setImageResource(wallets.get(position).getIconResourceId());

        return rowView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getBaseRowView(position, inflater.inflate(R.layout.item_wallet_spinner, parent, false));
    }

    public int getPositionById(long walletId) {
        for (Wallet wallet : wallets) {
            if (wallet.getId() == walletId) {
                return wallets.indexOf(wallet);
            }
        }
        return 0;
    }

}
