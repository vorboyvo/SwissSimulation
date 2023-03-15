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

object Swiss {

    /**
     * Runs matches for a Swiss season.
     * @param div The division to run Swiss matches on
     * @param matchCount Number of matches to be played
     * @return a list of weeks, as arrays of 2-tuple arrays of teams which represent individual matchups
     */
    @JvmStatic fun swissRunMatches(div: Division, matchCount: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in 1..matchCount) {
            // Print scheduling matches
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }

            // Schedule matches and add them to our match list
            val schedule = scheduleWeek(div)
            matches.add(schedule.toTypedArray())

            // Print schedule
            if (div.verbosityLevel >= Division.VerbosityLevel.DETAILED) {
                println("Schedule: " + schedule.map {
                    "[${it[0].name}, ${it[1].name}]"
                })
            }

            // Play the matches
            for (pairing in schedule) {
                div.runMatch(pairing[0], pairing[1], weekNo % 2 != 0)
                div.addMatchPlayed(pairing[0], pairing[1])
            }

            // Sort team list
            div.sort()
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println(div)
            }
        }
        return matches
    }

    /**
     * Runs matches for a Swiss season, running multiple matches at once without re-sorting. The use case is when, for
     * example in RGL 6s, multiple matches are scheduled at once (i.e. in 6s, two matches a week).
     */
    @JvmStatic fun swissRunTupleMatches(div: Division, weekCount: Int, matchesPerWeek: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in 1..weekCount) {
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }
            val weekSchedule = ArrayList<Array<Array<Team>>>()
            for (matchNo in 1..matchesPerWeek) {
                // Schedule matches
                val round = scheduleWeek(div).toTypedArray()
                weekSchedule.add(round)
                matches.add(round)
                if (div.verbosityLevel >= Division.VerbosityLevel.DETAILED) {
                    println("Week $weekNo${'A'-1+matchNo}: " + round.map {
                        "[${it[0].name}, ${it[1].name}]"
                    })
                }
                for (pairing in round) {
                    div.addMatchPlayed(pairing[0], pairing[1])
                }
                // Do not sort in between match calls!
            }

            // Run matches
            for (round in weekSchedule) {
                for (pairing in round) {
                    div.runMatch(pairing[0], pairing[1], weekNo % 2 != 0)
                }
            }

            // Sort team list
            div.sort()
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println(this)
            }
        }
        return matches
    }

    /**
     * Runs matches for a season, randomly rolling matches with no repeats.
     * @param div The division to run random matches on
     * @param matchCount Number of matches to be played
     * @return a list of weeks, as arrays of 2-tuple arrays of teams which represent individual matchups
     */
    @JvmStatic fun randomRunMatches(div: Division, matchCount: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in 1..matchCount) {

            // Shuffle because team list is sorted by contract, but we want random (with bye week still at bottom)
            div.shuffle()

            // Print scheduling matches
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }

            // Schedule matches and add them to our match list
            val schedule = scheduleWeek(div)
            matches.add(schedule.toTypedArray())

            // Print schedule
            if (div.verbosityLevel >= Division.VerbosityLevel.DETAILED) {
                println("Schedule: " + schedule.map {
                    "[${it[0].name}, ${it[1].name}]"
                })
            }

            // Play the matches
            for (pairing in schedule) {
                div.runMatch(pairing[0], pairing[1], weekNo % 2 != 0)
                div.addMatchPlayed(pairing[0], pairing[1])
            }

            // Sort team list to obey contract
            div.sort()
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println(div)
            }
        }
        return matches
    }

    /**
     * Schedule a single week's matches, without playing . Wraps recursive DFS to find a working set of matches.
     * @param div the Division to run a week's matches on
     * @return List of pairs of Team for that week's matches, where the pair's index 0 is home and 1 is away.
     */
    @JvmStatic fun scheduleWeek(div: Division): List<Array<Team>> {
        // Assume team list is sorted, by contract
        val scheduleUnpacked = dfsFindSchedule(LinkedList(), div.teamArray(),true, 0)
            ?: throw NullPointerException("Could not find a valid set of matches!")
        // Pack schedule into list of pairs (odd-even pairs)
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
    @JvmStatic private fun dfsFindSchedule(
        schedule: Deque<Team>, remainingTeams: Array<Team>, home: Boolean, depth: Int, scheduleFirst: Team = remainingTeams[0])
    : Deque<Team>? {

        // Base case: Positive (reached [green] leaf) - should only happen when home false (i.e. scheduling away team)
        if (remainingTeams.size == 1) {
            if (home) throw RuntimeException("Odd number of teams!")
            // Append scheduled team to schedule and return
            schedule.add(scheduleFirst)
            return schedule
        }
        // Recursive case 1:
        // On away -> Add current Away team to schedule, recur on rest
        // On home -> Add top team to schedule, then iterate through the rest of the teams looking for the first
        //      working green path to a leaf
        return if (!home) {
            // Append scheduled team to schedule
            schedule.add(scheduleFirst)
            // Recur on remainingTeams without scheduled team
            val newRemainingTeams = arrayOfNulls<Team>(remainingTeams.size - 1)
            var j = 0
            //  Copy remainingTeams to newRemainingTeams excluding newly scheduled team
            for (temp in remainingTeams) {
                if (temp !== scheduleFirst) {
                    newRemainingTeams[j] = temp
                    j++
                }
            }
            dfsFindSchedule(schedule, newRemainingTeams.requireNoNulls(), true, depth + 1)
        } else {
            // Append home team to schedule
            val lastScheduled = schedule.peekLast()
            schedule.add(scheduleFirst)
            // Slice remaining teams to pass to recursive function. Remember: the top team should be teamScheduleFirst!
            val newRemainingTeams = remainingTeams.slice(1 until remainingTeams.size).toTypedArray()
            for (team in newRemainingTeams) {
                if (scheduleFirst.hasFaced(team)) continue
                // Recur on newRemainingTeams
                val path = dfsFindSchedule(schedule, newRemainingTeams, false, depth + 1,
                    scheduleFirst = team)
                if (path != null) return path
            }
            var removed: Team
            do {
                if (schedule.isEmpty()) return null
                removed = schedule.removeLast()
            } while (removed !== lastScheduled)
            // No path found
            null
        }
    }
}