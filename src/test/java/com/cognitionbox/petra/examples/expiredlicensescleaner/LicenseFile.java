package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;

import java.io.File;

@Primative
@View
public interface LicenseFile
{
    void setFile(File f);
    File getFile(); // invariant has path
    default boolean exists(){
        return getFile().exists();
    }
    default boolean notExists(){
        return !exists();
    }
}
