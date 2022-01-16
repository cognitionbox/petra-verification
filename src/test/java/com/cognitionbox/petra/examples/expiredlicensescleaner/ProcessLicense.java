package com.cognitionbox.petra.examples.expiredlicensescleaner;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

//@Invariants({"licenseFile().exists()","licenseFile().notExists()"})
public class ProcessLicense implements Consumer<License> {
    @Override
    public void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndNotExpired(),
                        license->license.licenseFileExistsAndNotExpired() ^
                                license.licenseFileNotExistsAndExpired(),
                        license->{
                            seq(license,new MarkLicensesExpiredOrNot());
                            seq(license,new DeleteIfExpired());
                        }));
    }
}
