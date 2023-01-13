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

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

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

    private fun runMatch(homeTeam: Team, awayTeam: Team, koth: Boolean): Match {
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
        args[0].teamsFaced.add(args[1])
        args[1].teamsFaced.add(args[0])
        // Return this for convenience
        return this
    }

    /**
     * Run RR matches for this div. No parameters, the # of matches is fixed to
     * div size - 1.
     */
    fun rrRunMatches() {
        val weekList: MutableList<List<Array<Team>>> = ArrayList()

        // Generate matchups
        // Circle method for generating matchups
        // https://en.wikipedia.org/wiki/Round-robin_tournament#Circle_method
        val numberOfTeams = teamList.size
        val fixedTeam = teamList[0]
        val rotatingTeams: MutableList<Team> = teamList.subList(1, numberOfTeams)
        for (i in 0 until numberOfTeams - 1) {
            val week: MutableList<Array<Team>> = ArrayList()
            val currentTeamList: MutableList<Team> = ArrayList(rotatingTeams)
            currentTeamList.add(0, fixedTeam)
            for (j in 0 until (numberOfTeams / 2.0).roundToInt()) {
                week.add(
                    arrayOf(
                        currentTeamList[j],
                        currentTeamList[(currentTeamList.size - j - 1)]
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
                runMatch(pairing[0], pairing[1], koth)
                // printed.append(" ").append(match);
                addMatchPlayed(pairing[0], pairing[1])
            }
            // System.out.println(printed);
        }

        // Sort team list
        teamList.sortWith(Collections.reverseOrder())
    }

    /**
     *
     */
    fun randomRunMatches(matchCount: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in 0 until matchCount) {

            // Randomize list order
            val byeTeam = teamList.removeAt(teamList.size - 1)
            val byeRemoved = byeTeam.isBye
            if (!byeRemoved) teamList.add(byeTeam)
            teamList.shuffle(random)
            if (byeRemoved) teamList.add(byeTeam)

            // Schedule matches by Swiss
            if (this.verbosityLevel >= VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }
            val schedule = scheduleWeek()
            matches.add(schedule.toTypedArray())

            if (this.verbosityLevel >= VerbosityLevel.DETAILED) {
                println("Schedule: " + schedule.map {
                    "[${it[0].name}, ${it[1].name}]"
                })
            }

            // Run matches
            for (pairing in schedule) {
                runMatch(pairing[0], pairing[1], weekNo % 2 != 0)
                addMatchPlayed(pairing[0], pairing[1])
            }

            // Sort team list
            teamList.sortWith(Collections.reverseOrder())
            if (this.verbosityLevel >= VerbosityLevel.MINIMAL) {
                println(this)
            }
        }
        return matches
    }

    /**
     * Runs matches for a Swiss season.
     * @param matchCount Number of matches to be played
     */
    fun swissRunMatches(matchCount: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in 1..matchCount) {
            // Schedule matches
            if (this.verbosityLevel >= VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }
            val schedule = scheduleWeek()
            matches.add(schedule.toTypedArray())

            if (this.verbosityLevel >= VerbosityLevel.DETAILED) {
                println("Schedule: " + schedule.map {
                    "[${it[0].name}, ${it[1].name}]"
                })
            }

            // Run matches
            for (pairing in schedule) {
                runMatch(pairing[0], pairing[1], weekNo % 2 != 0)
                addMatchPlayed(pairing[0], pairing[1])
            }

            // Sort team list
            teamList.sortWith(Collections.reverseOrder())

            if (this.verbosityLevel >= VerbosityLevel.MINIMAL) {
                println(this)
            }
        }
        return matches
    }

    fun swissRunTupleMatches(weekCount: Int, matchesPerWeek: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in 1..weekCount) {
            if (this.verbosityLevel >= VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }
            val weekSchedule = ArrayList<Array<Array<Team>>>();
            for (matchNo in 1..matchesPerWeek) {
                // Schedule matches
                val round = scheduleWeek().toTypedArray()
                weekSchedule.add(round)
                matches.add(round)
                if (this.verbosityLevel >= VerbosityLevel.DETAILED) {
                    println("Week $weekNo${'A'-1+matchNo}: " + round.map {
                        "[${it[0].name}, ${it[1].name}]"
                    })
                }
                for (pairing in round) {
                    addMatchPlayed(pairing[0], pairing[1])
                }
            }

            // Run matches
            for (round in weekSchedule) {
                for (pairing in round) {
                    runMatch(pairing[0], pairing[1], weekNo % 2 != 0)
                }
            }

            // Sort team list
            teamList.sortWith(Collections.reverseOrder())
            if (this.verbosityLevel >= VerbosityLevel.MINIMAL) {
                println(this)
            }
        }
        return matches
    }

    /**
     * Run a single week's matches. Wraps a recursive method that uses DFS to find a working set of matches.
     *
     * @return List of pairs of TeamContext for that week's matches, where the pair's index 0 is home and 1 is away.
     */
    fun scheduleWeek(): List<Array<Team>> {
        // Assume team list is sorted. Otherwise, we have other issues going on.
        val scheduleUnpacked = dfsFindSchedule(
            LinkedList(), teamList.toTypedArray(),
            true, 0, null
        ) ?: throw NullPointerException("Could not find a valid set of matches!")
        // Pack schedule into list of pairs
        val schedule: MutableList<Array<Team>> = ArrayList(scheduleUnpacked.size / 2)
        val packer: Iterator<Team> = scheduleUnpacked.iterator()
        while (packer.hasNext()) {
            val pairing = arrayOf(packer.next(), packer.next())
            schedule.add(pairing)
        }
        return schedule
    }

    /**
     * Recursive algorithm to find a (sub)schedule that works Swiss wise using DFS
     * @param schedule The schedule, as tried so far, expressed as a list to be interpreted as even-odd pairings
     * @param remainingTeams Teams left to be scheduled
     * @param home Whether we are looking to schedule a home team (new match) or away team (fill match)
     * @param depth Current depth down the tree
     * @param scheduleFirst Which team to attempt scheduling first
     * @return Full schedule, or null if none was found down this path
     */
    private fun dfsFindSchedule(
        schedule: Deque<Team>, remainingTeams: Array<Team?>, home: Boolean,
        depth: Int, scheduleFirst: Team?
    ): Deque<Team>? {
        var teamScheduleFirst = scheduleFirst
        if (teamScheduleFirst == null) teamScheduleFirst = remainingTeams[0]

        // Base case: Positive (reached [green] leaf) - should only happen when home false (i.e. scheduling away team)
        if (remainingTeams.size == 1) {
            if (home) throw RuntimeException("Odd number of teams!")
            // Append scheduled team to schedule and return
            schedule.add(teamScheduleFirst)
            return schedule
        }
        // Recursive case 1: On away -> Add current Away team to schedule, recur on rest
        return if (!home) {
            // Append scheduled team to schedule
            schedule.add(teamScheduleFirst)
            // Recur on remainingTeams without scheduled team
            val newRemainingTeams = arrayOfNulls<Team>(remainingTeams.size - 1)
            var j = 0
            //  Copy remainingTeams to newRemainingTeams excluding newly scheduled team
            for (temp in remainingTeams) {
                if (temp !== teamScheduleFirst) {
                    newRemainingTeams[j] = temp
                    j++
                }
            }
            dfsFindSchedule(schedule, newRemainingTeams, true, depth + 1, null)
        } else {
            // Append home team to schedule
            val lastScheduled = schedule.peekLast()
            schedule.add(teamScheduleFirst)
            val newRemainingTeams = remainingTeams.slice(1 until remainingTeams.size).toTypedArray()
            val homeTeam = remainingTeams[0]
            for (team in newRemainingTeams) {
                if (team in homeTeam!!.teamsFaced) continue
                // Recur on newRemainingTeams
                val path = dfsFindSchedule(schedule, newRemainingTeams, false, depth + 1, team)
                if (path != null) return path
            }
            var removed: Team
            do {
                removed = schedule.removeLast()
            } while (removed !== lastScheduled)
            // No path found
            null
        }
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

        // Add bye week for odd number of teams, after shuffling so it's at the end
        // (It doesn't really make a huge difference but it's the principle of it)
        if (teamList.size % 2 == 1) {
            teamList.add(Team(true))
        }
    }
}