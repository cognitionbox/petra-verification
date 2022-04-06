package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.clothingchoice.ClothingChoiceL1;
import com.cognitionbox.petra.verification.Verification;
import com.cognitionbox.petra.verification.tasks.VerificationTask;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ClothingChoiceVerification extends Verification {
    public ClothingChoiceVerification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(ClothingChoiceL1.class);
        return Verification.tasks();
    }
}
