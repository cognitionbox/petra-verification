package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.collection.PCollection;
import com.cognitionbox.petra.lang.collection.PList;

public class SystemData implements Licenses {
    final PCollection<License> licenses = new PList<>();
    @Override
    public PCollection<License> licenses() {
        return licenses;
    }
}
