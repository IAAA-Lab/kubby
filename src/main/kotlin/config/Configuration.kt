package es.iaaa.kubby.config

import com.typesafe.config.ConfigFactory
import es.iaaa.kubby.util.getStringOrDefault

object Configuration {
    private val config = ConfigFactory.load()
    val route = Route

    object Route {
        val resource = config.getStringOrDefault("kubby.route.resource", "/resource")
        val data = config.getStringOrDefault("kubby.route.data", "/data")
        val page = config.getStringOrDefault("kubby.route.page", "/page")
    }
}

