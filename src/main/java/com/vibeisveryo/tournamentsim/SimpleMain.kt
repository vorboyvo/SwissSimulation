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

import com.vibeisveryo.tournamentsim.benchmarking.Benchmark
import com.vibeisveryo.tournamentsim.benchmarking.Benchmark.benchSwissMatches
import com.vibeisveryo.tournamentsim.measurement.MeasureSwiss
import com.vibeisveryo.tournamentsim.tournament.Division
import java.io.IOException

object SimpleMain {
    @Throws(IOException::class)
    @JvmStatic
    fun main(vararg args: String) {
        MeasureSwiss.measureCombinedDistortionsTwoMatches(1,10,36,1000,Division.SkillStyle.UNIFORM)
        MeasureSwiss.measureCombinedDistortions(1,10,36,1000,Division.SkillStyle.UNIFORM)
    }
}