package fr.lemfi.reachit.client.model

import java.nio.charset.Charset

data class Payload(
    val headers: MutableMap<String, String>, // TODO change values to list
    val method: String,
    val path: String,

    val body: ByteArray?,
) {
    override fun toString(): String {
        return """
            $headers
            $method
            ${body?.toString(Charset.defaultCharset())}
        """.trimIndent()
    }
}