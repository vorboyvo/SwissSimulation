package com.vibeisveryo.tournamentsim.util

import kotlin.math.abs

object ArrayMatch {
    /**
     * Finds an element of an array with the minimum one-dimensional Euclidean distance from our given value
     * @param value value to be found the closest match
     * @param array array to be searched within for the closest match
     * @return value of the closest match
     */
    @JvmStatic fun findClosestArrayMatch(value: Double, array: Array<Double>): Double {
        var match = array[0]
        for (i in 1 until array.size) {
            if (abs(value-array[i]) < abs(value-match)) {
                match = array[i]
            }
        }
        return match
    }
}