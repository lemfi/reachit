package fr.lemfi.reachit.server.model

class Response(
    val status: Int,
    val headers: Map<String, String>,
    val body: ByteArray?
)