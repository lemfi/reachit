package fr.lemfi.reachit.server

import fr.lemfi.reachit.server.model.Payload
import fr.lemfi.reachit.server.model.Response
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Reachit-Server")

private val channels: MutableMap<String, Channel<Response>> = mutableMapOf()
private val payloads: MutableMap<String, Payload> = mutableMapOf()

suspend fun answer(key: String, response: Response) =
    channels[key]?.send(response) ?: throw IllegalStateException("no matching key")

fun payload(key: String) = payloads[key] ?: throw IllegalStateException("no matching key")

fun Application.setupGateway() {

    routing {

        get("/req/{developer}/{...}") {
            manageRequest()
        }
        post("/req/{developer}/{...}") {
            manageRequest()
        }
        put("/req/{developer}/{...}") {
            manageRequest()
        }
        delete("/req/{developer}/{...}") {
            manageRequest()
        }

    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.manageRequest() {
    val developer = call.parameters["developer"]!!

    logger.info("receiving request for $developer")

    checkClientConnected(developer)

    val payload = extractPayload(developer)
    payloads.put(payload.key, payload)

    logger.info("   ${payload.method} ${payload.path}")

    val response = Channel<Response>().also {
        notifyClient(developer, payload.key)
        channels.put(payload.key, it)
    }.receive().apply {
        channels.remove(payload.key)
    }

    call.response.status(HttpStatusCode.fromValue(response.status))
    call.response.headers.apply {
        response.headers.forEach {
            append(it.key, it.value, false)
        }
    }
    call.respondBytes {
        response.body!!
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.extractPayload(developer: String): Payload {
    val headers = call.request.headers.toMap().toMutableMap().map { it.key to it.value.first() }.toMap().toMutableMap()
    val method = call.request.httpMethod.value
    val path = call.request.path().substringAfter("/req/$developer")
    val body = call.request.receiveChannel().toByteArray().let { if (it.isEmpty()) null else it }

    return Payload(headers, method, path, body)
}
