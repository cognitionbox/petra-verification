package com.cognitionbox.petra.examples.lightingsystem;

public class Light implements LightView {
    private final Switch s = new Switch();
    private final Power p = new Power();

    @Override
    public Switch s() { return s; }
    @Override
    public Power p() { return p; }
}
