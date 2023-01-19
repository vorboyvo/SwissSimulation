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
package com.vibeisveryo.tournamentsim.simulation

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.floor

class Division {
    enum class VerbosityLevel(val value: Int) {
        NONE(0), MINIMAL(1), DETAILED(2), FULL(3);
    }

    enum class SkillStyle {
        IDENTICAL, UNIFORM, RANDOM_NORMAL, TRUE_RANDOM
    }

    private val name: String
    private val teamList // Should always be sorted by any action modifying the team list!
            : MutableList<Team>
    var verbosityLevel // TODO add functionality
            : VerbosityLevel
    private val random: Random

    constructor(name: String, noOfTeams: Int, skillStyle: SkillStyle) {
        this.name = name
        this.teamList = ArrayList()
        this.verbosityLevel = VerbosityLevel.NONE
        this.random = Random()
        addTeams(noOfTeams, skillStyle)
    }

    constructor(name: String, noOfTeams: Int, skillStyle: SkillStyle, seed: Long) {
        this.name = name
        this.teamList = ArrayList()
        this.verbosityLevel = VerbosityLevel.NONE
        this.random = Random(seed)
        addTeams(noOfTeams, skillStyle)
    }

    constructor(name: String, teamList: List<Team>) {
        this.name = name
        this.teamList = ArrayList()
        this.teamList.addAll(teamList)
        this.verbosityLevel = VerbosityLevel.NONE
        this.random = Random()
    }

    constructor(name: String, teamList: List<Team>, seed: Long) {
        this.name = name
        this.teamList = ArrayList()
        this.teamList.addAll(teamList)
        this.verbosityLevel = VerbosityLevel.NONE
        this.random = Random(seed)
    }

    fun runMatch(homeTeam: Team, awayTeam: Team, koth: Boolean): Match {
        val myMatch: Match = if (!homeTeam.isBye && !awayTeam.isBye) {
            Match( homeTeam.skill, awayTeam.skill, koth, random)
        } else if (homeTeam.isBye) {
            Match(true)
        } else {
            Match(false)
        }
        homeTeam.addMatch(myMatch.homeResult)
        awayTeam.addMatch(myMatch.awayResult)
        return myMatch
    }

    fun addMatchPlayed(vararg args: Team): Division {
        require(args.size == 2) { "Must have two arguments!" }
        args[0].addTeamFaced(args[1])
        args[1].addTeamFaced(args[0])
        // Return this for convenience
        return this
    }

    /**
     * Sorts this Division's teamList by the natural ordering of the Teams within
     */
    fun sort() {
        this.teamList.sortWith(Collections.reverseOrder())
    }

    /**
     * Shuffles this Division's teamList randomly, while preserving the bye team at the end.
     * Assumes that the bye team is the lowest ranked team.
     */
    fun shuffle() {
        // Randomize list order
        val byeTeam = teamList.removeAt(teamList.size - 1)
        val byeRemoved = byeTeam.isBye
        if (!byeRemoved) teamList.add(byeTeam)
        teamList.shuffle(random)
        if (byeRemoved) teamList.add(byeTeam)
    }

    fun teamArray(): Array<Team> {
        return teamList.toTypedArray()
    }

    /**
     * Returns this division as a multi-line String representation
     * @return In CSV format,
     */
    override fun toString(): String {
        val returned = StringBuilder(
            String.format(
                "Division %s with %d teams\n", name,
                teamList.size
            )
        )
        returned.append("Name,Skill,W,L,RW,RL,MP,Teams Faced\n")
        for (team in teamList) {
            returned.append(team.toString())
                .append(',')
                .append(team.teamsFacedNames)
                .append('\n')
        }
        return returned.toString()
    }

    fun getTeamList(): List<Team> {
        return teamList
    }

    fun teamSkillRanks(): List<Int> {
        return this.getTeamList().stream().map { team: Team? ->
            val teams: MutableList<Team> = ArrayList(this.getTeamList())
            teams.sortWith(Collections.reverseOrder { o1: Team, o2: Team ->
                val diff = o1.skill - o2.skill
                (if (diff >= 0) ceil(diff) else floor(diff)).toInt()
            })
            teams.indexOf(team)
        }.toList()
    }

    fun teamExpectedMatchPoints(): Map<Team, Double> {
        val map: MutableMap<Team, Double> = HashMap()
        for (team in this.teamList) {
            var matchPoints = 0.0
            for (otherTeam in this.teamList) {
                if (team == otherTeam) continue
                matchPoints += Match.getExpectedMatchPoints(team.skill, otherTeam.skill)[0]
            }
            map[team] = matchPoints
        }
        return map
    }

    fun teamExpectedMatchPoints(matchCount: Int): Map<Team, Double> {
        return this.teamExpectedMatchPoints().mapValues { (it.value/(this.teamList.size-1))*matchCount }
    }

    private fun addTeams(noOfTeams: Int, skillStyle: SkillStyle) {
        when (skillStyle) {
            SkillStyle.IDENTICAL -> {
                for (i in 0 until noOfTeams) {
                    teamList.add(Team("Team $i", 0.0))
                }
                // Skill style 1: teams equally spaced out
            }
            SkillStyle.UNIFORM -> {
                for (i in 0 until noOfTeams) {
                    val skill = i.toDouble() / (noOfTeams - 1) * 6 - 3
                    teamList.add(Team("Team $i", skill))
                }
                // Skill style 2: Skills generated according to normal distribution
            }
            SkillStyle.RANDOM_NORMAL -> {
                for (i in 0 until noOfTeams) {
                    val skill = random.nextGaussian()
                    teamList.add(Team("Team $i", skill))
                }
            }
            SkillStyle.TRUE_RANDOM -> {
                for (i in 0 until noOfTeams) {
                    val skill = random.nextDouble(-3.0, 3.0)
                    teamList.add(Team("Team $i", skill))
                }
            }
        }
        teamList.shuffle(random)

        // Add bye week for odd number of teams, after shuffling, so it's at the end
        // (It doesn't really make a huge difference, but it's the principle of it)
        if (teamList.size % 2 == 1) {
            teamList.add(Team(true))
        }
    }
}