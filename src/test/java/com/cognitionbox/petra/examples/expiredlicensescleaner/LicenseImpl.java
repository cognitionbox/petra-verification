package com.cognitionbox.petra.examples.expiredlicensescleaner;

public class LicenseImpl implements License {
    private final LicenseFile licenseFile = new LicenseFileImpl();
    private final LicenseRange licenseRange = new LicenseRangeImpl();

    @Override
    public LicenseFile licenseFile() {
        return licenseFile;
    }

    @Override
    public LicenseRange licenseRange() {
        return licenseRange;
    }
}
