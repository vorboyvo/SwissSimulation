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

import com.vibeisveryo.tournamentsim.measurement.Distortions
import com.vibeisveryo.tournamentsim.measurement.MeasureSwiss
import com.vibeisveryo.tournamentsim.simulation.Division
import com.vibeisveryo.tournamentsim.tournament.Swiss
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        val timeTaken = Array(500) { Array(33) { 0.0 } }
        for (i in 0 until 500) {
            val main = Division("Main", 36, Division.SkillStyle.TRUE_RANDOM)
            for (j in 0 until 33) {
                val startTime = Instant.now()
                Swiss.swissRunMatches(main, 1)
                val endTime = Instant.now()
                val time = Duration.between(startTime, endTime).toNanos()
                timeTaken[i][j] = (time / 1000000000.0)
            }
        }
        println(timeTaken.map {
            it.joinToString {  }
            val builder = StringBuilder()
            for (i in it) {
                builder.append("${"%3.4f".format(i)},")
            }
            builder.append('\n')
        })
    }
}