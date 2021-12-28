package com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex;

import com.lightstreamer.ls_client.ConnectionListener;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionListenerAdapter implements ConnectionListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionListenerAdapter.class);

    public ConnectionListenerAdapter() {
    }

    public void onConnectionEstablished() {
        LOG.debug("onConnectionEstablished");
    }

    public void onSessionStarted(boolean b) {
        LOG.debug("onSessionStarted " + b);
    }

    public void onNewBytes(long l) {
        LOG.debug("onNewBytes " + l);
    }

    public void onDataError(PushServerException e) {
        LOG.debug("onDataError ", e);
        LOG.error("will fail fast due to data error in connection.");
        System.exit(-1);
    }

    public void onActivityWarning(boolean b) {
        LOG.debug("onActivityWarning");
    }

    public void onClose() {
        LOG.debug("onClose");
    }

    public void onEnd(int i) {
        LOG.debug("onEnd " + i);
    }

    public void onFailure(PushServerException e) {
        LOG.debug("onFailure", e);
        LOG.error("will fail fast due to broken connection.");
        System.exit(-1);
    }

    public void onFailure(PushConnException e) {
        LOG.debug("onFailure", e);
        LOG.error("will fail fast due to broken connection.");
        System.exit(-1);
    }
}
