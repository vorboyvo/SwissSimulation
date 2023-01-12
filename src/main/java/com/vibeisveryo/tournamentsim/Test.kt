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
package com.vibeisveryo.tournamentsim

import com.vibeisveryo.tournamentsim.tournament.Division
import com.vibeisveryo.tournamentsim.tournament.Team
import java.util.*

object Test {
    private fun testCase1() {
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
        val teamList: MutableList<Team> = ArrayList()
        // Add teams
        teamList.add(Team("Team A", 3.000))
        teamList.add(Team("Team B", 2.600))
        teamList.add(Team("Team C", 1.800))
        teamList.add(Team("Team D", 1.000))
        teamList.add(Team("Team E", 2.200))
        teamList.add(Team("Team F", 1.400))
        teamList.add(Team("Team G", -0.200))
        teamList.add(Team("Team H", 0.200))
        teamList.add(Team("Team I", -0.600))
        teamList.add(Team("Team J", 0.600))
        teamList.add(Team("Team K", -1.400))
        teamList.add(Team("Team L", -1.800))
        teamList.add(Team("Team M", -1.000))
        teamList.add(Team("Team N", -2.200))
        teamList.add(Team("Team O", -3.000))
        teamList.add(Team("Team P", -2.600))
        val main = Division("Main", teamList)
        // We don't set a seed since no randomness should be involved here.
        // If the results are not deterministic, we have a problem regardless of potential randomness.

        // Add matches
        main.addPreviousPair(teamList[0], teamList[1]).addPreviousPair(teamList[0], teamList[4]).addPreviousPair(
            teamList[0], teamList[5]
        ).addPreviousPair(teamList[0], teamList[9]).addPreviousPair(teamList[1], teamList[2])
            .addPreviousPair(teamList[1], teamList[3]).addPreviousPair(
            teamList[1], teamList[7]
        ).addPreviousPair(teamList[2], teamList[7]).addPreviousPair(teamList[2], teamList[13])
            .addPreviousPair(teamList[2], teamList[14]).addPreviousPair(
            teamList[3], teamList[8]
        ).addPreviousPair(teamList[3], teamList[9]).addPreviousPair(teamList[3], teamList[10])
            .addPreviousPair(teamList[4], teamList[5]).addPreviousPair(
            teamList[4], teamList[12]
        ).addPreviousPair(teamList[4], teamList[15]).addPreviousPair(teamList[5], teamList[6])
            .addPreviousPair(teamList[5], teamList[12]).addPreviousPair(
            teamList[6], teamList[9]
        ).addPreviousPair(teamList[6], teamList[12]).addPreviousPair(teamList[6], teamList[15])
            .addPreviousPair(teamList[7], teamList[8]).addPreviousPair(
            teamList[7], teamList[13]
        ).addPreviousPair(teamList[8], teamList[13]).addPreviousPair(teamList[8], teamList[14])
            .addPreviousPair(teamList[9], teamList[11]).addPreviousPair(
            teamList[10], teamList[11]
        ).addPreviousPair(teamList[10], teamList[12]).addPreviousPair(teamList[10], teamList[15]).addPreviousPair(
            teamList[11], teamList[13]
        ).addPreviousPair(teamList[11], teamList[14]).addPreviousPair(teamList[14], teamList[15])
        println(
            main.scheduleWeek().stream()
                .map { a: Array<Team>? ->
                    Arrays.deepToString(Arrays.stream(a).map { b: Team -> b.team.name }
                        .toArray())
                }.toList()
        )
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        testCase1()
    }
}