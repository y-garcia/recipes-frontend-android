package com.yeraygarcia.recipes.database

import android.arch.persistence.room.TypeConverter
import com.fasterxml.uuid.Generators
import java.nio.ByteBuffer
import java.util.*

object UUIDTypeConverter {

    @TypeConverter
    @JvmStatic
    fun toUUID(value: ByteArray?): UUID? {
        return value?.let { convertBinaryToUUID(it) }
    }

    @TypeConverter
    @JvmStatic
    fun toByteArray(value: UUID?): ByteArray? {
        return value?.let { convertUUIDToBinary(it) }
    }

    fun newUUID(): UUID {
        return Generators.timeBasedGenerator().generate()
    }

    private fun convertBinaryToUUID(bytes: ByteArray): UUID {
        val bb = reorder(bytes, false)
        val hi = bb.long
        val low = bb.long
        return UUID(hi, low)
    }

    private fun convertUUIDToBinary(uuid: UUID): ByteArray {
        val bytes = ByteArray(16)

        // Turn into byte array
        ByteBuffer.allocate(16).apply {
            putLong(uuid.mostSignificantBits)
            putLong(uuid.leastSignificantBits)
            rewind()
            get(bytes)
        }

        // Transform
        reorder(bytes, true).apply { get(bytes) }

        return bytes
    }

    private fun reorder(bytes: ByteArray, uuidToBin: Boolean): ByteBuffer {
        if (bytes.size != 16) {
            throw RuntimeException("Invalid UUID bytes!")
        }

        // Get hi
        val timeLow: ByteArray
        val timeMid: ByteArray
        val timeHigh: ByteArray

        if (uuidToBin) {
            timeLow = Arrays.copyOfRange(bytes, 0, 4)
            timeMid = Arrays.copyOfRange(bytes, 4, 6)
            timeHigh = Arrays.copyOfRange(bytes, 6, 8) // actually time_hi + version
        } else {
            timeLow = Arrays.copyOfRange(bytes, 0, 2) // actually time_hi + version
            timeMid = Arrays.copyOfRange(bytes, 2, 4)
            timeHigh = Arrays.copyOfRange(bytes, 4, 8)
        }

        // Get low
        val clockSeq = Arrays.copyOfRange(bytes, 8, 10) // actually variant + clock_seq
        val node = Arrays.copyOfRange(bytes, 10, 16) // node

        // Rebuild
        return ByteBuffer.allocate(16).apply {
            put(timeHigh)
            put(timeMid)
            put(timeLow)
            put(clockSeq)
            put(node)
            rewind()
        }
    }
}
