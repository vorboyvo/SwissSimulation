package com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement;

import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Division;
import com.vibeisveryo.RGLHighlanderMatchPointSimulation.util.OutWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement.Distortions.*;

public class MeasureSwiss {

    @SuppressWarnings("unused")
    public static void measureDistortionsOverMatches(int matchStart, int matchStop, int teamCount, int iterations,
                                                     Division.SkillStyle skillStyle) throws IOException {
        OutWriter outWriter = new OutWriter("distortions_matches", "matches", "distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            for (int matchCount = matchStart; matchCount < matchStop; matchCount++) {
                Division main = new Division("Main", teamCount, skillStyle);
                main.swissRunMatches(matchCount);
                List<Integer> distortions = getDistortions(main);
                outWriter.print(
                        matchCount,
                        String.format("%3.5f", sumDistortionsPerTeam(distortions))
                );
            }
        }
        // Close writer
        outWriter.close();
    }

    @SuppressWarnings("unused")
    public static void measureCombinedDistortions(int matchesStart, int teamsStart, int teamsStop,
                                                  int iterations, Division.SkillStyle skillStyle) throws IOException {
        OutWriter outWriter = new OutWriter("distortions_combined", "matches","teams","distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            for (int teamCount = teamsStart; teamCount < teamsStop; teamCount++) {
                for (int matchCount = matchesStart; matchCount < Math.ceil(teamCount / 2.0) * 2 - 2; matchCount++) {
                    Division main = new Division("Main", teamCount, skillStyle);
                    main.swissRunMatches(matchCount);
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
    public static void measureCombinedFractionalDistortions(int matchesStart, int matchesStop,int teamsStart,
                                                         int teamsStop, int iterations,
                                                         Division.SkillStyle skillStyle) throws IOException {
        OutWriter outWriter = new OutWriter("distortions_combined", "matches", "teams",
                "distortionstophalf", "distortionstoptwothirds", "distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            for (int teamCount = teamsStart; teamCount < teamsStop; teamCount++) {
                for (int matchCount = matchesStart; matchCount < (matchesStop < 0 ? Math.ceil(teamCount / 2.0) * 2 - 2 : matchesStop); matchCount++) {
                    Division main = new Division("Main", teamCount, skillStyle);
                    main.swissRunMatches(matchCount);
                    List<Integer> distortions = getDistortions(main);
                    outWriter.addRecord(matchCount, teamCount,
                            String.format("%3.5f", sumFractionalDistortions(distortions, 1/2.0)),
                            String.format("%3.5f", sumFractionalDistortions(distortions, 1/3.0)),
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
