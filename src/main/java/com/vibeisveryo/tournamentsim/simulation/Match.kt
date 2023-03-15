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

import com.vibeisveryo.tournamentsim.util.ArrayMatch
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.random.Random

object Match {
    private const val ODDS_SCALING_FACTOR = 2

    /**
     * koth is a boolean value, taking on false if the match is stopwatch and true if the match is koth.
     *
     * The difference in skill (home - away) is a proxy for how much more likely one team is to win ANY GIVEN
     * ROUND than the other. It is passed as a parameter to a logistic equation f(x) = 1 / (1 + e^-(jx)).
     *
     * Choosing exogenous j such that it is the lowest j that gives an average size of distortions relative to
     * skill less than 500, so that each match, on average, has less than 5 distortions.
     *
     * We run the match until we have a winner, i.e. at least one team has won 4 rounds on koth or 2 on stopwatch.
     */
    fun match(homeSkill: Double, awaySkill: Double, koth: Boolean, random: Random, homeBye: Boolean = false,
              awayBye: Boolean = false): Pair<MatchResult, MatchResult> {
        val homeResult: MatchResult
        val awayResult: MatchResult

        // Handle bye weeks
        if (homeBye && awayBye) {
            throw IllegalArgumentException("Home and away cannot both be bye!")
        } else if (homeBye) {
            return Pair(
                MatchResult(false, 0, 4, 0),
                MatchResult(true, 4, 0, 9)
            )
        } else if (awayBye) {
            return Pair(
                MatchResult(true, 4, 0, 9),
                MatchResult(false, 0, 4, 0)
            )
        }

        val diff = homeSkill - awaySkill
        val homeWinChance = 1 / (1 + exp(-1 * ODDS_SCALING_FACTOR * diff))

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

        // Treat stopwatch rounds as 2 each - doing this after the above because bug discovered
        if (!koth) {
            homeRoundsWon *= 2
            awayRoundsWon *= 2
        }
        homeResult = MatchResult(winner, homeRoundsWon, awayRoundsWon, homeMatchPoints)
        awayResult = MatchResult(!winner, awayRoundsWon, homeRoundsWon, awayMatchPoints)
        return Pair(homeResult, awayResult)
    }

    fun matchString(homeResult: MatchResult, awayResult: MatchResult): String {
        return String.format("%d-%d", homeResult.roundsWon, awayResult.roundsWon)
    }

    private fun runRound(homeWinChance: Double, random: Random): Boolean {
        return random.nextInt(0,100 + 1) < homeWinChance * 100
    }

    fun getExpectedMatchPoints(homeSkill: Double, awaySkill: Double): DoubleArray {
        val diff = homeSkill - awaySkill
        val homeWinChance = 1 / (1 + exp(-1 * ODDS_SCALING_FACTOR * diff))

        // Get average of results for koth and stopwatch
        val gameTypes = arrayOfNulls<IntArray>(2)
        for ((arrayTicker, winLimit) in intArrayOf(2, 4).withIndex()) {
            var homeRoundsWon: Int
            var awayRoundsWon: Int
            val possibleLoserRounds = (winLimit until (winLimit*2)).map {winLimit.toDouble()/it}
                .toTypedArray()

            if (homeWinChance > 0.5) {
                homeRoundsWon = winLimit
                awayRoundsWon = ((winLimit.toDouble() / ArrayMatch.findClosestArrayMatch(homeWinChance, possibleLoserRounds))
                        -winLimit).roundToInt()
            } else if (homeWinChance < 0.5) {
                homeRoundsWon = ((winLimit.toDouble() / ArrayMatch.findClosestArrayMatch(1-homeWinChance, possibleLoserRounds))
                        -winLimit).roundToInt()
                awayRoundsWon = winLimit
            } else {
                throw RuntimeException("Cannot check expected win chance for teams with equal skill")
            }
            gameTypes[arrayTicker] = getMatchPoints(homeRoundsWon, awayRoundsWon)
        }
        val homeMatchPoints: Double = gameTypes.map { it!![0] }.average()
        val awayMatchPoints: Double = gameTypes.map { it!![1] }.average()
        return doubleArrayOf(homeMatchPoints, awayMatchPoints)
    }

    private fun getMatchPoints(homeRoundsWon: Int, awayRoundsWon: Int): IntArray {
        val koth = homeRoundsWon.coerceAtLeast(awayRoundsWon) == 4
        var homeMatchPoints = 0
        var awayMatchPoints = 0
        if (!koth) {
            homeMatchPoints = (homeRoundsWon.toDouble() / (homeRoundsWon + awayRoundsWon) * 9).roundToInt()
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
}

data class MatchResult(val won: Boolean, val roundsWon: Int, val roundsLost: Int, val matchPoints: Int)