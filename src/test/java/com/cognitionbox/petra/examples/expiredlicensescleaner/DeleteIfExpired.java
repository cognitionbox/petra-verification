package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public interface DeleteIfExpired extends PGraph<License> {
    static void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndExpired(), license->license.licenseFileNotExistsAndExpired(),
                    license->{
                        seq(license,Delete::accept);
                    }),
                kase(license->license.licenseFileExistsAndNotExpired(), license->license.licenseFileExistsAndNotExpired() ,
                    license->{
                        seq(license,LogNotExpired::accept);
                    })
        );
    }
}
