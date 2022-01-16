package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.primitives.PValue;

import java.time.LocalDate;

public class LicenseRangeImpl implements LicenseRange {

    private final PValue<LocalDate> expiryDate = new PValue();

    @Override
    public LocalDate now() {
        return LocalDate.now();
    }

    @Override
    public LocalDate expiryDate() {
        return expiryDate.get();
    }

    @Override
    public void setExpiryDate(LocalDate localDate) {
        this.expiryDate.set(localDate);
    }
}
