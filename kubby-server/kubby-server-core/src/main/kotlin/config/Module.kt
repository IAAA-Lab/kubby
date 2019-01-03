package es.iaaa.kubby.config

import es.iaaa.kubby.repository.*
import org.koin.dsl.module.module
import java.nio.file.Path
import java.nio.file.Paths

val target: Path = Paths.get("build/datasource/tdb2")
val data: Path = Paths.get("src/test/resources/Tetris.n3")


val module = module(createOnStart = false) {
    single<DataSource> {
        val config = Tdb2DataSourceConfiguration(
            path = target,
            definition = DatasourceDefinition.CREATE,
            data = data
        )
        val ds = Tdb2DataSource(config)
        RewrittenDataSource(ds, "http://dbpedia.org/resource/", true)
    }
}
