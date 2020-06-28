package fr.lemfi.reachit.server.service

import com.fasterxml.jackson.databind.ObjectMapper
import fr.lemfi.reachit.server.business.Payload
import fr.lemfi.reachit.server.business.Room
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Component
class MessageService(val objectMapper: ObjectMapper) {

    val rooms: MutableSet<Room> = mutableSetOf()

    fun acceptSession(session: WebSocketSession) {
        getRoomName(session)
                .also { if (getRoom(it) != null ) throw IllegalArgumentException("developer $it already connected!") }
                .let { rooms.add(Room(it, session)) ; println("Hello $it!") }
    }

    fun removeSession(session: WebSocketSession) {
        getRoom(session).firstOrNull()?.let { rooms.remove(it) ; println("Bye ${it.name}!") }
    }

    fun notify(developer: String, payload: Payload) {
        getRoom(developer)?.developer?.sendMessage(
                TextMessage(objectMapper.writeValueAsBytes(payload))
        ) ?: throw IllegalArgumentException("developer $developer is not listening")
    }

    private fun getRoom(developer: String) = rooms.filter { developer == it.name }.firstOrNull()
    private fun getRoom(session: WebSocketSession) = rooms.filter { session == it.developer }

    private fun getRoomName(session: WebSocketSession) = session.uri?.query?.split("&")?.map { it.split("=") }?.map { it[0] to it[1] }?.find { it.first == "name" }?.second ?: throw IllegalArgumentException("who are you?")
}