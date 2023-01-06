/*
 * Copyright 2022, 2023 vorboyvo
 *
 * This file is part of TournamentSimulation.
 *
 * TournamentSimulation is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * TournamentSimulation is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with TournamentSimulation. If not, see
 * <https://www.gnu.org/licenses/>.
 */
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
