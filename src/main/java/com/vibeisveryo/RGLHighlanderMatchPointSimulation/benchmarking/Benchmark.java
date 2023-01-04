package com.vibeisveryo.RGLHighlanderMatchPointSimulation.benchmarking;

import com.vibeisveryo.RGLHighlanderMatchPointSimulation.Main;
import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Division;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class Benchmark {
    public static void benchSwissMatches(int iters, int maxTeams) {
        for (int teamCount = 10; teamCount <= maxTeams; teamCount+=2) {
            Instant start = Instant.now();
            int matchCount = (int) Math.ceil(teamCount/2.0)*2-3;
            for (int i = 0; i < iters; i++) {
                Division divMain = new Division("Main", teamCount, Main.SKILL_STYLE);
                divMain.swissRunMatches(matchCount);
            }
            Instant end = Instant.now();
            System.out.printf("%d teams and %d matches took %d milliseconds\n", teamCount, matchCount,
                    Duration.between(start, end).toMillis()
            );
        }
    }

    public static void benchRandomMatches(int iters, int maxTeams) {
        for (int teamCount = 10; teamCount <= maxTeams; teamCount+=2) {
            Instant start = Instant.now();
            int matchCount = (int) Math.ceil(teamCount/2.0)*2-3;
            for (int i = 0; i < iters; i++) {
                Division divMain = new Division("Main", teamCount, Main.SKILL_STYLE);
                divMain.randomRunMatches(matchCount);
            }
            Instant end = Instant.now();
            System.out.printf("%d teams and %d matches took %d milliseconds\n", teamCount, matchCount,
                    Duration.between(start, end).toMillis()
            );
        }
    }

    public static void benchSeason(int iters, int teamCount, int matchCount) {
        int[] durations = new int[iters];
        for (int i = 0; i < iters; i++) {
            Instant start = Instant.now();
            Division main = new Division("Main", teamCount, Main.SKILL_STYLE);
            main.swissRunMatches(matchCount);
            Instant stop = Instant.now();
            durations[i] = (int) Duration.between(start, stop).toMillis();
        }
        int min = Integer.MAX_VALUE;
        int max = 0;
        double mean = 0;
        double vari = 0;
        double stdev;
        double median = 0;

        for (int i = 0; i < iters; i++) {
            mean += durations[i];
            if (durations[i] < min) min = durations[i];
            if (durations[i] > max) max = durations[i];
        }
        mean /= iters;

        for (int i = 0; i < iters; i++) {
            vari += Math.pow(durations[i] - mean, 2);
        }
        vari /= iters;
        stdev = Math.sqrt(vari);

        Arrays.sort(durations);
        if (iters % 2 == 0) {
            median = (durations[iters/2-1] + durations[iters/2]) / 2.0;
        } else {
            median = durations[iters/2];
        }

        System.out.printf("min: %d max: %d mean: %.3f stdev: %.3f median: %.3f\n", min, max, mean, stdev, median);
    }
}
