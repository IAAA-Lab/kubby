package es.iaaa.kubby.ktor.features

import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl
import java.nio.charset.Charset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class VelocityTest {

    lateinit var engine: VelocityEngine
    lateinit var conf: VelocityConfiguration

    @BeforeTest
    fun before() {
        engine = VelocityEngine()
        conf = VelocityConfiguration(VelocityTest::class.java.classLoader,"template/", ".vm", Charset.forName("UTF-8"))
    }

    @Test
    fun `verify that resources are loaded after the setup`() {
        engine.setup(conf)
        val loader = engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepositoryImpl
        assertNotNull(loader.getStringResource("first.vm"))
    }

    @Test
    fun `verify that nested resources are loaded after the setup`() {
        engine.setup(conf)
        val loader = engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT) as StringResourceRepositoryImpl
        assertNotNull(loader.getStringResource("folder/second.vm"))
    }
}