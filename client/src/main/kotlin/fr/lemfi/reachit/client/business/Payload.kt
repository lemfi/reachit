package fr.lemfi.reachit.client.business

data class Payload(
        val key: String,
        val method: String,
        val path: String,
        val headers: MutableMap<String, String>,
        val body: String? = null
)