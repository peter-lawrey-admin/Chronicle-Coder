package net.openhft.chronicle.coder;

import sun.misc.Unsafe;

import static net.openhft.chronicle.coder.impl.UnsafeUtil.UNSAFE;

public interface Coder {
    default long parseLong(CharSequence cs) {
        return parseLong(cs, 0, cs.length());
    }

    long parseLong(CharSequence cs, int offset, int length);

    byte[] parseBytes(CharSequence cs);

    default long parseNative(CharSequence cs, long address, int maxLength) {
        byte[] bytes = parseBytes(cs);
        if (bytes.length > maxLength)
            throw new IllegalStateException("Input too long");
        UNSAFE.copyMemory(bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET, null, address, bytes.length);
        return address;
    }

    void appendLong(StringBuilder sb, long value);

    void appendBytes(StringBuilder sb, byte[] bytes);

    default String asString(long value) {
        StringBuilder sb = new StringBuilder();
        appendLong(sb, value);
        return sb.toString();
    }

    default void appendNative(StringBuilder sb, long address, int length) {
        byte[] bytes = new byte[length];
        UNSAFE.copyMemory(null, address, bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET, length);
        appendBytes(sb, bytes);
    }
}
