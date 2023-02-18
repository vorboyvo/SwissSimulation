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

object MeasureRandom {
    fun measureCombinedDistortions(
        matchesStart: Int, teamsStart: Int, teamsStop: Int,
        iterations: Int, skillStyle: SkillStyle
    ) {
        measureIterative(iterations, "distortions_random_combined", "matches", "teams", "distortions") {
            for (teamCount in teamsStart .. teamsStop) {
                for (matchCount in matchesStart .. (ceil(teamCount / 2.0) * 2 - 3).toInt()) {
                    val main = Division("Main", teamCount, skillStyle)
                    Swiss.randomRunMatches(main, matchCount)
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

    fun getStandingsOverASeason(iterations: Int, matchCount: Int, teamCount: Int) {
        measureIterative(iterations, "standings_weeks_random", "week", "skillRank", "leagueTableRank", "teams") {
            val main = Division("Main", teamCount, SkillStyle.UNIFORM)
            for (week in 0 until matchCount) {
                Swiss.randomRunMatches(main, 1)
                // Get team skill rank
                val teamSkillRanks = main.teamSkillRanks()
                for (j in 0 .. teamCount) {
                    it.addRecord(week, teamSkillRanks[j], j, teamCount)
                }
            }
        }

    }
}