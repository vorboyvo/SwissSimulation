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
