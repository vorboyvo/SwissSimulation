package com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement;

import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Division;
import com.vibeisveryo.RGLHighlanderMatchPointSimulation.util.OutWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement.Distortions.getDistortions;
import static com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement.Distortions.sumDistortionsPerTeam;

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
