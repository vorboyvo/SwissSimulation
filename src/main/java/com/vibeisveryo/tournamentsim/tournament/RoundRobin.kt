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
package com.vibeisveryo.tournamentsim.tournament

import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.simulation.Team
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

object RoundRobin {

    /**
     * Run RR matches for this div. No parameters, the # of matches is fixed to div size - 1.
     */
    @JvmStatic fun rrRunMatches(div: Division) {
        val weekList: MutableList<List<Array<Team>>> = ArrayList()

        val teamList = div.getTeamList()
        // Generate matchups
        // Circle method for generating matchups
        // https://en.wikipedia.org/wiki/Round-robin_tournament#Circle_method
        val numberOfTeams = teamList.size
        val fixedTeam = teamList[0]
        val rotatingTeams: MutableList<Team> = teamList.subList(1, numberOfTeams).toMutableList()
        for (i in 0 until numberOfTeams - 1) {
            val week: MutableList<Array<Team>> = ArrayList()
            val currentTeamList: MutableList<Team> = ArrayList(rotatingTeams)
            currentTeamList.add(0, fixedTeam)
            for (j in 0 until (numberOfTeams / 2.0).roundToInt()) {
                week.add(
                    arrayOf(
                        currentTeamList[j],
                        currentTeamList[currentTeamList.size - j - 1]
                    )
                )
            }
            Collections.rotate(rotatingTeams, -1)
            weekList.add(week)
        }

        // Run matches
        for ((i, week) in weekList.withIndex()) {
            val printed: StringBuilder = StringBuilder();
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                printed.append("Week ").append(i+1).append(": ").append(week.map {
                    val chosen = i % 2
                    "[${it[chosen].name}, ${it[1-chosen].name}]"
                });
            }
            val koth = i % 2 != 0
            for (pairing in week) {
                div.runMatch(pairing[0], pairing[1], koth)
                div.addMatchPlayed(pairing[0], pairing[1])
            }
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) println(printed)
        }

        // Sort team list
        div.sort()
    }
}