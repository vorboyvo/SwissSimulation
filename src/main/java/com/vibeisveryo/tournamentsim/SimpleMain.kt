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
package com.vibeisveryo.tournamentsim;

import com.vibeisveryo.tournamentsim.measurement.MeasureSwiss;

import java.io.IOException;

public class SimpleMain {
    public static void main(String[] args) throws IOException {
        int i = 14;
        int matchCount = (int) Math.ceil(i / 2.0)*2 - 3;
        MeasureSwiss.getStandingsOverASeason(1, matchCount, i);
    }
}
