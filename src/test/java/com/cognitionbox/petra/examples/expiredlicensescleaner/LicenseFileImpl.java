package com.cognitionbox.petra.examples.expiredlicensescleaner;

import com.cognitionbox.petra.lang.primitives.PValue;

import java.io.File;

public class LicenseFileImpl implements LicenseFile {

    PValue<File> file = new PValue();

    @Override
    public void setFile(File f) {
        this.file.set(f);
    }

    @Override
    public File getFile() {
        return this.file.get();
    }
}
