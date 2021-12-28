package com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex;

import com.cognitionbox.petra.lang.primitives.impls.PDouble;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IGindexHelper {
    public static boolean isConnected(){
        return cst!=null && x_security_token!=null;
    }
    public static String[] login() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://demo-api.ig.com/gateway/deal/session");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
            connection.setRequestProperty("X-IG-API-KEY", "d923453b74093ad5f068ec1a67b12e9c22a1e142");
            connection.setRequestProperty("Version", "2");
            OutputStream os = connection.getOutputStream();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(os));
            writer.beginObject();
            writer.name("identifier").value("Petra777");
            writer.name("password").value("Petra777");
            //writer.name("encryptedPassword").value((Boolean)null);
            writer.endObject();
            writer.flush();
            os.flush();
            System.out.println(connection.getResponseCode());
            System.out.println(new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n")));
            if (connection.getResponseCode()==200){
                cst = connection.getHeaderField("CST");
                x_security_token = connection.getHeaderField("X-SECURITY-TOKEN");
                return new String[]{
                        Integer.toString(connection.getResponseCode()),
                        connection.getHeaderField("CST"),
                        connection.getHeaderField("X-SECURITY-TOKEN")};
            } else {
                return new String[]{Integer.toString(connection.getResponseCode())};
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            connection.disconnect();
        }
    }

    public static String[] createPosition(String cst, String x_security_token,
                                          String epic,
                                          String direction,
                                          double entry,
                                          double qty,
                                          double exit,
                                          double stop) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://demo-api.ig.com/gateway/deal/positions/otc");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
            connection.setRequestProperty("X-IG-API-KEY", "d923453b74093ad5f068ec1a67b12e9c22a1e142");
            connection.setRequestProperty("CST", cst);
            connection.setRequestProperty("X-SECURITY-TOKEN", x_security_token);

            OutputStream os = connection.getOutputStream();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(os));
            writer.beginObject();
            writer.name("epic").value(epic);
            writer.name("expiry").value("DFB");
            writer.name("direction").value(direction);
            //writer2.name("orderSize").value("1");
            writer.name("orderType").value("LIMIT");
            writer.name("level").value(entry); // limit
            writer.name("currencyCode").value("GBP");
            writer.name("size").value(qty); // qty
            writer.name("guaranteedStop").value(false);
            writer.name("forceOpen").value(true);
            writer.name("limitLevel").value(exit);
            //writer.name("limitDistance").value(1); // exit
            writer.name("stopLevel").value(stop); // stop
            //writer.name("stopDistance").value(12); // stop
            writer.endObject();
            writer.flush();

//            OutputStream os = connection.getOutputStream();
//            JsonWriter writer = new JsonWriter(new OutputStreamWriter(os));
//            writer.beginObject();
//            writer.name("epic").value("IX.D.SUNFUN.DAILY.IP");
//            writer.name("expiry").value("DFB");
//            writer.name("direction").value("BUY");
//            //writer2.name("orderSize").value("1");
//            writer.name("orderType").value("LIMIT");
//            writer.name("level").value(7223.5); // limit
//            writer.name("currencyCode").value("GBP");
//            writer.name("size").value(1.0); // qty
//            writer.name("guaranteedStop").value(false);
//            writer.name("forceOpen").value(true);
//            writer.name("limitLevel").value(7230.0);
//            //writer.name("limitDistance").value(1); // exit
//            writer.name("stopLevel").value(7100.0); // stop
//            //writer.name("stopDistance").value(12); // stop
//            writer.endObject();
//            writer.flush();

