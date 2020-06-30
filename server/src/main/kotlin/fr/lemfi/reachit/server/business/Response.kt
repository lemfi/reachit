package fr.lemfi.reachit.server.business

data class Response(
        val status: Int,
        val headers: Map<String, String>,
        val body: Any?
)