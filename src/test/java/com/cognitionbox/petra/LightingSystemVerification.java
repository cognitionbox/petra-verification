package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.lightingsystem.ToggleLight;
import com.cognitionbox.petra.verification.Verification;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class LightingSystemVerification extends Verification {
    public LightingSystemVerification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(ToggleLight.class);
        return Verification.tasks();
    }
}
