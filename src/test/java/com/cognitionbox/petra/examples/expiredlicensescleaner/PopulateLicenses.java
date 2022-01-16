package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.annotations.Edge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

// this level cares about emptyness or (expired ^ not expired) but not deleted
@Edge public class PopulateLicenses implements Consumer<Licenses> {
    @Override
    public void accept(Licenses l) {
        kases(l,
                kase(licenses->licenses.isEmpty(),
                        licenses->licenses.allLicenseFileExistsAndNotExpired(),
                        licenses->{
                            try {
                                licenses.licenses().addAll(
                                    Files.walk(Paths.get(LicensesPathStrings.NON_EMERGENCY))
                                    .map(p->p.toFile())
                                    .filter(f->f.isFile())
                                    .map(f->{
                                        LicenseImpl license = new LicenseImpl();
                                        license.licenseFile().setFile(f);
                                        return license;
                                    }).collect(Collectors.toList())
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
    }
}
