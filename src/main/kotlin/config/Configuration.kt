package es.iaaa.kubby.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract


data class Route(
    val resource: String,
    val data : String,
    val page : String
)

object Configuration {
    val config : Config = ConfigFactory.load()
    val route = config.extract<Route>("kubby.route")
}

