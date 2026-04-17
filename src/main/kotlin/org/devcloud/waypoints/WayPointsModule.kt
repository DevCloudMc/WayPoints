package org.devcloud.waypoints

import io.github.bananapuncher714.cartographer.core.module.Module

class WayPointsModule : Module() {
    private var bootstrap: WayPointsBootstrap? = null

    override fun onEnable() {
        val b = WayPointsBootstrap(this)
        bootstrap = b
        try {
            b.start()
            logger.info("WayPoints 3.0.0 enabled.")
        } catch (e: Exception) {
            logger.severe("Failed to start WayPoints: ${e.message}")
            e.printStackTrace()
            cartographer.moduleManager.disableModule(this)
        }
    }

    override fun onDisable() {
        bootstrap?.stop()
        bootstrap = null
    }
}
