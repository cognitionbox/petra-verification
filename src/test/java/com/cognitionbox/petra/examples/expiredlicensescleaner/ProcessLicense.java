package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

//@Invariants({"licenseFile().exists()","licenseFile().notExists()"})
public interface ProcessLicense extends PGraph<License> {
    static void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndNotExpired(),
                        license->license.licenseFileExistsAndNotExpired() ^
                                license.licenseFileNotExistsAndExpired(),
                        license->{
                            seq(license,MarkLicensesExpiredOrNot::accept);
                            seq(license,DeleteIfExpired::accept);
                        }));
    }
}
