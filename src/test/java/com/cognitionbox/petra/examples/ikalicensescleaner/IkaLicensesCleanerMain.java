package com.cognitionbox.petra.examples.ikalicensescleaner;

import com.cognitionbox.petra.lang.Petra;

public class IkaLicensesCleanerMain {
    public static void main(String... args){
        Petra.finiteStart(
                new DeleteExpiredLicenses(),
                new SystemData());
    }
}
