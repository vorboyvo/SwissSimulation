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
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.roundToInt

object Distortions {
    /**
     * Gets distortions per team in a single simulated division.
     *
     * @return the absolute value of the sum of all distortions, divided by the number of teams.
     */
    fun getDistortions(division: Division): List<Int> {
        // Get team skill rank
        val teamSkillRanks = division.teamSkillRanks()
        // Return abs value of distortions
        return IntStream.range(0, division.getTeamList().size)
            .mapToObj { i: Int -> abs(i - teamSkillRanks[i]) }.toList()
    }

    fun sumDistortionsPerTeam(distortions: List<Int?>?): Double {
        return distortions!!.stream().mapToDouble { k: Int? -> abs(k!!) / distortions.size.toDouble() }.sum()
    }

    fun sumFractionalDistortions(distortions: List<Int?>?, fraction: Double): Double {
        val teamsMeasured = (distortions!!.size / fraction).roundToInt()
        return distortions.subList(0, teamsMeasured).stream().mapToDouble { l: Int? ->
            abs(
                l!!
            ) / teamsMeasured.toDouble()
        }.sum()
    }
}