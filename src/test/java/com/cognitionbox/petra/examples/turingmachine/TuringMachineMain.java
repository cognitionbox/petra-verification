package com.cognitionbox.petra.examples.turingmachine;

import com.cognitionbox.petra.examples.ikalicensescleaner.DeleteExpiredLicenses;
import com.cognitionbox.petra.examples.ikalicensescleaner.SystemData;
import com.cognitionbox.petra.lang.Petra;

public class TuringMachineMain {
    public static void main(String... args){
        Petra.finiteStart(
                new DeleteExpiredLicenses(),
                new SystemData());
    }
}
