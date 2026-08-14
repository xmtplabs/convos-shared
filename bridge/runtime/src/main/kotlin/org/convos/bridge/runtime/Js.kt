package org.convos.bridge.runtime

/**
 * Renders [value] as a double-quoted JavaScript string literal, escaping
 * everything that could break out of the literal — including U+2028/U+2029,
 * which are valid in JSON strings but are line terminators in JavaScript.
 *
 * Response envelopes are delivered as
 * `window.convos._handleResponse(JSON.parse(<literal>))`, so the envelope only
 * ever crosses into the page as an inert string.
 */
fun jsStringLiteral(value: String): String = buildString(value.length + 2) {
    append('"')
    for (ch in value) {
        when {
            ch == '\\' -> append("\\\\")
            ch == '"' -> append("\\\"")
            ch == '\n' -> append("\\n")
            ch == '\r' -> append("\\r")
            ch.code == 0x2028 -> append("\\u2028")
            ch.code == 0x2029 -> append("\\u2029")
            ch < ' ' -> append("\\u%04x".format(ch.code))
            else -> append(ch)
        }
    }
    append('"')
}
