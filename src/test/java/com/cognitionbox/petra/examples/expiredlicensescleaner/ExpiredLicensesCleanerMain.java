package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.Petra;

public class ExpiredLicensesCleanerMain {
    public static void main(String... args){
        Petra.finiteStart(
                new DeleteExpiredLicenses(),
                new SystemData());
    }
}
