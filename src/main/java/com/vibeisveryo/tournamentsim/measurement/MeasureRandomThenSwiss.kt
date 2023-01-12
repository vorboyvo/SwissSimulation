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

import com.vibeisveryo.tournamentsim.tournament.Division
import com.vibeisveryo.tournamentsim.tournament.Division.SkillStyle
import com.vibeisveryo.tournamentsim.util.OutWriter
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10

object MeasureRandomThenSwiss {
    @Throws(IOException::class)
    fun measureCombinedDistortions(
        matchesStart: Int, teamsStart: Int, teamsStop: Int,
        iterations: Int, skillStyle: SkillStyle?, propRandom: Double
    ) {
        val outWriter = OutWriter("distortions_randswiss_combined", "matches", "teams", "distortions")

        // Do the iters
        for (i in 0 until iterations) {
            val startTime = Instant.now()
            for (teamCount in teamsStart until teamsStop) {
                var matchCount = matchesStart
                while (matchCount < ceil(teamCount / 2.0) * 2 - 2) {
                    val randMatchCount = ceil(matchCount * propRandom).toInt()
                    val swissMatchCount = matchCount - randMatchCount
                    val main = Division("Main", teamCount, skillStyle!!)
                    main.randomRunMatches(randMatchCount)
                    main.swissRunMatches(swissMatchCount)
                    val distortions = Distortions.getDistortions(main)
                    outWriter.addRecord(
                        matchCount,
                        teamCount,
                        String.format("%3.5f", Distortions.sumDistortionsPerTeam(distortions))
                    )
                    matchCount++
                }
            }
            outWriter.print()
            val endTime = Instant.now()
            val time = Duration.between(startTime, endTime).toNanos()
            if (i % Math.pow(10.0, floor(log10((iterations - 1).toDouble()))) == 0.0
                || time > TimeUnit.SECONDS.toNanos(1L)
            ) System.out.printf("Iteration %d took %4.5f seconds\n", i, time / 1000000000.0)
        }

        // Output CSV
        outWriter.close()
    }
}