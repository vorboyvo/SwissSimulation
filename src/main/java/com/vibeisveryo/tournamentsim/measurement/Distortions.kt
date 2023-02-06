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
import kotlin.math.abs

object Distortions {
    /**
     * Gets distortions per team in a single simulated division.
     * Distortions are NOT NORMALIZED; the caller must take care to normalize them!
     * @param division The division to get distortions for
     * @param matchCount The number of matches to normalize over
     * @return a list with entries, indexed corresponding to teams in the division at call time, the distance between
     * their expected and actual match points
     */
    fun getDistortions(division: Division, matchCount: Int): List<Double> {
        val expectedMatchPoints = division.normalizedExpectedMatchPoints(matchCount)
        return division.teamArray().map {
            it.matchPoints - expectedMatchPoints[it]!!
        }
    }

    /**
     * Gets taxicab distance between vectors of expected and actual match points, as a measure of total division
     * deviation, normalized in team and match count.
     */
    fun taxicabDistortions(distortions: List<Double>, teamCount: Int, matchCount: Int): Double {
        return distortions.sumOf { abs(it) / (teamCount * matchCount) }
    }

    fun playoffsTaxicabDistortions(distortions: List<Double>, playoffsSize: (Int) -> Int): Double {
        val teamCount = distortions.size
        val playoffsCount = playoffsSize(teamCount)
        return distortions.subList(0, playoffsCount).sumOf { abs(it) }
    }
}