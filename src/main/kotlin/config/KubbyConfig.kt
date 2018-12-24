package es.iaaa.kubby.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object KubbyConfig {
    private val config = ConfigFactory.load()
    val route = Route

    object Route {
        val resource = config.getStringOrDefault("kubby.route.resource", "/resource")
        val data = config.getStringOrDefault("kubby.route.data", "/data")
        val page = config.getStringOrDefault("kubby.route.page", "/page")
    }
}

fun Config.getStringOrDefault(path: String, default: String): String =
    if (hasPath(path)) {
        getString(path)
    } else {
        default
    }
