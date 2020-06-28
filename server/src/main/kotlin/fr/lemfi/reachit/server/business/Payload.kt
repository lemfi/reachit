package fr.lemfi.reachit.server.business

import java.util.*

data class Payload(
        val method: String,
        val path: String,
        val key: String = UUID.randomUUID().toString(),
        val body: String? = null
)