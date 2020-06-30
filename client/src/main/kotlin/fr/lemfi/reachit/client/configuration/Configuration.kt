package fr.lemfi.reachit.client.configuration

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class Configuration {

    @Bean
    open fun httpClient(): OkHttpClient {
        return OkHttpClient.Builder().followRedirects(false).build()
    }

}