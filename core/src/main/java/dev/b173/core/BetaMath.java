package dev.b173.core;

public final class BetaMath {
    private static final float[] SIN_TABLE = new float[65536];

    static {
        for (int i = 0; i < 65536; i++) {
            SIN_TABLE[i] = (float) StrictMath.sin((double) i * 3.141592653589793D * 2.0D / 65536.0D);
        }
    }

    private BetaMath() {
    }

    public static float sin(float value) {
        return SIN_TABLE[(int) (value * 10430.378F) & 0xFFFF];
    }

    public static float cos(float value) {
        return SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 0xFFFF];
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }
}
