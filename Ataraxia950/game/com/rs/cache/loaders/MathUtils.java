package com.rs.cache.loaders;

public class MathUtils {
    public static int[] SIN = new int[16384];
    public static int[] COS = new int[16384];

    static {
        double d = 3.834951969714103E-4;
        for (int i = 0; i < 16384; i++) {
            SIN[i] = (int) (16384.0 * Math.sin((double) i * d));
            COS[i] = (int) (16384.0 * Math.cos((double) i * d));
        }
    }

    private MathUtils() {
        throw new Error();
    }

    public static float method7455(int i) {
        i &= 0x3fff;
        return (float) ((double) ((float) i / 16384.0F) * 6.283185307179586);
    }

    public static int method7456(int i, int i_0_) {
        return ((int) Math.round(Math.atan2(i, i_0_) * 2607.5945876176133) & 0x3fff);
    }

    public static int sin(int i) {
        return SIN[i & 0x3fff];
    }

    public static int cos(int i) {
        return COS[i & 0x3fff];
    }
}
