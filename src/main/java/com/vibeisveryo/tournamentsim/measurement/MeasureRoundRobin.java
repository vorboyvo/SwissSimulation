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

public class MeasureRoundRobin {

    @SuppressWarnings("unused")
    public static void getStandingsOverASeason(int iterations, int matchCount, int teamCount) throws IOException {

        OutWriter outWriter = new OutWriter("standings_weeks_rr", "week","skillRank","leagueTableRank");
        //int matchCount = teamCount - 1;
        for (int i = 0; i < iterations; i++) {
            Instant startTime = Instant.now();
            Division main = new Division("Main", teamCount, Division.SkillStyle.UNIFORM);
            for (int week = 0; week < matchCount; week++) {
                main.rrRunMatches();
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
                    outWriter.addRecord(week, teamSkillRanks.get(j), j);
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
