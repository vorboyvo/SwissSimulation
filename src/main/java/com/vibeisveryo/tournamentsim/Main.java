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

package com.vibeisveryo.tournamentsim;

import com.vibeisveryo.tournamentsim.benchmarking.Benchmark;
import com.vibeisveryo.tournamentsim.tournament.Division;

import static com.vibeisveryo.tournamentsim.measurement.MeasureSwiss.measureCombinedDistortions;
import static com.vibeisveryo.tournamentsim.measurement.MeasureSwiss.measureDistortionsOverMatches;

public class Main {
    public static final Division.SkillStyle SKILL_STYLE = Division.SkillStyle.UNIFORM;

    private static void helpCommand() {
        String usageString = "Usage: java -jar RGLHighlanderMatchPointSimulation.jar [OPTION]... <COMMAND> [<ARGS>]...";
        String[] helpStrings = {
                "distMatches: Measure distortions over adding matches; usage: distMatches <matchesStart> <matchesStop> "
                + "<teamCount> <iterations>",
                "distCombined: Measure distortions over matches and teams; usage: distCombined <matchesStart> " +
                        "<teamsStart> <teamsStop> <iterations>",
                "benchmarkSeason: Benchmarks the performance of a season over many iterations; usage: " +
                        "benchmarkSeason <iterations> <teams> <matches>",
                "benchmarkMatches: Benchmarks how long team sizes take in relation to each other; usage: " +
                        "benchmarkMatches <iterations> <maxTeams>"
        };
        System.out.println(usageString);
        for (String helpString: helpStrings) {
            System.out.print("   ");
            System.out.println(helpString);
        }
    }

    public static void main(String[] args) throws Exception {
        // Handle command line arguments
        if (args.length == 0) {
            helpCommand();
            System.exit(0);
        }
        switch (args[0].toLowerCase()) {
            case "help" -> helpCommand();
            case "distmatches" -> {
                if (args.length == 5)
                    measureDistortionsOverMatches(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]), Integer.parseInt(args[4]), SKILL_STYLE);
                else helpCommand();
            }
            case "distcombined" -> {
                if (args.length == 5)
                    measureCombinedDistortions(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]), Integer.parseInt(args[4]), SKILL_STYLE);
                else helpCommand();
            }
            case "benchmarkseason" -> {
                if (args.length == 4)
                    Benchmark.benchSeason(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]));
                else if (args.length == 3)
                    Benchmark.benchSeason(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            (int) (Math.ceil(Integer.parseInt(args[2]) / 2.0) * 2 - 2));
                else Benchmark.benchSeason(500, 30, 27);
            }
            case "benchmarkmatches" -> {
                if (args.length == 3)
                    Benchmark.benchSwissMatches(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                else if (args.length == 2)
                    Benchmark.benchSwissMatches(Integer.parseInt(args[1]), 999);
                else Benchmark.benchSwissMatches(100,34);
            }
        }
    }
}