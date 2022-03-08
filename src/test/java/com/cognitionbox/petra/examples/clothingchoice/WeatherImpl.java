package com.cognitionbox.petra.examples.clothingchoice;

import com.cognitionbox.petra.lang.primitives.PValue;

public class WeatherImpl implements Weather {
    PValue<WeatherEnum> weatherEnum = new PValue<>();
    @Override
    public PValue<WeatherEnum> weatherEnum() {
        return weatherEnum;
    }
}
