package com.vibeisveryo.tournamentsim.tournament

import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.simulation.Team
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

object RoundRobin {

    /**
     * Run RR matches for this div. No parameters, the # of matches is fixed to
     * div size - 1.
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
            // StringBuilder printed = new StringBuilder();
            // printed.append("Week ").append(i).append(": ").append(week);
            val koth = i % 2 != 0
            for (pairing in week) {
                div.runMatch(pairing[0], pairing[1], koth)
                // printed.append(" ").append(match);
                div.addMatchPlayed(pairing[0], pairing[1])
            }
            // System.out.println(printed);
        }

        // Sort team list
        div.sort()
    }
}