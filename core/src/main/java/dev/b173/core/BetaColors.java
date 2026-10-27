package dev.b173.core;

public final class BetaColors {
    private BetaColors() {
    }

    public static int colormapIndex(double temperature, double humidity) {
        humidity *= temperature;
        int x = (int) ((1.0D - temperature) * 255.0D);
        int y = (int) ((1.0D - humidity) * 255.0D);
        return y << 8 | x;
    }

    public static long tallGrassSample(int x, int y, int z) {
        long hash = x * 3129871 + z * 6129781 + y;
        hash = hash * hash * 42317861L + hash * 11L;
        int sampleX = (int) ((long) x + (hash >> 14 & 31L));
        int sampleZ = (int) ((long) z + (hash >> 24 & 31L));
        return (long) sampleX << 32 | (sampleZ & 0xFFFFFFFFL);
    }

    public static int sampleX(long sample) {
        return (int) (sample >> 32);
    }

    public static int sampleZ(long sample) {
        return (int) sample;
    }
}
