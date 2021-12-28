package com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex;

import com.lightstreamer.ls_client.HandyTableListener;
import com.lightstreamer.ls_client.SubscribedTableKey;
import com.lightstreamer.ls_client.UpdateInfo;

public class HandyTableListenerAdapter implements HandyTableListener {
    private SubscribedTableKey subscribedTableKey;

    public HandyTableListenerAdapter() {
    }

    public void onUpdate(int i, String s, UpdateInfo updateInfo) {
    }

    public void onSnapshotEnd(int i, String s) {
    }

    public void onRawUpdatesLost(int i, String s, int i2) {
    }

    public void onUnsubscr(int i, String s) {
    }

    public void onUnsubscrAll() {
    }

    public SubscribedTableKey getSubscribedTableKey() {
        return this.subscribedTableKey;
    }

    public void setSubscribedTableKey(SubscribedTableKey subscribedTableKey) {
        this.subscribedTableKey = subscribedTableKey;
    }
}