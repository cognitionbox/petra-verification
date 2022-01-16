package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.collection.PCollection;

// This state space needs reviewing
public interface Licenses {
    PCollection<License> licenses();

    // do these cover all the cases with no overlaps, i think so

    default boolean isEmpty(){
        return licenses().isEmpty();
    }

    default boolean allLicenseFileExistsAndExpired(){
        return licenses().forall(license->license.licenseFileExistsAndExpired());
    }

    default boolean allLicenseFileExistsAndNotExpired(){
        return licenses().forall(license->license.licenseFileExistsAndNotExpired());
    }

    default boolean allLicenseFileNotExistsAndExpired(){
        return licenses().forall(license->license.licenseFileNotExistsAndExpired());
    }

    default boolean allLicenseFileExistsAndExpiredOrLicenseFileExistsAndNotExpired(){
        return licenses().forall(license->license.licenseFileExistsAndExpired() ^ license.licenseFileExistsAndNotExpired());
    }

    default boolean allLicenseFileExistsAndExpiredOrLicenseFileNotExistsAndExpired(){
        return licenses().forall(license->license.licenseFileExistsAndExpired() ^ license.licenseFileNotExistsAndExpired());
    }

    default boolean allLicenseFileExistsAndNotExpiredOrLicenseFileNotExistsAndExpired(){
        return licenses().forall(license->license.licenseFileExistsAndNotExpired() ^ license.licenseFileNotExistsAndExpired());
    }

    default boolean allLicenseFileExistsAndExpiredOrLicenseFileExistsAndNotExpiredOrLicenseFileNotExistsAndExpired(){
        return licenses().forall(license->
                license.licenseFileExistsAndExpired() ^
                license.licenseFileExistsAndNotExpired() ^
                license.licenseFileNotExistsAndExpired());
    }

//    // exists l, c(l) + d(l)
//    default boolean someLicenseFileNotExistsAndNotExpiredOrlicenseFileExistsAndExpired(){
//        return licenses().some(l->l.licenseFileNotExistsAndNotExpired() ^ l.licenseFileExistsAndExpired());
//    }
//
}

