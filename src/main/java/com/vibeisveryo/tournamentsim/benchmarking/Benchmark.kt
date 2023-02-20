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
package com.vibeisveryo.tournamentsim.benchmarking

import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.tournament.Swiss
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime

object Benchmark {
    private val skillStyle = Division.SkillStyle.TRUE_RANDOM

    fun benchSwissMatches(iters: Int, maxTeams: Int) {
        var teamCount = 10
        while (teamCount <= maxTeams) {
            val matchCount = ceil(teamCount / 2.0).toInt() * 2 - 3
            val elapsed = measureTimeMillis {
                for (i in 0 until iters) {
                    val divMain = Division("Main", teamCount, skillStyle)
                    Swiss.swissRunMatches(divMain, matchCount)
                }
            }
            System.out.printf(
                "%d teams and %d matches took %d milliseconds\n", teamCount, matchCount,
                elapsed
            )
            teamCount += 2
        }
    }

    fun benchRandomMatches(iters: Int, maxTeams: Int) {
        var teamCount = 10
        while (teamCount <= maxTeams) {
            val matchCount = ceil(teamCount / 2.0).toInt() * 2 - 3
            val elapsed = measureTimeMillis {
                for (i in 0 until iters) {
                    val divMain = Division("Main", teamCount, skillStyle)
                    Swiss.randomRunMatches(divMain, matchCount)
                }
            }
            System.out.printf(
                "%d teams and %d matches took %d milliseconds\n", teamCount, matchCount,
                elapsed
            )
            teamCount += 2
        }
    }

    fun benchSeason(iters: Int, teamCount: Int, matchCount: Int) {
        val durations = LongArray(iters)
        for (i in 0 until iters) {
            durations[i] = measureTimeMillis {
                val main = Division("Main", teamCount, skillStyle)
                Swiss.swissRunMatches(main, matchCount)
            }
        }
        var min = Long.MAX_VALUE
        var max = 0L
        var mean = 0.0
        var vari = 0.0
        for (i in 0 until iters) {
            mean += durations[i].toDouble()
            if (durations[i] < min) min = durations[i]
            if (durations[i] > max) max = durations[i]
        }
        mean /= iters.toDouble()
        for (i in 0 until iters) {
            vari += (durations[i] - mean).pow(2.0)
        }
        vari /= iters.toDouble()
        val stdev: Double = sqrt(vari)
        durations.sort()
        val median: Double = if (iters % 2 == 0) {
            (durations[iters / 2 - 1] + durations[iters / 2]) / 2.0
        } else {
            durations[iters / 2].toDouble()
        }
        System.out.printf("min: %d max: %d mean: %.3f stdev: %.3f median: %.3f\n", min, max, mean, stdev, median)
    }
}