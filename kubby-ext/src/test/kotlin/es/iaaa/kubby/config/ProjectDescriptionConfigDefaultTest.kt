package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.commandLineConfig
import es.iaaa.kubby.fixtures.defaultCommentProperties
import es.iaaa.kubby.fixtures.defaultImageProperties
import es.iaaa.kubby.fixtures.defaultLabelProperties
import es.iaaa.kubby.fixtures.defaultUsePrefixes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProjectDescriptionConfigDefaultTest {

    lateinit var config: Config

    @BeforeTest
    fun before() {
        config = commandLineConfig(arrayOf())
    }

    @Test
    fun `default project name is null`() {
        assertNull(config.toProjectDescription().projectName)
    }

    @Test
    fun `default project homepage is null`() {
        assertNull(config.toProjectDescription().projectHomepage)
    }

    @Test
    fun `default label properties are defined`() {
        assertEquals(defaultLabelProperties, config.toProjectDescription().labelProperties.map { it.uri })
    }

    @Test
    fun `default comment properties are defined`() {
        assertEquals(defaultCommentProperties, config.toProjectDescription().commentProperties.map { it.uri })
    }

    @Test
    fun `default image properties are defined`() {
        assertEquals(defaultImageProperties, config.toProjectDescription().imageProperties.map { it.uri })
    }

    @Test
    fun `default use prefixes are defined`() {
        assertEquals(defaultUsePrefixes, config.toProjectDescription().usePrefixes)
    }

    @Test
    fun `default language is defined`() {
        assertEquals("en", config.toProjectDescription().defaultLanguage)
    }

    @Test
    fun `default index resource is null`() {
        assertNull(config.toProjectDescription().indexLocalPart)
    }
}


