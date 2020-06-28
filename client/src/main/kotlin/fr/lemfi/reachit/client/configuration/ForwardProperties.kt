package fr.lemfi.reachit.client.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "forward")
open class ForwardProperties {

    lateinit var host: String
}