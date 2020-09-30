package fr.lemfi.reachit.server.business

class Response(
        val status: Int,
        val headers: Map<String, String>,
        val body: ByteArray?
)