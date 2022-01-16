package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.annotations.Edge;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge public class MarkLicensesExpiredOrNot implements Consumer<License> {
    @Override
    public void accept(License l) {
        kases(l,
                kase(license->license.licenseFileExistsAndNotExpired(),
                        license->license.licenseFileExistsAndNotExpired() ^
                                license.licenseFileExistsAndExpired(),
                        license->{
                            // just do the xml passing here
                            try {
                                List<String> licenseFileLines = Files.readAllLines(license.licenseFile().getFile().toPath());
                                String endDateNode = licenseFileLines.get(7);
                                if (endDateNode.contains("<end-date>") && endDateNode.contains("</end-date>")){
                                    String endDateText = endDateNode
                                            .replaceAll("<end-date>","")
                                            .replaceAll("</end-date>","");
                                    if (!endDateText.equals("")){
                                        LocalDate endDate = LocalDate.parse(endDateText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                        license.licenseRange().setExpiryDate(endDate);
                                    } else {
                                        // to trigger deletion of blank license dates
                                        license.licenseRange().setExpiryDate(LocalDate.of(1986,7,1));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }));
    }
}
