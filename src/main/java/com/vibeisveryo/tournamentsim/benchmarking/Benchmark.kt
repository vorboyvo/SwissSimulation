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
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

object Benchmark {
    val skillStyle = Division.SkillStyle.TRUE_RANDOM

    @JvmStatic
    fun benchSwissMatches(iters: Int, maxTeams: Int) {
        var teamCount = 10
        while (teamCount <= maxTeams) {
            val start = Instant.now()
            val matchCount = ceil(teamCount / 2.0).toInt() * 2 - 3
            for (i in 0 until iters) {
                val divMain = Division("Main", teamCount, skillStyle)
                Swiss.swissRunMatches(divMain, matchCount)
            }
            val end = Instant.now()
            System.out.printf(
                "%d teams and %d matches took %d milliseconds\n", teamCount, matchCount,
                Duration.between(start, end).toMillis()
            )
            teamCount += 2
        }
    }

    fun benchRandomMatches(iters: Int, maxTeams: Int) {
        var teamCount = 10
        while (teamCount <= maxTeams) {
            val start = Instant.now()
            val matchCount = ceil(teamCount / 2.0).toInt() * 2 - 3
            for (i in 0 until iters) {
                val divMain = Division("Main", teamCount, skillStyle)
                Swiss.randomRunMatches(divMain, matchCount)
            }
            val end = Instant.now()
            System.out.printf(
                "%d teams and %d matches took %d milliseconds\n", teamCount, matchCount,
                Duration.between(start, end).toMillis()
            )
            teamCount += 2
        }
    }

    @JvmStatic
    fun benchSeason(iters: Int, teamCount: Int, matchCount: Int) {
        val durations = IntArray(iters)
        for (i in 0 until iters) {
            val start = Instant.now()
            val main = Division("Main", teamCount, skillStyle)
            Swiss.swissRunMatches(main, matchCount)
            val stop = Instant.now()
            durations[i] = Duration.between(start, stop).toMillis().toInt()
        }
        var min = Int.MAX_VALUE
        var max = 0
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
        Arrays.sort(durations)
        val median: Double = if (iters % 2 == 0) {
            (durations[iters / 2 - 1] + durations[iters / 2]) / 2.0
        } else {
            durations[iters / 2].toDouble()
        }
        System.out.printf("min: %d max: %d mean: %.3f stdev: %.3f median: %.3f\n", min, max, mean, stdev, median)
    }
}