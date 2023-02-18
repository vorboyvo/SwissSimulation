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

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        //MeasureSwiss.measureDistortions(5000, 10, 36, Division.SkillStyle.TRUE_RANDOM)
        fun <T> MutableList<T>.rotateLeft(places: Int) {
            val temp = this.drop(places) + this.take(places)
            for (j in 0 until this.size) {
                this[j] = temp[j]
            }
        }
        val hiii = mutableListOf("a", "b", "c", "d", "e", "f", "g")
        hiii.rotateLeft(2)
        println(hiii)
    }
}