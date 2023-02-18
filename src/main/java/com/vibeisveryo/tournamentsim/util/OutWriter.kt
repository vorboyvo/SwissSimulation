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
package com.vibeisveryo.tournamentsim.util

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class OutWriter(title: String, vararg headerValues: String) {
    private var outputWriter: FileWriter
    private var csvPrinter: CSVPrinter
    private var unsaved: MutableList<Any?>
    private var open: Boolean

    init {
        // Create output file
        val dateTime = DateTimeFormatter.ofPattern("MMddyyyyHHmmss").format(ZonedDateTime.now())
        val outputFile = File("results/" + title + "_" + dateTime + ".csv")
        if (!outputFile.parentFile.mkdirs()) {
            println("Parent directory already exists")
        }
        if (!outputFile.createNewFile()) {
            throw IOException("Failed to create output file!")
        }

        // Create output writer
        outputWriter = FileWriter(outputFile)
        csvPrinter = CSVPrinter(outputWriter, CSVFormat.DEFAULT)
        csvPrinter.printRecord(*headerValues)
        unsaved = ArrayList()
        open = true
    }

    fun addRecord(vararg values: Any?) {
        if (!open) throw IllegalArgumentException("Cannot add record to closed OutWriter!")
        unsaved.add(values)
    }

    @Throws(IOException::class)
    fun print() {
        if (!open) throw IllegalArgumentException("Cannot print from closed OutWriter!")
        csvPrinter.printRecords(unsaved)
        unsaved = ArrayList()
    }

    @Throws(IOException::class)
    fun print(vararg values: Any?) {
        if (!open) throw IllegalArgumentException("Cannot print to closed OutWriter!")
        csvPrinter.printRecord(*values)
    }

    @Throws(IOException::class)
    fun close() {
        if (!open) throw IllegalArgumentException("Cannot close already closed OutWriter!")
        outputWriter.close()
        open = false
    }
}