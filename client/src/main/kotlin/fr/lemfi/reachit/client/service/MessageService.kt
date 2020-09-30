package fr.lemfi.reachit.client.service

import fr.lemfi.reachit.client.business.Payload
import fr.lemfi.reachit.client.configuration.ForwardProperties
import fr.lemfi.reachit.client.configuration.ServerProperties
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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
        val forwardProperties: ForwardProperties
) {

    fun onMessage(payload: Payload) {
        httpClient.newCall(
                Request.Builder()
                        .url(forwardProperties.host + payload.path)
                        .method(payload.method, payload.body?.toRequestBody("application/json".toMediaType()))
                        .build()
        ).execute()
                .let {
                    response ->
                    httpClient.newCall(
                            Request.Builder()
                                    .url(serverProperties.api + "/" + payload.key)
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