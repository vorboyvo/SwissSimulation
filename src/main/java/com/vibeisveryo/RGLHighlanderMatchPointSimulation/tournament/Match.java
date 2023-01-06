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
package com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament;

import java.util.Random;

public class Match {
    private static final int ODDS_SCALING_FACTOR = 2;
    private final MatchResult homeMatchResult;
    private final MatchResult awayMatchResult;

    /**
     * koth is a boolean value, taking on false if the match is stopwatch and true if the match is koth.
     * <p>
     * The difference in skill (home - away) is a proxy for how much more likely one team is to win ANY GIVEN
     * ROUND than the other. It is passed as a parameter to a logistic equation f(x) = 1 / (1 + e^-(jx)).
     * <p>
     * Choosing exogenous j such that it is the lowest j that gives an average size of distortions relative to
     * skill less than 500, so that each match, on average, has less than 5 distortions.
     * <p>
     * We run the match until we have a winner, i.e. at least one team has won 4 rounds on koth or 2 on stopwatch.
     */
    public Match(double homeSkill, double awaySkill, boolean koth, Random random) {
        double diff = homeSkill - awaySkill;
        double homeWinChance = 1 / (1 + Math.exp(-1 * ODDS_SCALING_FACTOR * diff));

        // Run first to 4 on koth or 2 on stopwatch
        int winLimit = koth ? 4 : 2;

        int homeRoundsWon = 0;
        int awayRoundsWon = 0;
        while (homeRoundsWon < winLimit && awayRoundsWon < winLimit) {
            boolean roundOutcome = runRound(homeWinChance, random);
            if (roundOutcome) {
                homeRoundsWon += 1;
            } else {
                awayRoundsWon += 1;
            }
        }

        boolean winner = homeRoundsWon > awayRoundsWon;
        int[] matchPoints = getMatchPoints(homeRoundsWon, awayRoundsWon);
        int homeMatchPoints = matchPoints[0];
        int awayMatchPoints = matchPoints[1];
        double[] inqMatchPoints = getInqMatchPoints(homeRoundsWon, awayRoundsWon);
        double homeInqMatchPoints = inqMatchPoints[0];
        double awayInqMatchPoints = inqMatchPoints[1];

        // Treat stopwatch rounds as 2 each - doing this after the above because bug discovered
        if (!koth) {
            homeRoundsWon *= 2;
            awayRoundsWon *= 2;
        }

        homeMatchResult = new MatchResult(winner, homeRoundsWon, awayRoundsWon, homeMatchPoints, homeInqMatchPoints);
        awayMatchResult = new MatchResult(!winner, awayRoundsWon, homeRoundsWon, awayMatchPoints, awayInqMatchPoints);
    }

    public Match(boolean homeIsBye) {
        MatchResult byeResult = new MatchResult(false, 0, 4, 0, 0);
        MatchResult nonByeResult = new MatchResult(true, 4, 0, 9, 9);
        if (homeIsBye) {
            this.homeMatchResult = byeResult;
            this.awayMatchResult = nonByeResult;
        } else {
            this.homeMatchResult = nonByeResult;
            this.awayMatchResult = byeResult;
        }
    }

    public MatchResult getHomeResult() {
        return homeMatchResult;
    }

    public MatchResult getAwayResult() {
        return awayMatchResult;
    }

    @Override
    public String toString() {
        return String.format("%d-%d",homeMatchResult.roundsWon(), awayMatchResult.roundsWon());
    }

    private static boolean runRound(double homeWinChance, Random random) {
        return random.nextInt(100 + 1) < homeWinChance * 100;
    }

    public static int[] getMatchPoints(int homeRoundsWon, int awayRoundsWon) {
        boolean koth = Math.max(homeRoundsWon, awayRoundsWon) == 4;

        int homeMatchPoints = 0;
        int awayMatchPoints = 0;
        if (!koth) {
            homeMatchPoints = (int) Math.round(((double) homeRoundsWon / (homeRoundsWon + awayRoundsWon)) * 9);
            awayMatchPoints = 9 - homeMatchPoints;
        } else {
            if (homeRoundsWon > awayRoundsWon) {
                homeMatchPoints += 6;
                awayMatchPoints = awayRoundsWon;
                homeMatchPoints += 3 - awayMatchPoints;
            } else {
                awayMatchPoints += 6;
                homeMatchPoints = homeRoundsWon;
                awayMatchPoints += 3 - homeMatchPoints;
            }
        }

        return new int[]{homeMatchPoints, awayMatchPoints};
    }

    public static double[] getInqMatchPoints(int homeRoundsWon, int awayRoundsWon) {
        double homeMatchPoints = (double) homeRoundsWon * 9 / (homeRoundsWon + awayRoundsWon);
        double awayMatchPoints = (double) awayRoundsWon * 9 / (homeRoundsWon + awayRoundsWon);
        return new double[]{homeMatchPoints, awayMatchPoints};
    }
}

record MatchResult(boolean won, int roundsWon, int roundsLost, int matchPoints, double inqMatchPoints) {}