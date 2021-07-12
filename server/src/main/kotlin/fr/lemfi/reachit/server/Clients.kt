package fr.lemfi.reachit.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancel
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

private val logger = LoggerFactory.getLogger("Reachit-Server")

private val clients = Collections.synchronizedMap<String, DefaultWebSocketServerSession>(mutableMapOf())

suspend fun notifyClient(name: String, key: String) =
    checkClientConnected(name).send(key)

fun checkClientConnected(developer: String) = clients[developer] ?: throw IllegalArgumentException("$developer is not connected!")

fun Application.setupClientsConnections() {

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
    }

    routing {

        webSocket("/socket") {
            val name = call.parameters.get("name").apply { logger.info("hello $this!") }
                ?: throw IllegalArgumentException("who are you dude?")
            clients.put(name, this)
            try {
                incoming.receive()
            } catch (e: Throwable) {
                // fail silently
                clients.remove(name)
                logger.info("bye $name!")
            }
        }
    }
}