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

import com.vibeisveryo.tournamentsim.measurement.MeasureIterative.measureIterative
import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.simulation.Division.SkillStyle
import com.vibeisveryo.tournamentsim.tournament.Swiss
import kotlin.math.ceil

object MeasureRandomThenSwiss {
    fun measureCombinedDistortions(
        matchesStart: Int, teamsStart: Int, teamsStop: Int,
        iterations: Int, skillStyle: SkillStyle, propRandom: Double
    ) {
        measureIterative(iterations, "distortions_randswiss_combined", "matches", "teams", "distortions") {
            for (teamCount in teamsStart..teamsStop) {
                for (matchCount in matchesStart..(ceil(teamCount / 2.0) * 2 - 3).toInt()) {
                    val randMatchCount = ceil(matchCount * propRandom).toInt()
                    val swissMatchCount = matchCount - randMatchCount
                    val main = Division("Main", teamCount, skillStyle)
                    Swiss.randomRunMatches(main, randMatchCount)
                    Swiss.swissRunMatches(main, swissMatchCount)
                    val distortions = Distortions.getDistortions(main, matchCount)
                    it.addRecord(
                        matchCount,
                        teamCount,
                        String.format("%3.5f", Distortions.taxicabDistortions(distortions, teamCount, matchCount))
                    )
                }
            }
        }
    }
}