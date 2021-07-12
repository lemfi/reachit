package fr.lemfi.reachit.client

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.fp.invalid

inline fun <T> property(l: Properties.() -> T): T {
    val conf = listOf(
        System.getProperty("reachit-conf", ""),
        System.getenv().getOrDefault("REACHIT_CONF", ""),
        "/reachit.yml"
    ).let { source ->
        var configuration: ConfigResult<Properties> = ConfigFailure.UnknownSource("").invalid()
        val sourcesIterator = source.iterator()
        while (configuration.isInvalid() && sourcesIterator.hasNext()) {
            configuration = ConfigLoader
                .Builder()
                .addPropertySource(
                    EnvironmentVariablesPropertySource(
                        useUnderscoresAsSeparator = true,
                        allowUppercaseNames = true
                    )
                )
                .build()
                .loadConfig(sourcesIterator.next())
        }
        configuration.getUnsafe()
    }
    return conf.l()
}

data class Properties(
    val developer: String,
    val serverHost: String,
    val wsprotocol: String,
    val httpprotocol: String,
    val forwardurl: String,
)