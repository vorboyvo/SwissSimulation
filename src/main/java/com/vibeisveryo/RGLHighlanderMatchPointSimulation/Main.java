package com.vibeisveryo.RGLHighlanderMatchPointSimulation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Main {
    public static final int SKILL_STYLE = 1;
    public static final boolean MEASURE_TIME = false;

    static class OutWriter {
        FileWriter outputWriter;
        CSVPrinter csvPrinter;

        OutWriter(String title, Object... headerValues) throws IOException {
            // Create output file
            String dateTime = DateTimeFormatter.ofPattern("MMddyyyyHHmmss").format(ZonedDateTime.now());
            File outputFile = new File("results/" + title + "_" + dateTime + ".csv");
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();

            // Create output writer
            this.outputWriter = new FileWriter(outputFile);
            this.csvPrinter = new CSVPrinter(this.outputWriter, CSVFormat.DEFAULT);
            this.csvPrinter.printRecord(headerValues);
        }

        void close() throws IOException {
            this.outputWriter.close();
            this.outputWriter = null;
            this.csvPrinter = null;
        }
    }

    /**
     * Gets distortions per team in a single simulated division.
     *
     * @param teamCount  Number of teams
     * @param matchCount Number of matches; 0 for round-robin, otherwise capped at teamCount-2 or teamCount-3, if
     *                   teamCount is odd or even respectively.
     * @return the absolute value of the sum of all distortions, divided by the number of teams.
     */
    private static List<Integer> getDistortions(int teamCount, int matchCount) {
        Division main = new Division(
                "Main",
                null,
                teamCount,
                SKILL_STYLE,
                0,
                -1L
        );
        if (matchCount == 0) {
            main.rrRunMatches();
        } else {
            main.swissRunMatches(matchCount);
        }

        // Get team skill rank
        List<Integer> teamSkillRanks = main.getTeamList().stream().map(team -> {
            List<TeamContext> teams = new ArrayList<>(main.getTeamList());
            teams.sort(Collections.reverseOrder((o1, o2) -> {
                double diff = o1.getTeam().getSkill() - o2.getTeam().getSkill();
                return (int) ((diff >= 0) ? Math.ceil(diff) : Math.floor(diff));
            }));
            return teams.indexOf(team);
        }).toList();
        // Get distortions
        // Sum abs value of distortions
        return IntStream.range(0, main.getTeamList().size())
                .mapToObj(i -> i - teamSkillRanks.get(i)).toList();
    }

    /**
     * Measures distortions over adding new matches and outputs data to a csv
     * @param start Number of matches minimum, inclusive
     * @param stop Number of matches minimum, exclusive
     * @param teamCount Number of teams to keep constant
     * @param iterations Number of times to run
     */
    public static void measureDistortionsOverMatches(int start, int stop, int teamCount, int iterations)
            throws IOException {
        OutWriter outWriter = new OutWriter("distortions_matches", "matches", "distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            for (int j = start; j < stop; j++) {
                List<Integer> distortions = getDistortions(teamCount, j);
                outWriter.csvPrinter.printRecord(
                        j,
                        String.format("%3.5f",
                                distortions.stream().mapToDouble(k->Math.abs(k)/(double) distortions.size()).sum()
                        )
                );
            }
        }

        // Close writer
        outWriter.close();
    }

    public static void measureCombinedDistortions(int matchesStart, int matchesStop, int teamsStart, int teamsStop,
                                                  int iterations) throws IOException {
        OutWriter outWriter = new OutWriter("distortions_combined", "matches","teams","distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            ArrayList<Object[]> records = new ArrayList<>();
            for (int j = teamsStart; j < teamsStop; j++) {
                for (int k = matchesStart; k < (matchesStop < 0 ? Math.ceil(j / 2.0) * 2 - 2 : matchesStop); k++) {
                    List<Integer> distortions = getDistortions(j, k);
                    records.add(new Object[]{
                            k,
                            j,
                            String.format("%3.5f",
                                    distortions.stream().mapToDouble(l->Math.abs(l)/(double) distortions.size()).sum()
                            )
                    });
                }
            }
            outWriter.csvPrinter.printRecords(records);
            Instant endTime = Instant.now();
            long time = Duration.between(startTime, endTime).toNanos();
            if (i % Math.pow(10.0, Math.floor(Math.log10(iterations-1))) == 0
                    || time > TimeUnit.SECONDS.toNanos(1L))
                System.out.printf("Iteration %d took %4.5f seconds\n", i, time/1000000000.0);
        }

        // Output CSV
        outWriter.close();
    }

    public static void measureCombinedTopHalfDistortions(int matchesStart, int matchesStop, int teamsStart,
                                                         int teamsStop, int iterations) throws IOException {
        OutWriter outWriter = new OutWriter(
                "distortions_combined",
                "matches",
                "teams",
                "distortionstophalf",
                "distortionstoptwothirds",
                "distortions"
                );

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            ArrayList<Object[]> records = new ArrayList<>();
            for (int j = teamsStart; j < teamsStop; j++) {
                for (int k = matchesStart; k < (matchesStop < 0 ? Math.ceil(j / 2.0) * 2 - 2 : matchesStop); k++) {
                    List<Integer> distortions = getDistortions(j, k);
                    int halfTeams = (int) Math.round(j/2.0);
                    int twoThirdsTeams = (int) Math.round(2*j/3.0);
                    records.add(new Object[]{
                            k,
                            j,
                            String.format("%3.5f", distortions.subList(0, (int) Math.round(j/2.0))
                                    .stream().mapToDouble(l->Math.abs(l)/(double) halfTeams).sum()),
                            String.format("%3.5f", distortions.subList(0, (int) Math.round(2*j/3.0))
                                    .stream().mapToDouble(l->Math.abs(l)/(double) twoThirdsTeams).sum()),
                            String.format("%3.5f", distortions
                                    .stream().mapToDouble(l->Math.abs(l)/(double) distortions.size()).sum())
                    });
                }
            }
            outWriter.csvPrinter.printRecords(records);
            Instant endTime = Instant.now();
            long time = Duration.between(startTime, endTime).toNanos();
            if (i % Math.pow(10.0, Math.floor(Math.log10(iterations-1))) == 0
                    || time > TimeUnit.SECONDS.toNanos(1L))
                System.out.printf("Iteration %d took %4.5f seconds\n", i, time/1000000000.0);
        }

        // Output CSV
        outWriter.close();
    }

    private static void helpCommand() {
        String usageString = "Usage: java -jar RGLHighlanderMatchPointSimulation.jar [OPTION]... <COMMAND> [<ARGS>]...";
        String[] helpStrings = {
                "distMatches: Measure distortions over adding matches; usage: distMatches <matchesStart> <matchesStop> "
                + "<teamCount> <iterations>",
                "distCombined: Measure distortions over matches and teams; usage: distCombined <matchesStart> " +
                        "<matchesStop> <teamsStart> <teamsStop> <iterations>" +
                        "; if matchesStop is -1 it is auto-determined"
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
        switch (args[0]) {
            case "help" -> helpCommand();
            case "distMatches" -> {
                if (args.length == 5)
                    measureDistortionsOverMatches(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                else helpCommand();
            }
            case "distCombined" -> {
                if (args.length == 6)
                    measureCombinedDistortions(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                else helpCommand();
            }
        }
    }
}