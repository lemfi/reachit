package fr.lemfi.reachit.server.business

import java.util.*

sealed class Payload(val type: String) {
    val key: String = UUID.randomUUID().toString()

    abstract val method: String
    abstract val path: String
    abstract val headers: MutableMap<String, String>
    abstract val contentType: String?
}

data class NoMultipartPayload(
        override val contentType: String?,
        override val headers: MutableMap<String, String>,
        override val method: String,
        override val path: String,

        val body: ByteArray?
): Payload("nomultipart")

data class MultipartPayload(
        override val contentType: String?,
        override val headers: MutableMap<String, String>,
        override val method: String,
        override val path: String,

        val parts: List<Part>
): Payload("multipart")

data class Part(
        val data: ByteArray,
        val contentType: String,
        val name: String,
        val file: Boolean,
        val filename: String? = null
//        val size: Long,
//        val headers: MutableMap<String, String?>
)