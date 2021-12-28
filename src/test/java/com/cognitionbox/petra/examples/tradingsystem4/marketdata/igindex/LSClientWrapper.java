package com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex;

import com.lightstreamer.ls_client.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LSClientWrapper {
    LSClient lsClient;
    public HandyTableListenerAdapter subscribeForChartTicks(String epic, HandyTableListenerAdapter adapter) throws Exception {
        String subscriptionKey = "CHART:{epic}:TICK".replace("{epic}", epic);
        ExtendedTableInfo extendedTableInfo = new ExtendedTableInfo(new String[]{subscriptionKey}, "DISTINCT", new String[]{"BID", "OFR", "LTP", "LTV", "UTM", "DAY_OPEN_MID", "DAY_PERC_CHG_MID", "DAY_HIGH", "DAY_LOW"}, true);
        SubscribedTableKey subscribedTableKey = this.lsClient.subscribeTable(extendedTableInfo, adapter, false);
        adapter.setSubscribedTableKey(subscribedTableKey);
        return adapter;
    }
    public ConnectionListener connect() throws Exception {
        this.lsClient = new LSClient();
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.user = "Petra777";
        connectionInfo.password = "Petra777";
        connectionInfo.pushServerUrl = "https://demo-apd.marketdatasystems.com";
        ConnectionListenerAdapter adapter = new ConnectionListenerAdapter();
        this.lsClient.openConnection(connectionInfo, adapter);
        return adapter;
    }

    public void disconnect() {
        if (this.lsClient != null) {
            this.lsClient.closeConnection();
        }
    }

    private final String[] splitUpdateInfo(UpdateInfo uf) {
        return uf.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(" ", "").split(",");
    }

    private Map<String,BigDecimal[]> quotes = new ConcurrentHashMap<>();
    private final void subscribeForChartTicks(final String epic) throws Exception {
        System.out.println("Subscribing to Lightstreamer chart updates for market:"+epic);
        this.subscribeForChartTicks(epic, (HandyTableListenerAdapter) (new HandyTableListenerAdapter() {
            public void onUpdate(int i, String s, UpdateInfo updateInfo) {
                String[] marketL1Array = splitUpdateInfo(updateInfo);
                if (marketL1Array != null && marketL1Array.length != 0) {
                    String bidClose = marketL1Array[0];
                    String askClose = marketL1Array[1];
                    if (!bidClose.equals("null") || !askClose.equals("null")) {
                        quotes.put(epic,new BigDecimal[]{new BigDecimal(bidClose),new BigDecimal(bidClose)});
                        System.out.println(quotes);
                    }
                }
            }
        }));
    }
}
