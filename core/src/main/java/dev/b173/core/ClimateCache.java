package dev.b173.core;

import java.util.concurrent.ConcurrentHashMap;

public final class ClimateCache {
    private static final int MAX_CHUNKS = 4096;

    private final BetaClimate climate;
    private final ConcurrentHashMap<Long, double[]> chunks = new ConcurrentHashMap<>();

    public ClimateCache(BetaClimate climate) {
        this.climate = climate;
    }

    private double[] chunk(int chunkX, int chunkZ) {
        long key = (long) chunkX << 32 ^ (chunkZ & 0xFFFFFFFFL);
        double[] data = chunks.get(key);
        if (data == null) {
            double[] temperature = new double[256];
            double[] humidity = new double[256];
            climate.sample(chunkX << 4, chunkZ << 4, 16, 16, temperature, humidity);
            data = new double[512];
            System.arraycopy(temperature, 0, data, 0, 256);
            System.arraycopy(humidity, 0, data, 256, 256);
            if (chunks.size() >= MAX_CHUNKS) {
                chunks.clear();
            }
            chunks.put(key, data);
        }
        return data;
    }

    public double temperature(int x, int z) {
        return chunk(x >> 4, z >> 4)[(x & 15) << 4 | z & 15];
    }

    public double humidity(int x, int z) {
        return chunk(x >> 4, z >> 4)[256 + ((x & 15) << 4 | z & 15)];
    }

    public double skyTemperature(int x, int z) {
        return climate.skyTemperature(x, z);
    }
}
