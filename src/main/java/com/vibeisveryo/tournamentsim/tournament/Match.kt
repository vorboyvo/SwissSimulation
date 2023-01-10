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

class Match {
    val homeResult: MatchResult
    val awayResult: MatchResult

    /**
     * koth is a boolean value, taking on false if the match is stopwatch and true if the match is koth.
     *
     *
     * The difference in skill (home - away) is a proxy for how much more likely one team is to win ANY GIVEN
     * ROUND than the other. It is passed as a parameter to a logistic equation f(x) = 1 / (1 + e^-(jx)).
     *
     *
     * Choosing exogenous j such that it is the lowest j that gives an average size of distortions relative to
     * skill less than 500, so that each match, on average, has less than 5 distortions.
     *
     *
     * We run the match until we have a winner, i.e. at least one team has won 4 rounds on koth or 2 on stopwatch.
     */
    constructor(homeSkill: Double, awaySkill: Double, koth: Boolean, random: Random) {
        val diff = homeSkill - awaySkill
        val homeWinChance = 1 / (1 + Math.exp(-1 * ODDS_SCALING_FACTOR * diff))

        // Run first to 4 on koth or 2 on stopwatch
        val winLimit = if (koth) 4 else 2
        var homeRoundsWon = 0
        var awayRoundsWon = 0
        while (homeRoundsWon < winLimit && awayRoundsWon < winLimit) {
            val roundOutcome = runRound(homeWinChance, random)
            if (roundOutcome) {
                homeRoundsWon += 1
            } else {
                awayRoundsWon += 1
            }
        }
        val winner = homeRoundsWon > awayRoundsWon
        val matchPoints = getMatchPoints(homeRoundsWon, awayRoundsWon)
        val homeMatchPoints = matchPoints[0]
        val awayMatchPoints = matchPoints[1]
        val inqMatchPoints = getInqMatchPoints(homeRoundsWon, awayRoundsWon)
        val homeInqMatchPoints = inqMatchPoints[0]
        val awayInqMatchPoints = inqMatchPoints[1]

        // Treat stopwatch rounds as 2 each - doing this after the above because bug discovered
        if (!koth) {
            homeRoundsWon *= 2
            awayRoundsWon *= 2
        }
        homeResult = MatchResult(winner, homeRoundsWon, awayRoundsWon, homeMatchPoints, homeInqMatchPoints)
        awayResult = MatchResult(!winner, awayRoundsWon, homeRoundsWon, awayMatchPoints, awayInqMatchPoints)
    }

    constructor(homeIsBye: Boolean) {
        val byeResult = MatchResult(false, 0, 4, 0, 0.0)
        val nonByeResult = MatchResult(true, 4, 0, 9, 9.0)
        if (homeIsBye) {
            homeResult = byeResult
            awayResult = nonByeResult
        } else {
            homeResult = nonByeResult
            awayResult = byeResult
        }
    }

    override fun toString(): String {
        return String.format("%d-%d", homeResult.roundsWon, awayResult.roundsWon)
    }

    companion object {
        private const val ODDS_SCALING_FACTOR = 2
        private fun runRound(homeWinChance: Double, random: Random): Boolean {
            return random.nextInt(100 + 1) < homeWinChance * 100
        }

        fun getMatchPoints(homeRoundsWon: Int, awayRoundsWon: Int): IntArray {
            val koth = Math.max(homeRoundsWon, awayRoundsWon) == 4
            var homeMatchPoints = 0
            var awayMatchPoints = 0
            if (!koth) {
                homeMatchPoints = Math.round(homeRoundsWon.toDouble() / (homeRoundsWon + awayRoundsWon) * 9).toInt()
                awayMatchPoints = 9 - homeMatchPoints
            } else {
                if (homeRoundsWon > awayRoundsWon) {
                    homeMatchPoints += 6
                    awayMatchPoints = awayRoundsWon
                    homeMatchPoints += 3 - awayMatchPoints
                } else {
                    awayMatchPoints += 6
                    homeMatchPoints = homeRoundsWon
                    awayMatchPoints += 3 - homeMatchPoints
                }
            }
            return intArrayOf(homeMatchPoints, awayMatchPoints)
        }

        fun getInqMatchPoints(homeRoundsWon: Int, awayRoundsWon: Int): DoubleArray {
            val homeMatchPoints = homeRoundsWon.toDouble() * 9 / (homeRoundsWon + awayRoundsWon)
            val awayMatchPoints = awayRoundsWon.toDouble() * 9 / (homeRoundsWon + awayRoundsWon)
            return doubleArrayOf(homeMatchPoints, awayMatchPoints)
        }
    }
}

data class MatchResult(val won: Boolean, val roundsWon: Int, val roundsLost: Int, val matchPoints: Int, val inqMatchPoints: Double)