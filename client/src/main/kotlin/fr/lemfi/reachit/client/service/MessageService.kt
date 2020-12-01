package fr.lemfi.reachit.client.service

import com.fasterxml.jackson.databind.ObjectMapper
import fr.lemfi.reachit.client.business.Payload
import fr.lemfi.reachit.client.configuration.ForwardProperties
import fr.lemfi.reachit.client.configuration.ServerProperties
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.closeQuietly
import okhttp3.internal.headersContentLength
import okio.BufferedSink
import okio.Source
import okio.source
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class MessageService(
        val httpClient: OkHttpClient,
        val serverProperties: ServerProperties,
        val objectMapper: ObjectMapper,
        val forwardProperties: ForwardProperties
) {

    fun onMessage(uuid: String) {
        httpClient.newCall(
                Request.Builder()
                        .url("${serverProperties.api}/payloads?uuid=$uuid")
                        .method("GET", null)
                        .build()
        ).execute().body!!.byteStream().let {
            objectMapper.readValue(it, Payload::class.java)
        }.also {
            treatPayload(it)
        }
    }

    private fun treatPayload(payload: Payload) {
        httpClient.newCall(
                Request.Builder()
                        .url(forwardProperties.host + payload.path)
                        .method(payload.method, payload.body?.toRequestBody(payload.headers["content-type"]?.toMediaType() ?: "application/json".toMediaType()))
                        .headers(payload.headers.toHeaders())
                        .build()
        ).execute()
                .let {
                    response ->
                    httpClient.newCall(
                            Request.Builder()
                                    .url("${serverProperties.api}/resp/${payload.key}")
                                    .method("POST", response.body?.byteStream()?.toRequestBody(response.header("Content-Type", "application/octet-stream;Charset=binary")!!.toMediaType(), response.headersContentLength()))
                                    .apply {
                                        response.headers.forEach {
                                            addHeader(it.first, it.second)
                                        }
                                        addHeader("X-Reachit-Status", response.code.toString())
                                    }
                                    .build()
                    ).execute()
                }


    }
}

fun InputStream.toRequestBody(mediaType: MediaType, contentLength: Long): RequestBody {
    return object : RequestBody() {
        override fun contentType(): MediaType {
            return mediaType
        }

        override fun contentLength(): Long {
            return contentLength
        }

        override fun writeTo(sink: BufferedSink) {
            var source: Source? = null
            try {
                source = this@toRequestBody.source()
                sink.writeAll(source)
            } finally {
                source?.closeQuietly()
            }
        }
    }

}