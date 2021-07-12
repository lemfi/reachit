package fr.lemfi.reachit.server

import fr.lemfi.reachit.server.model.Response
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import java.util.*

fun Application.setupProxyResponses() {

    routing {

        post("/resp/{key}") {

            call.response.status(HttpStatusCode.NoContent)

            val response = Response(
                status = call.request.headers["X-Reachit-Status"]!!.toInt(),
                headers = call.request.headers.toMap().filterNot {
                    it.key.uppercase().startsWith("X-REACHIT-")
                            || it.key.uppercase().equals("CONNECTION")
                            || it.key.uppercase().equals("CONTENT-LENGTH")
                }.mapValues { it.value.first() },
                body = call.request.receiveChannel().toByteArray()
            )

            answer(call.parameters["key"]!!, response)
        }

        get("/payloads") {

            val key = call.parameters["uuid"] ?: throw IllegalArgumentException("payload key should be provided")
            val payload = payload(key)

            call.respond(payload)
        }

    }
}