//            StringWriter sw = new StringWriter();
//            JsonWriter writer2 = new JsonWriter(sw);
//            writer2.beginObject();
//            writer2.name("epic").value("IX.D.SUNFUN.DAILY.IP");
//            writer2.name("expiry").value("DFB");
//            writer2.name("direction").value("BUY");
//            //writer2.name("orderSize").value("1");
//            writer2.name("orderType").value("LIMIT");
//            writer2.name("level").value(7223.5); // limit
//            writer2.name("currencyCode").value("GBP");
//            writer2.name("size").value(1.0); // qty
//            writer2.name("guaranteedStop").value(false);
//            writer2.name("forceOpen").value(true);
//            writer2.name("limitLevel").value(7230.0);
//            //writer.name("limitDistance").value(1); // exit
//            writer2.name("stopLevel").value(7100.0); // stop
//            //writer.name("stopDistance").value(12); // stop
//            writer2.endObject();
//            writer2.flush();
//
//            System.out.println(sw.toString());

            os.flush();
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());

            String resp = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String dealRef = resp
                    .replaceAll("\\}","")
                    .replaceAll("\\{","")
                    .split(":")[1]
                    .replaceAll("\"","").trim();

            System.out.println(dealRef);
            if (connection.getResponseCode()==200){
                return new String[]{
                        Integer.toString(connection.getResponseCode()),
                        cst,
                        x_security_token,dealRef};
            } else {
                return new String[]{
                        Integer.toString(connection.getResponseCode())
                };
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            connection.disconnect();
        }
    }

    public static String[] confirmTrade(String cst, String x_security_token, String dealRef) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://demo-api.ig.com/gateway/deal/confirms/"+dealRef);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
            connection.setRequestProperty("X-IG-API-KEY", "d923453b74093ad5f068ec1a67b12e9c22a1e142");
            connection.setRequestProperty("CST", cst);
            connection.setRequestProperty("X-SECURITY-TOKEN", x_security_token);
            connection.setRequestProperty("Version", "1");
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());

            String resp = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            System.out.println(resp);

            if (connection.getResponseCode()==200){
                return new String[]{
                        Integer.toString(connection.getResponseCode()),
                        "ACCEPTED"};
            } else {
                return new String[]{
                        Integer.toString(connection.getResponseCode())
                };
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            connection.disconnect();
        }
    }

    public static String[] createPositionOld(String cst, String x_security_token) {
        if (!isConnected()){
            login();
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://demo-api.ig.com/gateway/deal/positions/otc");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
            connection.setRequestProperty("X-IG-API-KEY", "d923453b74093ad5f068ec1a67b12e9c22a1e142");
            connection.setRequestProperty("CST", cst);
            connection.setRequestProperty("X-SECURITY-TOKEN", x_security_token);

            OutputStream os = connection.getOutputStream();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(os));
            writer.beginObject();
            writer.name("epic").value("IX.D.SUNFUN.DAILY.IP");
            writer.name("expiry").value("DFB");
            writer.name("direction").value("BUY");
            //writer2.name("orderSize").value("1");
            writer.name("orderType").value("LIMIT");
            writer.name("level").value(7223.5); // limit
            writer.name("currencyCode").value("GBP");
            writer.name("size").value(1.0); // qty
            writer.name("guaranteedStop").value(false);
            writer.name("forceOpen").value(true);
            writer.name("limitLevel").value(7230.0);
            //writer.name("limitDistance").value(1); // exit
            writer.name("stopLevel").value(7100.0); // stop
            //writer.name("stopDistance").value(12); // stop
            writer.endObject();
            writer.flush();

            StringWriter sw = new StringWriter();
            JsonWriter writer2 = new JsonWriter(sw);
            writer2.beginObject();
            writer2.name("epic").value("IX.D.SUNFUN.DAILY.IP");
            writer2.name("expiry").value("DFB");
            writer2.name("direction").value("BUY");
            //writer2.name("orderSize").value("1");
            writer2.name("orderType").value("LIMIT");
            writer2.name("level").value(7223.5); // limit
            writer2.name("currencyCode").value("GBP");
            writer2.name("size").value(1.0); // qty
            writer2.name("guaranteedStop").value(false);
            writer2.name("forceOpen").value(true);
            writer2.name("limitLevel").value(7230.0);
            //writer.name("limitDistance").value(1); // exit
            writer2.name("stopLevel").value(7100.0); // stop
            //writer.name("stopDistance").value(12); // stop
            writer2.endObject();
            writer2.flush();

            System.out.println(sw.toString());

            os.flush();
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());

            String resp = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String dealRef = resp
                    .replaceAll("\\}","")
                    .replaceAll("\\{","")
                    .split(":")[1]
                    .replaceAll("\"","").trim();

            System.out.println(dealRef);
            if (connection.getResponseCode()==200){
                return new String[]{
                        Integer.toString(connection.getResponseCode()),
                        cst,
                        x_security_token,dealRef};
            } else {
                return new String[]{
                        Integer.toString(connection.getResponseCode())
                };
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            connection.disconnect();
        }
    }

    public static class BidAsk {
        PDouble bid = new PDouble(0d);
        PDouble ask = new PDouble(0d);

        @Override
        public String toString() {
            return "BidAsk{" +
                    "bid=" + bid +
                    ", ask=" + ask +
                    '}';
        }

        public BidAsk() {}
        public BidAsk(PDouble bid, PDouble ask) {
            this.bid = bid;
            this.ask = ask;
        }
    }
    public static PDouble getBid(String epic){ return quotes.getOrDefault(epic,new BidAsk()).bid; }
    public static PDouble getAsk(String epic){ return quotes.getOrDefault(epic,new BidAsk()).ask; }
    static Map<String,BidAsk> quotes = new ConcurrentHashMap<>();
    static String cst = null;
    public static String getCst() {
        return cst;
    }

    public static String getX_security_token() {
        return x_security_token;
    }
    static String x_security_token = null;
    public static void updateMarketSnapshots(String cst, String x_security_token, List<String> epics) {
        if (!isConnected()){
            login();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(epics.get(0));
        for (int i=1;i<epics.size();i++){
            sb.append(","+epics.get(i));
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://demo-api.ig.com/gateway/deal/markets?epics="+sb.toString()+"&filter=SNAPSHOT_ONLY");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
            connection.setRequestProperty("X-IG-API-KEY", "d923453b74093ad5f068ec1a67b12e9c22a1e142");
            connection.setRequestProperty("CST", cst);
            connection.setRequestProperty("X-SECURITY-TOKEN", x_security_token);
            connection.setRequestProperty("Version", "1");
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());

            String resp = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            System.out.println(resp);
            JsonParser parser = new JsonParser();
            for (JsonElement e : parser.parse(resp).getAsJsonObject().get("marketDetails").getAsJsonArray()){
                String epic = e.getAsJsonObject().get("instrument").getAsJsonObject().get("epic").getAsString();
                String bid = e.getAsJsonObject().get("snapshot").getAsJsonObject().get("bid").getAsString();
                String offer = e.getAsJsonObject().get("snapshot").getAsJsonObject().get("offer").getAsString();
                PDouble b = new PDouble();
                PDouble a = new PDouble();
                b.set(Double.parseDouble(bid));
                a.set(Double.parseDouble(offer));
                quotes.put(epic, new BidAsk(b,a));
                System.out.println(epic+" "+bid+" "+offer);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            connection.disconnect();
        }
    }

    static LSClientWrapper lsClientWrapper = new LSClientWrapper();

    static void start() {
        LightstreamerClient client = new LightstreamerClient("https://demo-apd.marketdatasystems.com", "DEFAULT");
//        client.connectionDetails.setUser("Petra777");
//        client.connectionDetails.setPassword("Petra777");
        ClientListener clientListener = new SystemOutClientListener();
        client.addListener(clientListener);



        Subscription sub = new Subscription("DISTINCT", "CHART:IX.D.SUNFUN.DAILY.IP", new String[]{"BID", "OFR", "LTP", "LTV", "UTM", "DAY_OPEN_MID", "DAY_PERC_CHG_MID", "DAY_HIGH", "DAY_LOW"});
        SubscriptionListener subListener = new SystemOutSubscriptionListener();
        sub.addListener(subListener);
        client.subscribe(sub);

        client.connect();
    }

    public static void main(String[] args) throws Exception {
        String[] res = login();
        String[] res2 = createPositionOld(res[1],res[2]);
        System.out.println(confirmTrade(res2[1],res2[2],res2[3])[0]);
        List<String> epics = new ArrayList<>();
        epics.add("IX.D.SUNFUN.DAILY.IP");
        epics.add("IX.D.SUNDAX.DAILY.IP");
        updateMarketSnapshots(res2[1],res2[2],epics);
        System.out.println(quotes);
//        start();
//        lsClientWrapper.connect();
//        lsClientWrapper.subscribeForChartTicks("IX.D.SUNFUN.DAILY.IP",new HandyTableListenerAdapter());
    }
}
