package org.devcloud.waypoints.command


object CommandName {
    const val BASE_COMMAND = "waypoints"
    const val GLOBAL_COMMAND = "global"
    const val LIST_COMMAND = "list"
    const val HELP_COMMAND = "help"
    const val HIDE_COMMAND = "hide"
    const val CREATE_COMMAND = "create"
    const val DELETE_COMMAND = "delete"
    const val TELEPORT_COMMAND = "teleport"
    const val RELOAD_COMMAND = "reload"
}

enum class HideType {
    ALL,
    LOCAL,
    GLOBAL
}