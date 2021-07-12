package fr.lemfi.reachit.server.model

import java.util.*

class Payload(

    val headers: MutableMap<String, String>,
    val method: String,
    val path: String,

    val body: ByteArray?,

    val key: String = UUID.randomUUID().toString(),
)