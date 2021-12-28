package com.cognitionbox.petra.examples.lightingsystem;

import java.util.ArrayList;
import java.util.List;

public class LightSystem {
    final List<Light> lights = new ArrayList<>();
    {
        lights.add(new Light());
        lights.add(new Light());
    }
}
