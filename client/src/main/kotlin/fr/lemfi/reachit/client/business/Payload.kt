package fr.lemfi.reachit.client.business

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type")
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = NoMultipartPayload::class, name = "nomultipart"),
    JsonSubTypes.Type(value = MultipartPayload::class, name = "multipart")
])
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
)