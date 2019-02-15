package es.iaaa.kubby.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import es.iaaa.kubby.ktor.features.VelocityConfiguration
import io.mockk.every
import io.mockk.mockk
import java.nio.charset.Charset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VelocityConfigTest {

    lateinit var config: Config

    @BeforeTest
    fun before() {
        config = mockk()
        every { config.getConfig("kubby.velocity") } returns ConfigFactory.parseMap(mapOf(
            "resource-loader-path" to "templates/",
            "suffix" to ".vm",
            "charset" to "UTF-8"
        ))
    }

    @Test
    fun `derive velocity configuration from application environment`() {
        val expect = VelocityConfiguration(
            classLoader = VelocityConfigTest::class.java.classLoader,
            resourceLoaderPath = "templates/",
            suffix = ".vm",
            charset = Charset.forName("UTF-8")
        )
        assertEquals(expect, velocityConfig(VelocityConfigTest::class.java.classLoader, config))
    }
}