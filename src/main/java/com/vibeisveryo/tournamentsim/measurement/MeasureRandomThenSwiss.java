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
import com.vibeisveryo.tournamentsim.util.OutWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vibeisveryo.tournamentsim.measurement.Distortions.getDistortions;
import static com.vibeisveryo.tournamentsim.measurement.Distortions.sumDistortionsPerTeam;

public class MeasureRandomThenSwiss {
    @SuppressWarnings("unused")
    public static void measureCombinedDistortions(int matchesStart, int teamsStart, int teamsStop,
                                                  int iterations, Division.SkillStyle skillStyle, double propRandom)
            throws IOException {
        OutWriter outWriter = new OutWriter("distortions_randswiss_combined", "matches","teams","distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            for (int teamCount = teamsStart; teamCount < teamsStop; teamCount++) {
                for (int matchCount = matchesStart; matchCount < Math.ceil(teamCount / 2.0) * 2 - 2; matchCount++) {
                    int randMatchCount = (int) Math.ceil(matchCount * propRandom);
                    int swissMatchCount = matchCount - randMatchCount;
                    Division main = new Division("Main", teamCount, skillStyle);
                    main.randomRunMatches(randMatchCount);
                    main.swissRunMatches(swissMatchCount);
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
}
