package com.cognitionbox.petra.examples.algorithmictrading.orders;

import com.cognitionbox.petra.lang.primitives.PValue;

import java.io.FileWriter;

public class CsvDecisionWriterImpl implements CsvDecisionWriter{
    private final PValue<FileWriter> fileWriter = new  PValue<FileWriter>();
    @Override
    public PValue<FileWriter> fileWriter() {
        return fileWriter;
    }
}
