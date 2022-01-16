package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.lightingsystemsimple.ToggleLightSystem;
import com.cognitionbox.petra.verification.Verification;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@Ignore
@RunWith(Parameterized.class)
public class LightingsystemsimpleVerification extends Verification {
    public LightingsystemsimpleVerification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(ToggleLightSystem.class);
        return Verification.tasks();
    }
}
