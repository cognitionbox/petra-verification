package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.sensorlightingsystem.LightSystem;
import com.cognitionbox.petra.verification.Verification;
import com.cognitionbox.petra.verification.tasks.VerificationTask;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class SensorLightingSystemVerification extends Verification {
    public SensorLightingSystemVerification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(LightSystem.class);
        return Verification.tasks();
    }
}
