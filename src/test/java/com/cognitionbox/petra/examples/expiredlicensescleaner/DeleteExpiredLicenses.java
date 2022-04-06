package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public interface DeleteExpiredLicenses extends PGraph<Licenses> {
   static void accept(Licenses x) {
        kases(x,
                kase(licenses->licenses.isEmpty(),
                        licenses->licenses.allLicenseFileExistsAndNotExpiredOrLicenseFileNotExistsAndExpired(), licenses->{
                    seq(licenses, PopulateLicenses::accept);
                    seqr(licenses.licenses(), ProcessLicense::accept);
                })
        );
    }
}
