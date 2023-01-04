package com.vibeisveryo.RGLHighlanderMatchPointSimulation;

import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Division;
import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {

    private static void testCase1() {
        /*
         * Division Main with 16 teams
         * Name,Skill,W,L,RW,RL,MP,Teams Faced
         * Team A,3.000,4,0,16,3,33,[Team J, Team F, Team E, Team B]
         * Team B,2.600,3,1,15,4,30,[Team D, Team H, Team C, Team A]
         * Team C,1.800,3,1,12,4,27,[Team O, Team N, Team B, Team H]
         * Team D,1.000,3,1,12,5,26,[Team B, Team I, Team K, Team J]
         * Team E,2.200,3,1,12,6,25,[Team P, Team M, Team A, Team F]
         * Team F,1.400,2,2,10,8,20,[Team G, Team A, Team M, Team E]
         * Team G,-0.200,2,2,11,9,20,[Team F, Team J, Team P, Team M]
         * Team H,0.200,2,2,8,8,18,[Team I, Team B, Team N, Team C]
         * Team I,-0.600,2,2,8,8,18,[Team H, Team D, Team O, Team N]
         * Team J,0.600,2,2,9,11,16,[Team A, Team G, Team L, Team D]
         * Team K,-1.400,2,2,8,10,16,[Team M, Team P, Team D, Team L]
         * Team L,-1.800,1,3,6,12,11,[Team N, Team O, Team J, Team K]
         * Team M,-1.000,1,3,5,12,10,[Team K, Team E, Team F, Team G]
         * Team N,-2.200,1,3,4,12,9,[Team L, Team C, Team H, Team I]
         * Team O,-3.000,1,3,4,12,9,[Team C, Team L, Team I, Team P]
         * Team P,-2.600,0,4,0,16,0,[Team E, Team K, Team G, Team O]
         *
         * Expected output:
         * [[Team A, Team C], [Team B, Team E], [Team D, Team F], [Team G, Team H], [Team I, Team J], [Team K, Team N], [Team L, Team P], [Team M, Team O]]
         */
        List<Team> teamList = new ArrayList<>();
        // Add teams
        teamList.add(new Team("Team A", 3.000));
        teamList.add(new Team("Team B", 2.600));
        teamList.add(new Team("Team C", 1.800));
        teamList.add(new Team("Team D", 1.000));
        teamList.add(new Team("Team E", 2.200));
        teamList.add(new Team("Team F", 1.400));
        teamList.add(new Team("Team G", -0.200));
        teamList.add(new Team("Team H", 0.200));
        teamList.add(new Team("Team I", -0.600));
        teamList.add(new Team("Team J", 0.600));
        teamList.add(new Team("Team K", -1.400));
        teamList.add(new Team("Team L", -1.800));
        teamList.add(new Team("Team M", -1.000));
        teamList.add(new Team("Team N", -2.200));
        teamList.add(new Team("Team O", -3.000));
        teamList.add(new Team("Team P", -2.600));
        Division main = new Division("Main", teamList);
        // We don't set a seed since no randomness should be involved here.
        // If the results are not deterministic, we have a problem regardless of potential randomness.

        // Add matches
        main.addPreviousPair(teamList.get(0), teamList.get(1)).addPreviousPair(teamList.get(0), teamList.get(4)).addPreviousPair(teamList.get(0), teamList.get(5)).addPreviousPair(teamList.get(0), teamList.get(9)).addPreviousPair(teamList.get(1), teamList.get(2)).addPreviousPair(teamList.get(1), teamList.get(3)).addPreviousPair(teamList.get(1), teamList.get(7)).addPreviousPair(teamList.get(2), teamList.get(7)).addPreviousPair(teamList.get(2), teamList.get(13)).addPreviousPair(teamList.get(2), teamList.get(14)).addPreviousPair(teamList.get(3), teamList.get(8)).addPreviousPair(teamList.get(3), teamList.get(9)).addPreviousPair(teamList.get(3), teamList.get(10)).addPreviousPair(teamList.get(4), teamList.get(5)).addPreviousPair(teamList.get(4), teamList.get(12)).addPreviousPair(teamList.get(4), teamList.get(15)).addPreviousPair(teamList.get(5), teamList.get(6)).addPreviousPair(teamList.get(5), teamList.get(12)).addPreviousPair(teamList.get(6), teamList.get(9)).addPreviousPair(teamList.get(6), teamList.get(12)).addPreviousPair(teamList.get(6), teamList.get(15)).addPreviousPair(teamList.get(7), teamList.get(8)).addPreviousPair(teamList.get(7), teamList.get(13)).addPreviousPair(teamList.get(8), teamList.get(13)).addPreviousPair(teamList.get(8), teamList.get(14)).addPreviousPair(teamList.get(9), teamList.get(11)).addPreviousPair(teamList.get(10), teamList.get(11)).addPreviousPair(teamList.get(10), teamList.get(12)).addPreviousPair(teamList.get(10), teamList.get(15)).addPreviousPair(teamList.get(11), teamList.get(13)).addPreviousPair(teamList.get(11), teamList.get(14)).addPreviousPair(teamList.get(14), teamList.get(15));

        System.out.println(main.scheduleWeek().stream()
                .map(a -> Arrays.deepToString(Arrays.stream(a).map(b -> b.getTeam().getName()).toArray())).toList()
        );
    }

    public static void main(String[] args) throws Exception {
        testCase1();
    }
}