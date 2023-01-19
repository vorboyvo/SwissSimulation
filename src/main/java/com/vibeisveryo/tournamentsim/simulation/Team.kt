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

class Team(val name: String, val skill: Double) : Comparable<Team> {
    var isBye = false
    var wins = 0
    var losses = 0
    var roundsWon = 0
    var roundsLost = 0
    var matchPoints = 0
    private val teamsFaced: HashSet<Team> = HashSet()

    constructor(bye: Boolean) : this("Bye Week", -999999.0) {
        if (!bye) throw UnsupportedOperationException("Cannot use this constructor except for bye!")
        isBye = true
    }

    fun addMatch(matchResult: MatchResult) {
        wins += if (matchResult.won) 1 else 0
        losses += if (matchResult.won) 0 else 1
        roundsWon += matchResult.roundsWon
        roundsLost += matchResult.roundsLost
        matchPoints += matchResult.matchPoints
    }

    override fun toString(): String {
        return (name + "," + String.format("%.3f", skill) + "," + wins + "," + losses
                + "," + roundsWon + "," + roundsLost + "," + matchPoints)
    }

    override fun compareTo(other: Team): Int {
        if (isBye) return -1 else if (other.isBye) return 1
        return if (matchPoints < other.matchPoints) {
            -1
        } else if (matchPoints > other.matchPoints) {
            1
        } else {
            // Tiebreak by Median Buchholz
            var thisMB = 0
            var otherMB = 0
            run {
                var max = 0
                var min = Int.MAX_VALUE // Really Big Number
                for (team in this.teamsFaced) {
                    thisMB += team.matchPoints
                    max = max.coerceAtLeast(team.matchPoints)
                    min = min.coerceAtMost(team.matchPoints)
                }
                thisMB = thisMB - max - min
            }
            run {
                var max = 0
                var min = Int.MAX_VALUE // Really Big Number
                for (team in other.teamsFaced) {
                    otherMB += team.matchPoints
                    max = max.coerceAtLeast(team.matchPoints)
                    min = min.coerceAtMost(team.matchPoints)
                }
                otherMB = otherMB - max - min
            }
            thisMB - otherMB
        }
    }

    val teamsFacedNames: ArrayList<String>
        get() {
            val list = ArrayList<String>()
            for (team in teamsFaced) {
                list.add(team.name)
            }
            return list
        }

    fun addTeamFaced(team: Team) {
        this.teamsFaced.add(team)
    }

    fun hasFaced(team: Team): Boolean {
        return team in this.teamsFaced
    }
}