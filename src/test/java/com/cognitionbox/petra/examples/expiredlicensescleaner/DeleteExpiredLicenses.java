package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.annotations.Graph;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

@View public interface DeleteExpiredLicenses extends Licenses {
    @Graph static void accept(Licenses x) {
        kases(x,
                kase(licenses->licenses.isEmpty(),
                        licenses->licenses.allLicenseFileExistsAndNotExpiredOrLicenseFileNotExistsAndExpired(), licenses->{
                    seq(licenses, PopulateLicenses::accept);
                    seqr(licenses.licenses(), ProcessLicense::accept);
                })
        );
    }
}
