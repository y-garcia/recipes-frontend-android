package com.yeraygarcia.recipes.database;

import android.arch.persistence.room.TypeConverter;

import com.fasterxml.uuid.Generators;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class UUIDTypeConverter {

    @TypeConverter
    public static UUID toUUID(byte[] value) {
        return value == null ? null : convertBinaryToUUID(value);
    }

    @TypeConverter
    public static byte[] toByteArray(UUID value) {
        return value == null ? null : convertUUIDToBinary(value);
    }

    public static UUID newUUID() {
        return Generators.timeBasedGenerator().generate();
    }

    private static UUID convertBinaryToUUID(byte[] bytes) {
        ByteBuffer bb = reorder(bytes, false);
        long hi = bb.getLong();
        long low = bb.getLong();
        return new UUID(hi, low);
    }

    private static byte[] convertUUIDToBinary(UUID uuid) {
        byte[] bytes = new byte[16];

        // Turn into byte array
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        bb.rewind();
        bb.get(bytes);

        // Transform
        bb = reorder(bytes, true);

        // Turn into byte array
        bb.get(bytes);

        return bytes;
    }

    private static ByteBuffer reorder(byte[] bytes, boolean uuidToBin) {
        if (bytes.length != 16) {
            throw new RuntimeException("Invalid UUID bytes!");
        }

        // Get hi
        byte[] time_low;
        byte[] time_mid;
        byte[] time_hi;

        if (uuidToBin) {
            time_low = Arrays.copyOfRange(bytes, 0, 4);
            time_mid = Arrays.copyOfRange(bytes, 4, 6);
            time_hi = Arrays.copyOfRange(bytes, 6, 8); // actually time_hi + version
        } else {
            time_low = Arrays.copyOfRange(bytes, 0, 2); // actually time_hi + version
            time_mid = Arrays.copyOfRange(bytes, 2, 4);
            time_hi = Arrays.copyOfRange(bytes, 4, 8);
        }

        // Get low
        byte[] clock_seq = Arrays.copyOfRange(bytes, 8, 10); // actually variant + clock_seq
        byte[] node = Arrays.copyOfRange(bytes, 10, 16); // node

        // Rebuild
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.put(time_hi);
        bb.put(time_mid);
        bb.put(time_low);
        bb.put(clock_seq);
        bb.put(node);

        // Grab longs
        bb.rewind();
        return bb;
    }
}
