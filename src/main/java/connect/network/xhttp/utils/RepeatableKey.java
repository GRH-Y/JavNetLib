package connect.network.xhttp.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class RepeatableKey {
    private String key;

    public String getKey() {
        return key;
    }

    public RepeatableKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepeatableKey)) return false;
        RepeatableKey that = (RepeatableKey) o;
        Unsafe unsafe = getUnsafe();
        int offset = unsafe.arrayBaseOffset(getClass());
        int thatOffset = unsafe.arrayBaseOffset(that.getClass());
        return offset == thatOffset;
    }

    private Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
