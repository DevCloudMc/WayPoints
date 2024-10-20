package org.devcloud.waypoints.exception


class DependencyException(
    message: String,
    val dependencyName: String
) : RuntimeException(message)


class PlayerNotFoundException(message: String) : RuntimeException(message)