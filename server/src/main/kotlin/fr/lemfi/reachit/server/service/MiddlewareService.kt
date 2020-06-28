package fr.lemfi.reachit.server.service

import fr.lemfi.reachit.server.business.Payload
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class MiddlewareService(val messageService: MessageService) {

    val channels: MutableMap<String, Channel<Any>> = mutableMapOf()

    fun notify(developer: String, payload: Payload): Any {
        return runBlocking {
            Channel<Any>().also {
                messageService.notify(developer, payload).apply {
                    channels.put(payload.key, it)
                }
            }.receive().apply {
                channels.remove(payload.key)
            }
        }
    }

    fun response(id: String, body: Any) {
        channels.filter { it.key == id }.values.firstOrNull()?.sendBlocking(body)
    }
}