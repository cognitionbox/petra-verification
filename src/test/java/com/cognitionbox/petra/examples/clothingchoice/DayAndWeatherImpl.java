package com.cognitionbox.petra.examples.clothingchoice;

public class DayAndWeatherImpl implements DayAndWeather {
    private final Day day = new DayImpl();
    private final Weather weather = new WeatherImpl();
    @Override
    public Day day() {
        return day;
    }

    @Override
    public Weather weather() {
        return weather;
    }
}
