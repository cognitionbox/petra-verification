package com.cognitionbox.petra.examples.expiredlicensescleaner;

import static com.cognitionbox.petra.lang.Petra.start;

public class ExpiredLicensesCleanerMain {
    public static void main(String... args){
        start(
                new DeleteExpiredLicenses(),
                new SystemData());
    }
}
