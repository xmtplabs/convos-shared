package org.convos.bridge.runtime

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class JsStringLiteralTest {

    @Test
    fun `plain text is quoted verbatim`() {
        assertThat(jsStringLiteral("""{"id":"1","ok":true}"""))
            .isEqualTo("\"{\\\"id\\\":\\\"1\\\",\\\"ok\\\":true}\"")
    }

    @Test
    fun `backslashes and quotes are escaped`() {
        assertThat(jsStringLiteral("""a\b"c""")).isEqualTo("\"a\\\\b\\\"c\"")
    }

    @Test
    fun `newlines and control characters are escaped`() {
        assertThat(jsStringLiteral("a\nb\rc\td")).isEqualTo("\"a\\nb\\rc\\u0009d\"")
    }

    @Test
    fun `js line separators are escaped`() {
        val input = "a" + 0x2028.toChar() + "b" + 0x2029.toChar() + "c"
        assertThat(jsStringLiteral(input)).isEqualTo("\"a\\u2028b\\u2029c\"")
    }
}
