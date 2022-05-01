package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;

import java.time.LocalDate;

@Primative
@View
public interface LicenseRange {
    LocalDate now();
    LocalDate expiryDate();
    void setExpiryDate(LocalDate localDate);
    default boolean expired(){
        return expiryDate()!=null && now().isAfter(expiryDate());
    }

    default boolean notExpired(){
        return !expired(); // assumes not expired when not read
    }
}
