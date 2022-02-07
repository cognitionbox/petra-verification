package com.cognitionbox.petra.examples.clothingchoice;

public class SystemImpl implements SystemL1, SystemL2 {

    DayAndWeather dayAndWeather = new DayAndWeatherImpl();

    Clothing clothing = new ClothingImpl();

    @Override
    public DayAndWeather dayAndWeather() {
        return dayAndWeather;
    }

    @Override
    public Clothing clothing() {
        return clothing;
    }
}
