package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.step.PEdge;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

 public class Delete implements PEdge<License> {
    @Override
    public void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndExpired(), license->license.licenseFileNotExistsAndExpired(),
                        license->{
                    license.licenseFile().getFile().delete();
                    String[] split = license.licenseFile().getFile().toString().split("\\\\");
                    System.out.println(split[split.length-1]+": expired hence license deleted.");
                })
        );
    }
}
