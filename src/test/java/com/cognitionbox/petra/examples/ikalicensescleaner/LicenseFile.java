package com.cognitionbox.petra.examples.ikalicensescleaner;

import com.cognitionbox.petra.annotations.Primative;

import java.io.File;

@Primative
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
