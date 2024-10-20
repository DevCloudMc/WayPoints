package org.devcloud.waypoints.util

object LocationUtil {

    fun round(loc: Double): Double {
        return Math.round(loc * 10.0) / 10.0
    }

    fun round(loc: Float): Float {
        return Math.round(loc * 10f) / 10f
    }
}