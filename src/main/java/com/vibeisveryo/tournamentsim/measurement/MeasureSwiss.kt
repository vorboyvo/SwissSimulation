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
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

object MeasureSwiss {
    fun measureCombinedDistortions(matchesStart: Int, teamsStart: Int, teamsStop: Int, iterations: Int, skillStyle: SkillStyle) {
        measureIterative(iterations, "distortions_swiss_combined", "matches", "teams", "distortions") {
            // Loop over number of teams
            for (teamCount in teamsStart..teamsStop) {
                // Loop over number of matches
                for (matchCount in matchesStart..(ceil(teamCount / 2.0) * 2 - 3).toInt()) {
                    val main = Division("Main", teamCount, skillStyle)
                    Swiss.swissRunMatches(main, matchCount)
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

    fun measureCombinedDistortionsTwoMatches(weeksStart: Int, teamsStart: Int, teamsStop: Int, iterations: Int, skillStyle: SkillStyle) {
        measureIterative(iterations, "distortions_swiss_combined_double", "matches", "teams", "distortions") {
            // Loop over number of teams
            for (teamCount in teamsStart .. teamsStop) {
                // Loop over number of weeks, running two matches each week
                for (weekCount in weeksStart .. (ceil(teamCount / 2.0) - 2).toInt()) {
                    val main = Division("Main", teamCount, skillStyle)
                    Swiss.swissRunTupleMatches(main, weekCount,2)
                    val distortions = Distortions.getDistortions(main, weekCount*2)
                    it.addRecord(
                        weekCount*2,
                        teamCount,
                        String.format("%3.5f", Distortions.taxicabDistortions(distortions, teamCount, weekCount*2))
                    )
                }
            }
        }
    }

    fun measureCombinedPartialDistortions(matchesStart: Int, teamsStart: Int, teamsStop: Int, iterations: Int, skillStyle: SkillStyle) {
        measureIterative(iterations, "distortions_swiss_combined_fractional", "matches",
            "teams", "distortions", "distortionstop4", "distortionstop8", "distortionstop12", "distortionstop16") {
            for (teamCount in teamsStart .. teamsStop) {
                for (matchCount in matchesStart..(ceil(teamCount / 2.0) * 2 - 3).toInt()) {
                    val main = Division("Main", teamCount, skillStyle)
                    Swiss.swissRunMatches(main, matchCount)
                    val distortions = Distortions.getDistortions(main, matchCount)
                    it.addRecord(
                        matchCount,
                        teamCount,
                        String.format("%3.5f", Distortions.taxicabDistortions(distortions, teamCount, matchCount)),
                        if (teamCount >= 4)
                            String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(0, 4), 4, matchCount))
                        else "",
                        if (teamCount >= 8)
                            String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(0, 8), 8, matchCount))
                        else "",
                        if (teamCount >= 12)
                            String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(0, 12), 12, matchCount))
                        else "",
                        if (teamCount >= 16)
                            String.format("%3.5f", Distortions.taxicabDistortions(distortions.subList(0, 16), 16, matchCount))
                        else ""
                    )
                }
            }
        }
    }

    fun measureDistortions(iterations: Int, teamsStart: Int, teamsStop: Int, skillStyle: SkillStyle) {
        // Want new file for each team count
        for (teamCount in teamsStart..teamsStop) {
            println("Doing $teamCount matches now")
            val matchesStop = (ceil(teamCount / 2.0) * 2 - 3).toInt()
            val matchRange = 1..matchesStop
            val teamRange = 1..teamCount
            measureIterative(iterations, "distortions_swiss_${teamCount}_teams", "matchCount",
                *teamRange.map { "exp$it" }.toTypedArray(), *teamRange.map { "act$it" }.toTypedArray()) { outWriter ->
                for (matchCount in matchRange) {
                    val main = Division("Main", teamCount, skillStyle)
                    Swiss.swissRunMatches(main, matchCount)
                    val expMatchPointsMap = main.normalizedExpectedMatchPoints(matchCount)
                    val expMatchPoints = main.teamArray().map { expMatchPointsMap[it]!! }
                    val actMatchPoints = main.teamArray().map { it.matchPoints }
                    outWriter.addRecord(
                        matchCount,
                        *expMatchPoints.map { String.format("%3.5f", it) }.toTypedArray(),
                        *actMatchPoints.map { String.format("%d", it) }.toTypedArray()
                    )
                }
            }

        }
    }

    fun getStandingsOverASeason(iterations: Int, matchCount: Int, teamCount: Int, skillStyle: SkillStyle) {
        measureIterative(iterations, "standings_weeks_swiss_${teamCount}teams", "week", "skillRank", "leagueTableRank") {
            val main = Division("Main", teamCount, skillStyle)
            for (week in 1..matchCount) {
                Swiss.swissRunMatches(main, 1)
                // Get team skill rank
                val teamSkillRanks = main.teamSkillRanks()
                for (j in 1..teamCount) {
                    it.addRecord(week, teamSkillRanks[j], j)
                }
            }
        }
    }

    fun getSkillDiffs(iterations: Int, teamsStart: Int, teamsStop: Int, skillStyle: SkillStyle) {
        measureIterative(iterations, "skill_diffs_swiss", "teams", "week", "averageDiff") {
            for (teamCount in teamsStart..teamsStop) {
                val main = Division("Main", teamCount, skillStyle)
                val matchCount = (ceil(teamCount / 2.0) * 2 - 3).roundToInt()
                for (week in 1..matchCount) {
                    val matches = Swiss.swissRunMatches(main, 1)[0]
                    val diffs = matches.sumOf { abs((it[0].skill - it[1].skill).coerceAtMost(6.0)) } / teamCount
                    it.addRecord(teamCount, week, diffs)
                }
            }
        }
    }
}