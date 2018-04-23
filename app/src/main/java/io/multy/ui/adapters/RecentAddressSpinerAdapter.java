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

import java.util.List;

import io.multy.R;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.util.NativeDataHelper;

public class RecentAddressSpinerAdapter extends ArrayAdapter<RecentAddress> {

    private LayoutInflater inflater;
    private List<RecentAddress> objects;

    public RecentAddressSpinerAdapter(@NonNull Context context, int resource, @NonNull List<RecentAddress> objects) {
        super(context, resource, objects);
        this.objects = objects;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getBaseRowView(position, inflater.inflate(R.layout.item_recent_address, parent, false));
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getBaseRowView(position, inflater.inflate(R.layout.item_recent_address, parent, false));
    }


    private View getBaseRowView(int position, View rowView) {
        ImageView imageCurrency = rowView.findViewById(R.id.image_currency);
        imageCurrency.setImageResource(objects.get(position).getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue() ?
                R.drawable.ic_btc_huge : R.drawable.ic_chain_btc_test);

        TextView textAddress = rowView.findViewById(R.id.text_address);
        textAddress.setText(objects.get(position).getAddress());
        return rowView;
    }

}
