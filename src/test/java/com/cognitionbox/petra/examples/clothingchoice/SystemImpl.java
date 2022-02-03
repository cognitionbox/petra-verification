package com.cognitionbox.petra.examples.clothingchoice;

public class SystemImpl implements System {

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
