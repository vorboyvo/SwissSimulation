package com.vibeisveryo.RGLHighlanderMatchPointSimulation;

import com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement.MeasureRandom;
import com.vibeisveryo.RGLHighlanderMatchPointSimulation.measurement.MeasureRandomThenSwiss;
import com.vibeisveryo.RGLHighlanderMatchPointSimulation.tournament.Division;

import java.io.IOException;

public class SimpleMain {
    public static void main(String[] args) throws IOException {
        MeasureRandomThenSwiss.measureCombinedDistortions(1, 10, 37, 100, Division.SkillStyle.UNIFORM, 1/3.0);
    }
}
