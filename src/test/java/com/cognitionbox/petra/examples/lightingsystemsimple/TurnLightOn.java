package com.cognitionbox.petra.examples.lightingsystemsimple;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

//                    ((Consumer<String>) (x -> {
//                    })).accept("");
@Edge
public class TurnLightOn implements Consumer<Light> {
    @Override
    public void accept(Light light) {
        kases(light,
                kase(l->l.off(), l->l.on(),l->{
                    join(
                            par(l.s(),new SwitchOn()),
                            par(l.p(),new PowerOn()));
                            System.out.println("light on");
                })
            );
    }
}
