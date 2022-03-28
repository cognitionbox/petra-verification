package com.cognitionbox.petra;

import com.cognitionbox.petra.examples.expiredlicensescleaner.DeleteExpiredLicenses;
import com.cognitionbox.petra.verification.Verification;
import com.cognitionbox.petra.verification.tasks.VerificationTask;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class LicenseDeletionVerification extends Verification {
    public LicenseDeletionVerification(VerificationTask task) {
        super(task);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection tasks() {
        setRoot(DeleteExpiredLicenses.class);
        return Verification.tasks();
    }
}
