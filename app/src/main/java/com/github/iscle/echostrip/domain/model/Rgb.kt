package com.github.iscle.echostrip.domain.model

/** A colour as the strip understands it: one byte per channel. */
data class Rgb(val red: Int, val green: Int, val blue: Int) {
    init {
        require(red in 0..255 && green in 0..255 && blue in 0..255) { "Channels must be 0..255" }
    }

    /** Packed as 0xRRGGBB. */
    val packed: Int get() = (red shl 16) or (green shl 8) or blue

    companion object {
        val White = Rgb(255, 255, 255)

        fun fromPacked(value: Int) = Rgb((value shr 16) and 0xFF, (value shr 8) and 0xFF, value and 0xFF)
    }
}
