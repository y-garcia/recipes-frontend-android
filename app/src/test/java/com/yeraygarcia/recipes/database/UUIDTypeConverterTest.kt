package com.yeraygarcia.recipes.database

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class UUIDTypeConverterTest {

    @Test
    fun toUUID() {
        val bin = "11e8a0727f9307e09ac40a0027000012".hexToByteArray()
        val uuid = UUIDTypeConverter.toUUID(bin)
        assertEquals("7f9307e0-a072-11e8-9ac4-0a0027000012", uuid.toString())
    }

    @Test
    fun toByteArray() {
        val uuid = UUID.fromString("7f9307e0-a072-11e8-9ac4-0a0027000012")
        val bytes = UUIDTypeConverter.toByteArray(uuid)
        val hex = bytes.toHex()

        assertEquals("11e8", hex.substring(0, 4))
        assertEquals("a072", hex.substring(4, 8))
        assertEquals("7f9307e0", hex.substring(8, 16))
        assertEquals("9ac40a0027000012", hex.substring(16, 32))
    }

    @Test
    fun newUUID() {
        val uuid = UUIDTypeConverter.newUUID()
        assertEquals(1, uuid.version())
    }

    fun ByteArray.toHex(): String {
        val HEX_CHARS = "0123456789abcdef".toCharArray()
        val result = StringBuffer()

        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }

        return result.toString()
    }

    fun String.hexToByteArray(): ByteArray {
        val HEX_CHARS = "0123456789abcdef"
        val result = ByteArray(length / 2)

        for (i in 0 until length step 2) {
            val firstIndex = HEX_CHARS.indexOf(this[i])
            val secondIndex = HEX_CHARS.indexOf(this[i + 1])

            val octet = firstIndex.shl(4).or(secondIndex)
            result.set(i.shr(1), octet.toByte())
        }

        return result
    }
}