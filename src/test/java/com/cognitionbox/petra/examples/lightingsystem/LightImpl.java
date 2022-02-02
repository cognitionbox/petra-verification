package com.cognitionbox.petra.examples.lightingsystem;

public class LightImpl implements Light {
    private final ButtonImpl s = new ButtonImpl();
    private final PowerImpl p = new PowerImpl();

    @Override
    public ButtonImpl button() { return s; }
    @Override
    public PowerImpl power() { return p; }
}
