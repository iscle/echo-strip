package com.github.iscle.echostrip.data.ble

/**
 * A 9-byte ELK-BLEDOM frame. Frames that share a [key] replace each other while queued, so a
 * slider being dragged sends only its latest value.
 */
class StripCommand(val key: String, val bytes: ByteArray) {
    init {
        require(bytes.size == 9) { "Frames are 9 bytes" }
    }

    override fun toString(): String = "$key[${bytes.joinToString(" ") { "%02X".format(it) }}]"
}
