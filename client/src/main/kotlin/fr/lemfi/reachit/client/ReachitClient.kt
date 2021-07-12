package fr.lemfi.reachit.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.lemfi.reachit.client.model.Payload
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Reachit-Client")

private val developer = property { developer }
private val serverHost = property { serverHost }
private val forwardUrl = property { forwardurl }
private val httpProtocol = property { httpprotocol }
private val wsProtocol = property { wsprotocol }

fun main() {

    while (true) {
        try {
            runBlocking {
                logger.info("connecting $developer...")
                connectToServer()
            }
        } catch (e: Throwable) {
            logger.info("$developer disconnected, reconnecting")
            Thread.sleep(1000)
        }
    }
}

private suspend fun connectToServer() {

    HttpClient(CIO) {
        expectSuccess = false
        install(WebSockets)
        install(JsonFeature) {
            serializer = JacksonSerializer(
                jacksonObjectMapper().configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
                )
            )
        }
    }.apply {

        webSocket("$wsProtocol://$serverHost/socket?name=$developer") {
            logger.info("connection ok!")
            for (data in incoming) {
                forward((data as Frame.Text).readText())
            }
        }
    }
}

private suspend fun HttpClient.forward(key: String) {
    retrievePayload(key)
        .run { forwardRequest(this) }
        .apply { answer(key, this) }
}

private suspend fun HttpClient.retrievePayload(key: String): Payload {

    logger.info("receiving $key")

    return get("$httpProtocol://$serverHost/payloads") {
        parameter("uuid", key)
    }
}

private suspend fun HttpClient.forwardRequest(payload: Payload): HttpResponse {
    return request<HttpStatement>("$forwardUrl${payload.path}") {
        method = HttpMethod(payload.method)

        payload.headers.filterNot {
            it.key.uppercase() == "CONTENT-LENGTH" || it.key.uppercase() == "CONTENT-TYPE"
        }.forEach {
            headers.append(it.key, it.value)
        }

        if (payload.body != null) {
            body = ByteArrayContent(
                payload.body,
                ContentType.parse(payload.headers["Content-Type"] ?: "application/octet-stream")
            )
        }
    }.execute()
}

private suspend fun HttpClient.answer(
    key: String,
    httpResponse: HttpResponse
) {
    post<HttpStatement>("$httpProtocol://$serverHost/resp/$key") {
        method = HttpMethod.Post
        headers.apply {
            httpResponse.headers.toMap().filterNot {
                it.key.uppercase() == "CONTENT-LENGTH" || it.key.uppercase() == "CONTENT-TYPE"
            }.forEach { (key, value) ->
                append(key, value.first())
            }
            append("X-Reachit-Status", "${httpResponse.status.value}")
        }
        body = ByteArrayContent(
            httpResponse.readBytes(),
            ContentType.parse(httpResponse.headers["Content-Type"] ?: "application/octet-stream")
        )
    }.execute()
}