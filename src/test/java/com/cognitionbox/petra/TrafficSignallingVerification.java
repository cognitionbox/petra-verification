package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.trafficsignalling2.TrafficSignalling;
import com.cognitionbox.petra.verification.Verification;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class TrafficSignallingVerification extends Verification {
    public TrafficSignallingVerification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(TrafficSignalling.class);
        return Verification.tasks();
    }
}
