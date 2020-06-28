package fr.lemfi.reachit.client.business

data class Payload(
        val key: String,
        val method: String,
        val path: String,
        val body: String? = null
)