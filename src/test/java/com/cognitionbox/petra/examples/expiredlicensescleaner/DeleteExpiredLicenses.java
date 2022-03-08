package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public class DeleteExpiredLicenses implements PGraph<Licenses> {
    @Override
    public void accept(Licenses x) {
        kases(x,
                kase(licenses->licenses.isEmpty(),
                        licenses->licenses.allLicenseFileExistsAndNotExpiredOrLicenseFileNotExistsAndExpired(), licenses->{
                    seq(licenses, new PopulateLicenses());
                    seqr(licenses.licenses(), new ProcessLicense());
                })
        );
    }
}
