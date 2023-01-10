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
package com.vibeisveryo.tournamentsim.measurement;

import com.vibeisveryo.tournamentsim.tournament.Division;
import com.vibeisveryo.tournamentsim.tournament.Team;
import com.vibeisveryo.tournamentsim.util.OutWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vibeisveryo.tournamentsim.measurement.Distortions.getDistortions;
import static com.vibeisveryo.tournamentsim.measurement.Distortions.sumDistortionsPerTeam;

public class MeasureRandom {
    @SuppressWarnings("unused")
    public static void measureCombinedDistortions(int matchesStart, int teamsStart, int teamsStop,
                                                  int iterations, Division.SkillStyle skillStyle) throws IOException {
        OutWriter outWriter = new OutWriter("distortions_random_combined", "matches","teams","distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            for (int teamCount = teamsStart; teamCount < teamsStop; teamCount++) {
                for (int matchCount = matchesStart; matchCount < Math.ceil(teamCount / 2.0) * 2 - 2; matchCount++) {
                    Division main = new Division("Main", teamCount, skillStyle);
                    main.randomRunMatches(matchCount);
                    List<Integer> distortions = getDistortions(main);
                    outWriter.addRecord(matchCount, teamCount,
                            String.format("%3.5f", sumDistortionsPerTeam(distortions)));
                }
            }
            outWriter.print();
            Instant endTime = Instant.now();
            long time = Duration.between(startTime, endTime).toNanos();
            if (i % Math.pow(10.0, Math.floor(Math.log10(iterations-1))) == 0
                    || time > TimeUnit.SECONDS.toNanos(1L))
                System.out.printf("Iteration %d took %4.5f seconds\n", i, time/1000000000.0);
        }

        // Output CSV
        outWriter.close();
    }

    @SuppressWarnings("unused")
    public static void getStandingsOverASeason(int iterations, int matchCount, int teamCount) throws IOException {

        OutWriter outWriter = new OutWriter("standings_weeks_random", "week","skillRank","leagueTableRank","teams");

        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            Division main = new Division("Main", teamCount, Division.SkillStyle.UNIFORM);
            for (int week = 0; week < matchCount; week++) {
                main.randomRunMatches(1);
                // Get team skill rank
                List<Integer> teamSkillRanks = main.getTeamList().stream().map(team -> {
                    List<Team> teams = new ArrayList<>(main.getTeamList());
                    teams.sort(Collections.reverseOrder((o1, o2) -> {
                        double diff = o1.getTeam().getSkill() - o2.getTeam().getSkill();
                        return (int) ((diff >= 0) ? Math.ceil(diff) : Math.floor(diff));
                    }));
                    return teams.indexOf(team);
                }).toList();
                for (int j = 0; j < teamCount; j++) {
                    outWriter.addRecord(week, teamSkillRanks.get(j), j, teamCount);
                }
            }
            outWriter.print();
            Instant endTime = Instant.now();
            long time = Duration.between(startTime, endTime).toNanos();
            if (i % Math.pow(10.0, Math.floor(Math.log10(iterations-1))) == 0
                    || time > TimeUnit.SECONDS.toNanos(1L))
                System.out.printf("Iteration %d took %4.5f seconds\n", i, time/1000000000.0);
        }
    }
}
