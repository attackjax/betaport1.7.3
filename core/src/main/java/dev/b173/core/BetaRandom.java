package dev.b173.core;

public final class BetaRandom {
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;
    private static final double DOUBLE_UNIT = 0x1.0p-53;

    private long seed;

    public BetaRandom(long seed) {
        setSeed(seed);
    }

    public void setSeed(long seed) {
        this.seed = (seed ^ MULTIPLIER) & MASK;
    }

    public int next(int bits) {
        seed = (seed * MULTIPLIER + ADDEND) & MASK;
        return (int) (seed >>> (48 - bits));
    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive: " + bound);
        }
        int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0) {
            return (int) ((bound * (long) r) >> 31);
        }
        for (int u = r; u - (r = u % bound) + m < 0; u = next(31)) {
        }
        return r;
    }

    public long nextLong() {
        return ((long) next(32) << 32) + next(32);
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public float nextFloat() {
        return next(24) / ((float) (1 << 24));
    }

    public double nextDouble() {
        return (((long) next(26) << 27) + next(27)) * DOUBLE_UNIT;
    }
}
