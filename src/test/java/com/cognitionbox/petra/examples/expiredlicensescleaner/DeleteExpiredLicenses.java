package com.cognitionbox.petra.examples.expiredlicensescleaner;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

public class DeleteExpiredLicenses implements Consumer<Licenses> {
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
