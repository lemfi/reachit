package fr.lemfi.reachit.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {

    embeddedServer(CIO, port = 8080) {

        install(ContentNegotiation) {
            jackson()
        }

        routing {

            setupGateway()
            setupClientsConnections()
            setupProxyResponses()
        }
    }.start(wait = true)

}

