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
package com.vibeisveryo.tournamentsim

import com.vibeisveryo.tournamentsim.benchmarking.Benchmark.benchSeason
import com.vibeisveryo.tournamentsim.benchmarking.Benchmark.benchSwissMatches
import com.vibeisveryo.tournamentsim.measurement.MeasureSwiss.measureCombinedDistortions
import com.vibeisveryo.tournamentsim.simulation.Division.SkillStyle
import java.util.*
import kotlin.math.ceil
import kotlin.system.exitProcess

object Main {
    val SKILL_STYLE = SkillStyle.UNIFORM
    private fun helpCommand() {
        val usageString = "Usage: java -jar RGLHighlanderMatchPointSimulation.jar [OPTION]... <COMMAND> [<ARGS>]..."
        val helpStrings = arrayOf(
            "distMatches: Measure distortions over adding matches; usage: distMatches <matchesStart> <matchesStop> "
                    + "<teamCount> <iterations>",
            "distCombined: Measure distortions over matches and teams; usage: distCombined <matchesStart> " +
                    "<teamsStart> <teamsStop> <iterations>",
            "benchmarkSeason: Benchmarks the performance of a season over many iterations; usage: " +
                    "benchmarkSeason <iterations> <teams> <matches>",
            "benchmarkMatches: Benchmarks how long team sizes take in relation to each other; usage: " +
                    "benchmarkMatches <iterations> <maxTeams>"
        )
        println(usageString)
        for (helpString in helpStrings) {
            print("   ")
            println(helpString)
        }
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Handle command line arguments
        if (args.isEmpty()) {
            helpCommand()
            exitProcess(0)
        }
        when (args[0].lowercase(Locale.getDefault())) {
            "help" -> helpCommand()
            "distcombined" -> {
                if (args.size == 5) measureCombinedDistortions(
                    args[1].toInt(),
                    args[2].toInt(),
                    args[3].toInt(),
                    args[4].toInt(),
                    SKILL_STYLE
                ) else helpCommand()
            }
            "benchmarkseason" -> {
                if (args.size == 4) benchSeason(
                    args[1].toInt(),
                    args[2].toInt(),
                    args[3].toInt()
                ) else if (args.size == 3) benchSeason(
                    args[1].toInt(), args[2].toInt(), (ceil(args[2].toInt() / 2.0) * 2 - 2).toInt()
                ) else benchSeason(500, 30, 27)
            }
            "benchmarkmatches" -> {
                if (args.size == 3) benchSwissMatches(
                    args[1].toInt(),
                    args[2].toInt()
                ) else if (args.size == 2) benchSwissMatches(
                    args[1].toInt(), 999
                ) else benchSwissMatches(100, 34)
            }
        }
    }
}