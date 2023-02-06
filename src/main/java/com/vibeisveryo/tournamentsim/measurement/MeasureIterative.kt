package com.vibeisveryo.tournamentsim.measurement

import com.vibeisveryo.tournamentsim.util.OutWriter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object MeasureIterative {

    /**
     * Function enabling repeated processing/trials for experimental results; order not randomized because trials are
     * independent in all variables except for pseudorandom number generation, in which they are essentially independent
     * anyway.
     * @param iterations number of iterations/trials to run
     * @param title name to give the output file
     * @param headerValues headers for the output file
     * @param gen function that takes in an OutWriter, and adds the desired records
     */
    fun measureIterative(iterations: Int, title: String, vararg headerValues: String, gen: (OutWriter) -> Unit) {
        val outWriter = OutWriter(title, *headerValues)
        for (i in 0 until iterations) {
            val startTime = Instant.now()
            gen(outWriter) // Run passed in function
            outWriter.print() // Print/clear entries
            val endTime = Instant.now()
            // Measure time
            val time = Duration.between(startTime, endTime).toNanos()
            if (i % 10.0.pow(floor(log10((iterations - 1).toDouble()))) == 0.0
                || time > TimeUnit.SECONDS.toNanos(1L)
            ) System.out.printf("Iteration %d took %4.5f seconds\n", i, time / 1000000000.0)
        }

        // Output csv, make sure file is complete and saved
        outWriter.close()
    }
}