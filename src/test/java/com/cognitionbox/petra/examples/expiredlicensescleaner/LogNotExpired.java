package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.lang.step.PGraph;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@View
public interface LogNotExpired extends License {
    @Edge static void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndNotExpired(), license->license.licenseFileExistsAndNotExpired(),
                    license->{
                        String[] split = license.licenseFile().getFile().toString().split("\\\\");
                        System.out.println(split[split.length-1]+": not expired hence, no action necessary.");
                    })
        );
    }
}
