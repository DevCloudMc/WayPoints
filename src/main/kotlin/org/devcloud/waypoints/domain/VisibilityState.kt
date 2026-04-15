package org.devcloud.waypoints.domain

data class VisibilityState(
    val hidePersonal: Boolean = false,
    val hideGlobal: Boolean = false,
    val hideShared: Boolean = false,
) {
    companion object {
        val ALL_VISIBLE = VisibilityState()
        val ALL_HIDDEN = VisibilityState(true, true, true)
    }
}
