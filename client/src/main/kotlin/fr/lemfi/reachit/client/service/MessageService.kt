package fr.lemfi.reachit.client.service

import fr.lemfi.reachit.client.business.Payload
import fr.lemfi.reachit.client.configuration.ForwardProperties
import fr.lemfi.reachit.client.configuration.ServerProperties
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Component

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
                    httpClient.newCall(
                            Request.Builder()
                                    .url(serverProperties.api + "/" + payload.key)
                                    .method("POST", it.body?.string()?.toRequestBody("application/json".toMediaType()))
                                    .build()
                    ).execute()
                }


    }
}