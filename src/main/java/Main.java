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
import java.util.stream.IntStream;

public class Main {
    public static final int SKILL_STYLE = 1;
    public static final boolean MEASURE_TIME = false;

    static class OutWriter {
        FileWriter outputWriter;
        CSVPrinter csvPrinter;
        Thread hook;

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
     * @param teamCount Number of teams
     * @param matchCount Number of matches; 0 for round-robin, otherwise capped at teamCount-2 or teamCount-3, if
     *                   teamCount is odd or even respectively.
     * @return the absolute value of the sum of all distortions, divided by the number of teams.
     */
    private static double getDistortions(int teamCount, int matchCount) {
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
        List<Integer> distortions = IntStream.range(0, main.getTeamList().size())
                .mapToObj(i -> i - teamSkillRanks.get(i)).toList();
        // Sum abs value of distortions
        return distortions.stream().mapToDouble(i->Math.abs(i)/(double) main.getTeamList().size()).sum();
    }

    /**
     * Measures distortions over adding new matches and outputs data to a csv
     * @param start Number of matches minimum, inclusive
     * @param stop Number of matches minimum, exclusive
     * @param teamCount Number of teams to keep constant
     * @param iterations Number of times to run
     */
    private static void measureDistortionsOverMatches(int start, int stop, int teamCount, int iterations) throws IOException {
        OutWriter outWriter = new OutWriter("distortions_matches", "matches", "distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            for (int j = start; j < stop; j++) {
                outWriter.csvPrinter.printRecord(
                        j, getDistortions(teamCount, j)
                );
            }
        }

        // Close writer
        outWriter.close();
    }

    private static void measureCombinedDistortions(int matchesStart, int teamsStart, int teamsStop, int iterations) throws IOException {
        OutWriter outWriter = new OutWriter("distortions_combined", "matches","teams","distortions");

        // Do the iters
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            for (int j = teamsStart; j < teamsStop; j++) {
                for (int k = matchesStart; k < Math.ceil(j / 2.0) * 2 - 2; k++) {
                    outWriter.csvPrinter.printRecord(
                            k, j, getDistortions(j, k)
                    );
                }
            }
            Instant endTime = Instant.now();
            System.out.printf("Iteration %d took %4.5f seconds\n", i, Duration.between(startTime, endTime).toNanos()/1000000000.0);
        }

        // Output CSV
        outWriter.close();
    }

    public static void main(String[] args) throws Exception {
        measureCombinedDistortions(4,10,32,1);
    }

}