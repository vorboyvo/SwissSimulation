package com.vibeisveryo.tournamentsim.measurement

import com.vibeisveryo.tournamentsim.tournament.Division
import com.vibeisveryo.tournamentsim.tournament.Division.SkillStyle
import com.vibeisveryo.tournamentsim.util.OutWriter
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object MeasureRoundRobin {
    @Throws(IOException::class)
    fun getStandingsOverASeason(iterations: Int, matchCount: Int, teamCount: Int) {
        val outWriter = OutWriter("standings_weeks_rr", "week", "skillRank", "leagueTableRank")
        //int matchCount = teamCount - 1;
        for (i in 0 until iterations) {
            val startTime = Instant.now()
            val main = Division("Main", teamCount, SkillStyle.UNIFORM)
            for (week in 0 until matchCount) {
                main.rrRunMatches()
                // Get team skill rank
                val teamSkillRanks = main.teamSkillRanks()
                for (j in 0 until teamCount) {
                    outWriter.addRecord(week, teamSkillRanks[j], j)
                }
            }
            outWriter.print()
            val endTime = Instant.now()
            val time = Duration.between(startTime, endTime).toNanos()
            if (i % 10.0.pow(floor(log10((iterations - 1).toDouble()))) == 0.0
                || time > TimeUnit.SECONDS.toNanos(1L)
            ) System.out.printf("Iteration %d took %4.5f seconds\n", i, time / 1000000000.0)
        }
    }
}