/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


import io.realm.RealmObject;

public class Fee extends RealmObject {

    private String time;
    private String name;
    private long amount;
    private int blockCount;
    private boolean isSelected = false;
    private int index;

    public Fee() {

    }

    public Fee(String name, long amount, int blockCount, String time, int index) {
        this.name = name;
        this.amount = amount;
        this.blockCount = blockCount;
        this.time = time;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }
}
