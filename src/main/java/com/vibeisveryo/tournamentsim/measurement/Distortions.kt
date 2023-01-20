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
    fun getDistortions(division: Division, matchCount: Int): List<Double> {
        // Return abs value of distortions
        val expectedMatchPoints = division.teamExpectedMatchPoints(matchCount)
        return division.getTeamList().map {
            it.matchPoints - expectedMatchPoints[it]!!
        }
    }

    fun sumDistortionsPerTeamAndMatch(distortions: List<Double>, matchCount: Int): Double {
        return distortions.stream().mapToDouble { k: Double -> abs(k) / (distortions.size.toDouble() * matchCount) }.sum()
    }

    fun sumFractionalDistortions(distortions: List<Double>, fraction: Double): Double {
        val teamsMeasured = (distortions.size / fraction).roundToInt()
        return distortions.subList(0, teamsMeasured).stream().mapToDouble { l: Double ->
            abs(
                l
            ) / teamsMeasured.toDouble()
        }.sum()
    }
}