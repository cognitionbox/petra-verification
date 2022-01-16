package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.annotations.Edge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge public class LogNotExpired implements Consumer<License> {
    @Override
    public void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndNotExpired(), license->license.licenseFileExistsAndNotExpired(),
                    license->{
                        String[] split = license.licenseFile().getFile().toString().split("\\\\");
                        System.out.println(split[split.length-1]+": not expired hence, no action necessary.");
                    })
        );
    }
}
