package org.convos.metrics.descriptors

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MetricsStableIdEncoder(
    private val salt: ByteArray,
    private val info: ByteArray,
) {
    fun derive(privateKey: ByteArray): String {
        val prk = hmacSha256(if (salt.isEmpty()) ByteArray(SHA256_LEN) else salt, privateKey)
        val t = hmacSha256(prk, info + byteArrayOf(0x01))
        return t.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALG)
        mac.init(SecretKeySpec(key, HMAC_ALG))
        return mac.doFinal(data)
    }

    companion object {
        private const val HMAC_ALG = "HmacSHA256"
        private const val SHA256_LEN = 32
    }
}
