package fr.lemfi.reachit.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.lemfi.reachit.client.model.Payload
import io.ktor.client.*
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
private val serverHost = property { serverhost }
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

    HttpClient {
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
    try {
        retrievePayload(key)
            .run { forwardRequest(key, this) }
            .apply { answer(key, this) }
    } catch (e: Throwable) {
        logger.warn("an error orccured while forwarding request: ", e.message, e)
    }
}

private suspend fun HttpClient.retrievePayload(key: String): Payload {

    logger.info("receiving $key")

    return get("$httpProtocol://$serverHost/payloads") {
        parameter("uuid", key)
    }
}

private suspend fun HttpClient.forwardRequest(key: String, payload: Payload): HttpResponse {
    return try {

        logger.info("Forwarding request ${payload.method} ${payload.path} ...")

        request<HttpStatement>("$forwardUrl${payload.path}") {
            method = HttpMethod(payload.method)

            payload.headers
                .filterUnsafeHeaders()
                .forEach {
                    headers.append(it.key, it.value)
                }

            if (payload.body != null) {
                body = ByteArrayContent(
                    payload.body,
                    ContentType.parse(payload.headers["Content-Type"] ?: "application/octet-stream")
                )
            }
        }.execute()
    } catch (e: Throwable) {
        answerError(key)
        throw e
    }
}

private suspend fun HttpClient.answer(
    key: String,
    httpResponse: HttpResponse
) {
    post<HttpStatement>("$httpProtocol://$serverHost/resp/$key") {
        method = HttpMethod.Post
        headers.apply {
            httpResponse.headers
                .toMap()
                .filterUnsafeHeaders()
                .forEach { (key, value) ->
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

private suspend fun HttpClient.answerError(
    key: String
) {
    post<HttpStatement>("$httpProtocol://$serverHost/resp/$key") {
        method = HttpMethod.Post
        headers.apply { append("X-Reachit-Status", "503") }
        body = TextContent(
            """{ "error": "server is currently not available" }""",
            ContentType.parse("application/json")
        )

    }.execute()
}

private fun <R> Map<String, R>.filterUnsafeHeaders() =
    filterNot {
        it.key.uppercase() == "CONTENT-LENGTH"
                || it.key.uppercase() == "CONTENT-TYPE"
                || it.key.uppercase() == "TRANSFER-ENCODING"
    }