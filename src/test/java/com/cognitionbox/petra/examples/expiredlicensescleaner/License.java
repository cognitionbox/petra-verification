package com.cognitionbox.petra.examples.expiredlicensescleaner;

public interface License {
    LicenseFile licenseFile();
    LicenseRange licenseRange();

    default boolean licenseFileExistsAndExpired(){return licenseFile().exists() && licenseRange().expired();}
    default boolean licenseFileExistsAndNotExpired(){return licenseFile().exists() && licenseRange().notExpired();}
    default boolean licenseFileNotExistsAndExpired(){return licenseFile().notExists() && licenseRange().expired();}
    //default boolean licenseFileNotExistsAndNotExpired(){return licenseFile().notExists() && licenseRange().notExpired();}
}
