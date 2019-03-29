package net.openhft.chronicle.coder.impl;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {
    public static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (@NotNull NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }
}
