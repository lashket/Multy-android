/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.api.socket.CurrenciesRate;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.FeeValueSpinnerAdapter;
import io.multy.ui.adapters.MyFeeAdapter;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.adapters.RecentAddressSpinerAdapter;
import io.multy.ui.adapters.WalletsSpinnerAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.NumberFormatter;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;

public class SavedOperationFragment extends BaseFragment implements MyFeeAdapter.OnCustomFeeClickListener {


    public static SavedOperationFragment newInstance() {
        return new SavedOperationFragment();
    }

    @BindView(R.id.input_balance_original)
    EditText inputOriginal;

    @BindView(R.id.spinner_choose_wallet)
    AppCompatSpinner walletSpinner;

    @BindView(R.id.spinner_choose_fee)
    AppCompatSpinner feeSpinner;

    @BindView(R.id.text_receiver_address)
    TextView textReceiverAddress;

    @BindView(R.id.input_balance_currency)
    EditText inputCurrency;

//    @BindView(R.id.spinner_receiver_address)
    AppCompatSpinner receiverAddressSpinner;

    AssetSendViewModel viewModel;
    private CurrenciesRate currenciesRate;

    private boolean isAmountSwapped = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_operation_details, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        subscribeToErrors();
        currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();

        setInfo();
        setupInputOriginal();
        setupInputCurrency();
        initWalletsSpinner();
        return view;
    }

    private void initWalletsSpinner() {
        WalletsSpinnerAdapter walletsSpinnerAdapter = new WalletsSpinnerAdapter(getContext(), R.layout.view_asset_item, RealmManager.getAssetsDao().getWallets());
        walletSpinner.setAdapter(walletsSpinnerAdapter);
        walletSpinner.setSelection(walletsSpinnerAdapter.getPositionById(viewModel.getWallet().getId()));
    }

    private void setInfo() {
        inputOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
        viewModel.speeds.observe(this, speeds -> setFeeSpinnerAdapter());
        viewModel.requestFeeRates(viewModel.getWallet().getCurrencyId(), viewModel.getWallet().getNetworkId());
        textReceiverAddress.setText(viewModel.thoseAddress.getValue());
    }

    private void setFeeSpinnerAdapter() {
        feeSpinner.setAdapter(new FeeValueSpinnerAdapter(getContext(), R.layout.item_spinner_fee, viewModel.speeds.getValue().asList()));
        feeSpinner.setSelection(viewModel.getFee().getIndex());
    }

    private void initReceiverAddressSpinner() {
        RecentAddressSpinerAdapter recentAddressSpinerAdapter = new RecentAddressSpinerAdapter(getContext(), R.layout.item_recent_address, RealmManager.getAssetsDao().getRecentAddresses());
        receiverAddressSpinner.setAdapter(recentAddressSpinerAdapter);
    }

    @Override
    public void onClickCustomFee(long currentValue) {

    }

    @Override
    public void logTransactionFee(int position) {

    }

    private void setupInputOriginal() {
        if (viewModel.getAmount() != 0) {
            inputOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
        }

        inputOriginal.setOnTouchListener((v, event) -> {
            inputOriginal.setSelection(inputOriginal.getText().length());
            if (!inputOriginal.hasFocus()) {
                inputOriginal.requestFocus();
                return true;
            }
            showKeyboard(getActivity(), v);
            Analytics.getInstance(getActivity()).logSendChooseAmount(AnalyticsConstants.SEND_AMOUNT_CRYPTO, viewModel.getChainId());
            return true;
        });

        inputOriginal.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateOriginalBalance();
                inputOriginal.setSelection(inputOriginal.getText().length());
                if (!TextUtils.isEmpty(inputOriginal.getText().toString())) {
//                    setTotalAmountForInput();
                }
            }
        });

        inputOriginal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isAmountSwapped) { // if currency input is main
                    if (!TextUtils.isEmpty(charSequence)) {
                        if (isParsable(charSequence.toString())) {
                            inputCurrency.setText(NumberFormatter.getFiatInstance().format(viewModel.getCurrenciesRate().getBtcToUsd() * Double.parseDouble(charSequence.toString())));
                        }
                    } else {
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
                    }
                }
                checkMaxLengthAfterPoint(inputOriginal, 9, i, i2);
                checkMaxLengthBeforePoint(inputOriginal, 6, i, i1, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkForPointAndZeros(editable.toString(), inputOriginal);
//                calculateTransactionPrice();
            }
        });
    }

    private void setupInputCurrency() {
        if (viewModel.getAmount() != 0) {
            inputCurrency.setText(NumberFormatter.getFiatInstance().format(viewModel.getAmount() * currenciesRate.getBtcToUsd()));
        }

        inputCurrency.setOnTouchListener((v, event) -> {
            inputCurrency.setSelection(inputCurrency.getText().length());
            if (!inputCurrency.hasFocus()) {
                inputCurrency.requestFocus();
                return true;
            }
            showKeyboard(getActivity(), v);
            Analytics.getInstance(getActivity()).logSendChooseAmount(AnalyticsConstants.SEND_AMOUNT_FIAT, viewModel.getChainId());
            return true;
        });

        inputCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateCurrencyBalance();
                inputCurrency.setSelection(inputCurrency.getText().length());
                if (!TextUtils.isEmpty(inputCurrency.getText().toString())) {
//                    setTotalAmountForInput();
                }
            }
        });

        inputCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isAmountSwapped && currenciesRate != null) {
                    if (!TextUtils.isEmpty(charSequence)) {
                        if (isParsable(charSequence.toString())) {
                            inputOriginal.setText(NumberFormatter.getInstance()
                                    .format(Double.parseDouble(charSequence.toString()) / currenciesRate.getBtcToUsd()));
                        }
                    } else {
//                        setEmptyTotalWithFee();
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
//                        textTotal.getEditableText().clear();
                    }
                }
                checkMaxLengthAfterPoint(inputCurrency, 3, i, i2);
                if (isAmountSwapped) {
                    checkMaxLengthBeforePoint(inputCurrency, 9, i, i1, i2);
                } else {
                    checkMaxLengthBeforePoint(inputCurrency, 10, i, i1, i2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable)
                        && editable.toString().length() == 0
                        && editable.toString().contains(".")) {
                    String result = editable.toString().replaceAll(".", "");
                    inputOriginal.setText(result);
                }
            }
        });
    }

    private void checkForPointAndZeros(String input, EditText inputView) {
        int selection = inputView.getSelectionStart();
        if (!TextUtils.isEmpty(input) && input.length() == 1 && input.contains(".")) {
            String result = input.replaceAll(".", "0.");
            inputView.setText(result);
            inputView.setSelection(result.length());
        } else if (!TextUtils.isEmpty(input) && input.startsWith("00")) {
            inputView.setText(input.substring(1, input.length()));
            inputView.setSelection(selection - 1);
        }
    }

    private void checkMaxLengthBeforePoint(EditText input, int max, int start, int end, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.length() > max) {
            if (amount.contains(".")) {
                if (amount.indexOf(".") > max) {
                    if (start != 0 && end != amount.length() && count == amount.length()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(amount.substring(0, start));
                        stringBuilder.append(amount.substring(start + count, amount.length()));
                        input.setText(stringBuilder.toString());
                        if (start <= input.getText().length()) {
                            input.setSelection(start);
                        } else {
                            input.setSelection(input.getText().length());
                        }
                    } else {
                        input.setText(amount.substring(0, amount.length() - 1));
                        input.setSelection(input.getText().length());
                    }
                }
            } else {
                if (start != 0 && end != amount.length() && count == amount.length()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(amount.substring(0, start));
                    stringBuilder.append(amount.substring(start + count, amount.length()));
                    input.setText(stringBuilder.toString());
                    input.setSelection(start);
                } else {
                    input.setText(amount.substring(0, amount.length() - 1));
                    input.setSelection(input.getText().length());
                }
            }
        }
    }

    private void checkMaxLengthAfterPoint(EditText input, int max, int start, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.contains(".")) {
            if (amount.length() - amount.indexOf(".") > max) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(amount.substring(0, start));
                stringBuilder.append(amount.substring(start + count, amount.length()));
                input.setText(stringBuilder.toString());
                input.setSelection(start);
            }
        }
    }

    private void animateOriginalBalance() {
        inputOriginal.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_main));
        inputCurrency.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_grey));
        isAmountSwapped = false;
    }

    private void animateCurrencyBalance() {
        inputOriginal.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_grey));
        inputCurrency.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_main));
        isAmountSwapped = true;
    }

    private boolean isParsable(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
