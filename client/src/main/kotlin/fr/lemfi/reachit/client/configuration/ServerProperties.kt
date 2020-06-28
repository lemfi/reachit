package fr.lemfi.reachit.client.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "server")
open class ServerProperties {

    lateinit var socket: String
    lateinit var api: String
    lateinit var developer: String
}