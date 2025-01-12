package io.kotless

/** Representation of URI path. Removes blank parts. */
data class URIPath(var parts: Iterable<String>) {
    init {
        parts = parts.filter { it.isNotBlank() }
    }

    constructor() : this(emptyList())
    constructor(path: URIPath, part: String) : this(path.parts + part)
    constructor(part: String, path: URIPath) : this(listOf(part) + path.parts)

    override fun toString() = parts.joinToString(separator = "/")
}

fun Iterable<String>.toURIPath() = URIPath(this)
fun String.toURIPath() = URIPath(this.split("/"))
