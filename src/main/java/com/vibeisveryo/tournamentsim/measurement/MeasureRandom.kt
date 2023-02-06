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
package com.vibeisveryo.tournamentsim.measurement

import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.simulation.Division.SkillStyle
import com.vibeisveryo.tournamentsim.tournament.Swiss
import com.vibeisveryo.tournamentsim.util.OutWriter
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object MeasureRandom {
    @JvmStatic
    @Throws(IOException::class)
    fun measureCombinedDistortions(
        matchesStart: Int, teamsStart: Int, teamsStop: Int,
        iterations: Int, skillStyle: SkillStyle
    ) {
        val outWriter = OutWriter("distortions_random_combined", "matches", "teams", "distortions")

        // Do the iters
        for (i in 0 until iterations) {
            val startTime = Instant.now()
            for (teamCount in teamsStart .. teamsStop) {
                for (matchCount in matchesStart .. (ceil(teamCount / 2.0) * 2 - 3).toInt()) {
                    val main = Division("Main", teamCount, skillStyle)
                    Swiss.randomRunMatches(main, matchCount)
                    val distortions = Distortions.getDistortions(main, matchCount)
                    outWriter.addRecord(
                        matchCount,
                        teamCount,
                        String.format("%3.5f", Distortions.taxicabDistortions(distortions, teamCount, matchCount))
                    )
                }
            }
            outWriter.print()
            val endTime = Instant.now()
            val time = Duration.between(startTime, endTime).toNanos()
            if (i % 10.0.pow(floor(log10((iterations - 1).toDouble()))) == 0.0
                || time > TimeUnit.SECONDS.toNanos(1L)
            ) System.out.printf("Iteration %d took %4.5f seconds\n", i, time / 1000000000.0)
        }

        // Output CSV
        outWriter.close()
    }

    @Throws(IOException::class)
    fun getStandingsOverASeason(iterations: Int, matchCount: Int, teamCount: Int) {
        val outWriter = OutWriter("standings_weeks_random", "week", "skillRank", "leagueTableRank", "teams")
        for (i in 0 until iterations) {
            val startTime = Instant.now()
            val main = Division("Main", teamCount, SkillStyle.UNIFORM)
            for (week in 0 until matchCount) {
                Swiss.randomRunMatches(main, 1)
                // Get team skill rank
                val teamSkillRanks = main.teamSkillRanks()
                for (j in 0 .. teamCount) {
                    outWriter.addRecord(week, teamSkillRanks[j], j, teamCount)
                }
            }
            outWriter.print()
            val endTime = Instant.now()
            val time = Duration.between(startTime, endTime).toNanos()
            if (i % 10.0.pow(floor(log10((iterations - 1).toDouble()))) == 0.0
                || time > TimeUnit.SECONDS.toNanos(1L)
            ) System.out.printf("Iteration %d took %4.5f seconds\n", i, time / 1000000000.0)
        }
    }
}