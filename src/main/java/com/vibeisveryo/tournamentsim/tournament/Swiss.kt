package com.vibeisveryo.tournamentsim.tournament

import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.simulation.Team
import java.util.*
import kotlin.collections.ArrayList

object Swiss {

    /**
     * Runs matches for a Swiss season.
     * @param matchCount Number of matches to be played
     */
    @JvmStatic fun swissRunMatches(div: Division, matchCount: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in 1..matchCount) {
            // Schedule matches
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }
            val schedule = scheduleWeek(div)
            matches.add(schedule.toTypedArray())

            if (div.verbosityLevel >= Division.VerbosityLevel.DETAILED) {
                println("Schedule: " + schedule.map {
                    "[${it[0].name}, ${it[1].name}]"
                })
            }

            // Run matches
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
     *
     */
    @JvmStatic fun randomRunMatches(div: Division, matchCount: Int): List<Array<Array<Team>>> {
        // Make list of matches
        val matches: MutableList<Array<Array<Team>>> = ArrayList()
        // Play the week's matches and make necessary adjustments for each week
        for (weekNo in  1..matchCount) {

            div.shuffle()

            // Schedule matches by Swiss
            if (div.verbosityLevel >= Division.VerbosityLevel.MINIMAL) {
                println("==================================================")
                println("Scheduling matches for week $weekNo")
                println("==================================================")
            }
            val schedule = scheduleWeek(div)
            matches.add(schedule.toTypedArray())

            if (div.verbosityLevel >= Division.VerbosityLevel.DETAILED) {
                println("Schedule: " + schedule.map {
                    "[${it[0].name}, ${it[1].name}]"
                })
            }

            // Run matches
            for (pairing in schedule) {
                div.runMatch(pairing[0], pairing[1], weekNo % 2 != 0)
                div.addMatchPlayed(pairing[0], pairing[1])
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
     * Run a single week's matches. Wraps a recursive method that uses DFS to find a working set of matches.
     *
     * @return List of pairs of TeamContext for that week's matches, where the pair's index 0 is home and 1 is away.
     */
    @JvmStatic fun scheduleWeek(div: Division): List<Array<Team>> {
        // Assume team list is sorted. Otherwise, we have other issues going on.
        val scheduleUnpacked = dfsFindSchedule(
            LinkedList(), div.teamArray(),
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
    @JvmStatic private fun dfsFindSchedule(
        schedule: Deque<Team>, remainingTeams: Array<Team>, home: Boolean,
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
            dfsFindSchedule(schedule, newRemainingTeams.requireNoNulls(), true, depth + 1, null)
        } else {
            // Append home team to schedule
            val lastScheduled = schedule.peekLast()
            schedule.add(teamScheduleFirst)
            val newRemainingTeams = remainingTeams.slice(1 until remainingTeams.size).toTypedArray()
            val homeTeam = remainingTeams[0]
            for (team in newRemainingTeams) {
                if (homeTeam.hasFaced(team)) continue
                // Recur on newRemainingTeams
                val path = dfsFindSchedule(schedule, newRemainingTeams, false, depth + 1, team)
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