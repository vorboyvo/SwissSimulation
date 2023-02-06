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

import com.vibeisveryo.tournamentsim.measurement.Distortions
import com.vibeisveryo.tournamentsim.measurement.MeasureIterative
import com.vibeisveryo.tournamentsim.measurement.MeasureSwiss
import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.tournament.Swiss
import kotlin.math.ceil

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        MeasureIterative.measureIterative(
            1000, "distortions_indiv_32teams", "matches",
            "teams", "distortions", "dist1", "dist2", "dist3", "dist4", "dist5", "dist6", "dist7", "dist8"
        ) {
            val teamCount = 32

            for (matchCount in 1..(ceil(teamCount / 2.0) * 2 - 3).toInt()) {
                val main = Division("Main", teamCount, Division.SkillStyle.TRUE_RANDOM)
                Swiss.swissRunMatches(main, matchCount)
                val distortions = Distortions.getDistortions(main, matchCount)
                it.addRecord(
                    matchCount,
                    teamCount,
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions, teamCount, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(0, 1), 1, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(1, 2), 1, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(2, 3), 1, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(3, 4), 1, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(4, 5), 1, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(5, 6), 1, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(6, 7), 1, matchCount)),
                    String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(7, 8), 1, matchCount)),
                )
            }
        }
    }
}