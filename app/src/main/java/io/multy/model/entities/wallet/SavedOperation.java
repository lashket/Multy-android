/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;


import io.multy.model.entities.Fee;
import io.multy.storage.RealmManager;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SavedOperation extends RealmObject {

    public static final String ID_FIELD_NAME = "id";

    @PrimaryKey
    private long id;
    private Wallet wallet;
    private Fee fee;
    private String receiverAddress;
    private String thoseAddress;
    private String name;
    private double amount;
    private long walletId;
    private boolean isAmountScanned;
    private String time;
    private String feeName;
    private long feeAmount;
    private int blockCount;
    private int feeIndex;


    public SavedOperation() {

    }

    public SavedOperation(Fee fee, String receiverAddress, String thoseAddress, String name, double amount, long walletId) {
        this.receiverAddress = receiverAddress;
        this.thoseAddress = thoseAddress;
        this.name = name;
        this.amount = amount;
        this.walletId = walletId;
        this.feeAmount = fee.getAmount();
        this.feeName = fee.getName();
        this.blockCount = fee.getBlockCount();
        this.time =fee.getTime();
        this.feeIndex = fee.getIndex();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Fee getFee() {
        return new Fee(feeName, feeAmount, blockCount, time, feeIndex);
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getThoseAddress() {
        return thoseAddress;
    }

    public void setThoseAddress(String thoseAddress) {
        this.thoseAddress = thoseAddress;
    }

    public String getWalletName() {
        return RealmManager.getAssetsDao().getWalletById(walletId).getWalletName();
    }

    public long getWalletId() {
        return walletId;
    }

}
