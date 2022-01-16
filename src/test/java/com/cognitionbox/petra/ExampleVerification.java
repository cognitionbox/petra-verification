package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.tradingsystem4.RunTradingSystem;
import com.cognitionbox.petra.verification.Verification;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@Ignore
@RunWith(Parameterized.class)
public class ExampleVerification extends Verification {
    public ExampleVerification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(RunTradingSystem.class);
        return Verification.tasks();
    }
}
