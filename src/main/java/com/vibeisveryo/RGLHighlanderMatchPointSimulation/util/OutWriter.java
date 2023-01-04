package com.vibeisveryo.RGLHighlanderMatchPointSimulation.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OutWriter {
    FileWriter outputWriter;
    private CSVPrinter csvPrinter;
    private List<Object> unsaved;

    public OutWriter(String title, Object... headerValues) throws IOException {
        // Create output file
        String dateTime = DateTimeFormatter.ofPattern("MMddyyyyHHmmss").format(ZonedDateTime.now());
        File outputFile = new File("results/" + title + "_" + dateTime + ".csv");
        if (outputFile.getParentFile().mkdirs()) {
            System.out.println("Parent directory already exists");
        }
        if (!outputFile.createNewFile()) {
            throw new IOException("Failed to create output file!");
        }

        // Create output writer
        this.outputWriter = new FileWriter(outputFile);
        this.csvPrinter = new CSVPrinter(this.outputWriter, CSVFormat.DEFAULT);
        this.csvPrinter.printRecord(headerValues);
        this.unsaved = new ArrayList<>();
    }

    public void addRecord(Object... values) {
        this.unsaved.add(values);
    }

    public void print() throws IOException {
        this.csvPrinter.printRecords(this.unsaved);
        this.unsaved = new ArrayList<>();
    }

    public void print(Object... values) throws IOException {
        this.csvPrinter.printRecord(values);
    }

    public void close() throws IOException {
        this.outputWriter.close();
        this.outputWriter = null;
        this.csvPrinter = null;
    }
}
