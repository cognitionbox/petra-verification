package com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemOutSubscriptionListener implements SubscriptionListener {
    
    @Override
    public void onClearSnapshot(String itemName, int itemPos) {
      System.out.println("Server has cleared the current status of the chat");
    }
  
    @Override
    public void onCommandSecondLevelItemLostUpdates(int lostUpdates, String key) {
      //not on this subscription
    }
  
    @Override
    public void onCommandSecondLevelSubscriptionError(int code, String message, String key) {
      //not on this subscription
    }
  
    @Override
    public void onEndOfSnapshot(String arg0, int arg1) {
      System.out.println("Snapshot is now fully received, from now on only real-time messages will be received");
    }
  
    @Override
    public void onItemLostUpdates(String itemName, int itemPos, int lostUpdates) {
      System.out.println(lostUpdates + " messages were lost");
    }
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("E hh:mm:ss");
  
    @Override
    public void onItemUpdate(ItemUpdate update) {
      long timestamp = Long.parseLong(update.getValue("raw_timestamp"));
      Date time = new Date(timestamp);
      System.out.println("MESSAGE @ " + dateFormatter.format(time) + " |" + update.getValue("IP") + ": " + update.getValue("message"));
    }
  
    @Override
    public void onListenEnd(Subscription subscription) {
      System.out.println("Stop listeneing to subscription events");
    }
  
    @Override
    public void onListenStart(Subscription subscription) {
      System.out.println("Start listeneing to subscription events");
    }
  
    @Override
    public void onSubscription() {
      System.out.println("Now subscribed to the chat item, messages will now start coming in");
    }
  
    @Override
    public void onSubscriptionError(int code, String message) {
      System.out.println("Cannot subscribe because of error " + code + ": " + message); 
    }
  
    @Override
    public void onUnsubscription() {
      System.out.println("Now unsubscribed from chat item, no more messages will be received");
    }

    @Override
    public void onRealMaxFrequency(String frequency) {
        System.out.println("Frequency is " + frequency);
    }
    
  }