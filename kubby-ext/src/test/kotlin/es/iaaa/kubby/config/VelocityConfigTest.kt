package es.iaaa.kubby.config

import es.iaaa.kubby.ktor.features.VelocityConfiguration
import io.ktor.application.ApplicationEnvironment
import io.ktor.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import java.nio.charset.Charset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VelocityConfigTest {

    lateinit var env: ApplicationEnvironment

    @BeforeTest
    fun before() {
        env = mockk()
        every { env.config.config("kubby.velocity") } returns MapApplicationConfig(
            "resource-loader-path" to "templates/",
            "suffix" to ".vm",
            "charset" to "UTF-8"
        )
        every { env.classLoader } returns VelocityConfigTest::class.java.classLoader
    }

    @Test
    fun `derive velocity configuration from application environment`() {
        val expect = VelocityConfiguration(
            classLoader = VelocityConfigTest::class.java.classLoader,
            resourceLoaderPath = "templates/",
            suffix = ".vm",
            charset = Charset.forName("UTF-8")
        )
        assertEquals(expect, env.toVelocityConfiguration())
    }
}