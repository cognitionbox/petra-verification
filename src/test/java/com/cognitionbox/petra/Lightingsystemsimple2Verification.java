package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.lightingsystemsimple2.TurnLightOn;
import com.cognitionbox.petra.verification.Verification;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class Lightingsystemsimple2Verification extends Verification {
    public Lightingsystemsimple2Verification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(TurnLightOn.class);
        return Verification.tasks();
    }
}
