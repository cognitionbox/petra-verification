package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public class DeleteIfExpired implements PGraph<License> {
    @Override
    public void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndExpired(), license->license.licenseFileNotExistsAndExpired(),
                    license->{
                        seq(license,new Delete());
                    }),
                kase(license->license.licenseFileExistsAndNotExpired(), license->license.licenseFileExistsAndNotExpired() ,
                    license->{
                        seq(license, new LogNotExpired());
                    })
        );
    }
}
