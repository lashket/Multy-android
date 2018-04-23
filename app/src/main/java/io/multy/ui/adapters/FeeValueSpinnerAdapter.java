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
import java.util.zip.Inflater;

import butterknife.BindView;
import io.multy.R;
import io.multy.model.entities.Fee;
import io.multy.util.CryptoFormatUtils;

public class FeeValueSpinnerAdapter extends ArrayAdapter<Fee> {

    private List<Fee> objects;
    private LayoutInflater inflater;

    public FeeValueSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<Fee> objects) {
        super(context, resource, objects);
        this.objects = objects;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private View getBaseRowView(int position, View rowView) {
        ImageView imageLogo = rowView.findViewById(R.id.image_logo);

        TextView textName = rowView.findViewById(R.id.textName);

        TextView textBalanceOriginal = rowView.findViewById(R.id.text_balance_original);

        TextView textBlocks = rowView.findViewById(R.id.text_blocks);
        
        Fee rate = objects.get(position);
        long price = rate.getAmount() == 0 ? 1000 : rate.getAmount();

        if (position == objects.size() - 1) {
            textName.setText(rate.getName());
//            divider.setVisibility(View.GONE);
            imageLogo.setImageResource(R.drawable.ic_custom);
            textBalanceOriginal.setText(price == -1 ? "" : String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(price)));
        } else {
            imageLogo.setImageResource(getIconResId(position));
            textBlocks.setText(String.format("%d blocks", rate.getBlockCount()));
            textName.setText(String.format("%s · %s", rate.getName(), rate.getTime()));
            textBalanceOriginal.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(price)));
//            root.setOnClickListener(v -> {
//                setItemSelected(position);
//                listener.logTransactionFee(position);
//            });
        }
        

        return rowView;
    }

    private int getIconResId(int position) {
        switch (position) {
            case 0:
                return R.drawable.ic_very_fast;
            case 1:
                return R.drawable.ic_fast;
            case 2:
                return R.drawable.ic_medium;
            case 3:
                return R.drawable.ic_slow;
            default:
                return R.drawable.ic_very_slow;
        }
    }


    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getBaseRowView(position, inflater.inflate(R.layout.item_spinner_fee, parent, false));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getBaseRowView(position, inflater.inflate(R.layout.item_spinner_fee, parent, false));
    }
}
