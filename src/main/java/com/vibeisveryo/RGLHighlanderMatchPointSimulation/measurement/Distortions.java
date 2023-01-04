package com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement;

import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Division;
import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Distortions {

    /**
     * Gets distortions per team in a single simulated division.
     *
     * @return the absolute value of the sum of all distortions, divided by the number of teams.
     */
    public static List<Integer> getDistortions(Division division) {
        // Get team skill rank
        List<Integer> teamSkillRanks = division.getTeamList().stream().map(team -> {
            List<Team> teams = new ArrayList<>(division.getTeamList());
            teams.sort(Collections.reverseOrder((o1, o2) -> {
                double diff = o1.getTeam().getSkill() - o2.getTeam().getSkill();
                return (int) ((diff >= 0) ? Math.ceil(diff) : Math.floor(diff));
            }));
            return teams.indexOf(team);
        }).toList();
        // Return abs value of distortions
        return IntStream.range(0, division.getTeamList().size())
                .mapToObj(i -> Math.abs(i - teamSkillRanks.get(i))).toList();
    }

    public static double sumDistortionsPerTeam(List<Integer> distortions) {
        return distortions.stream().mapToDouble(k->Math.abs(k)/(double) distortions.size()).sum();
    }

    public static double sumFractionalDistortions(List<Integer> distortions, double fraction) {
        int teamsMeasured = (int) Math.round(distortions.size()/fraction);
        return distortions.subList(0, teamsMeasured).stream().mapToDouble(l->Math.abs(l)/(double) teamsMeasured).sum();
    }
}
