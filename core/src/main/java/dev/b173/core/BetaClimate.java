package dev.b173.core;

import dev.b173.core.noise.SimplexOctaves;

public final class BetaClimate {
    private static final double TEMPERATURE_SCALE = (double) 0.025F;
    private static final double HUMIDITY_SCALE = (double) 0.05F;
    private static final double DETAIL_SCALE = 0.25D;
    private static final double TEMPERATURE_LACUNARITY = 0.25D;
    private static final double HUMIDITY_LACUNARITY = 1.0D / 3.0D;
    private static final double DETAIL_LACUNARITY = 1.0D / 1.7D;

    private final SimplexOctaves temperatureNoise;
    private final SimplexOctaves humidityNoise;
    private final SimplexOctaves detailNoise;

    public BetaClimate(long seed) {
        temperatureNoise = new SimplexOctaves(new BetaRandom(seed * 9871L), 4);
        humidityNoise = new SimplexOctaves(new BetaRandom(seed * 39811L), 4);
        detailNoise = new SimplexOctaves(new BetaRandom(seed * 543321L), 2);
    }

    public void sample(int x, int z, int sizeX, int sizeZ, double[] temperature, double[] humidity) {
        int n = sizeX * sizeZ;
        if (temperature.length < n || humidity.length < n) {
            throw new IllegalArgumentException("climate arrays must hold " + n + " values");
        }
        double[] detail = detailNoise.generate(null, x, z, sizeX, sizeZ, DETAIL_SCALE, DETAIL_SCALE, DETAIL_LACUNARITY);
        temperatureNoise.generate(temperature, x, z, sizeX, sizeZ, TEMPERATURE_SCALE, TEMPERATURE_SCALE, TEMPERATURE_LACUNARITY);
        humidityNoise.generate(humidity, x, z, sizeX, sizeZ, HUMIDITY_SCALE, HUMIDITY_SCALE, HUMIDITY_LACUNARITY);
        for (int i = 0; i < n; i++) {
            double d = detail[i] * 1.1D + 0.5D;
            double t = (temperature[i] * 0.15D + 0.7D) * (1.0D - 0.01D) + d * 0.01D;
            double h = (humidity[i] * 0.15D + 0.5D) * (1.0D - 0.002D) + d * 0.002D;
            t = 1.0D - (1.0D - t) * (1.0D - t);
            if (t < 0.0D) {
                t = 0.0D;
            }
            if (h < 0.0D) {
                h = 0.0D;
            }
            if (t > 1.0D) {
                t = 1.0D;
            }
            if (h > 1.0D) {
                h = 1.0D;
            }
            temperature[i] = t;
            humidity[i] = h;
        }
    }

    public double[] temperatures(double[] out, int x, int z, int sizeX, int sizeZ) {
        int n = sizeX * sizeZ;
        if (out == null || out.length < n) {
            out = new double[n];
        }
        double[] detail = detailNoise.generate(null, x, z, sizeX, sizeZ, DETAIL_SCALE, DETAIL_SCALE, DETAIL_LACUNARITY);
        temperatureNoise.generate(out, x, z, sizeX, sizeZ, TEMPERATURE_SCALE, TEMPERATURE_SCALE, TEMPERATURE_LACUNARITY);
        for (int i = 0; i < n; i++) {
            double d = detail[i] * 1.1D + 0.5D;
            double t = (out[i] * 0.15D + 0.7D) * (1.0D - 0.01D) + d * 0.01D;
            t = 1.0D - (1.0D - t) * (1.0D - t);
            if (t < 0.0D) {
                t = 0.0D;
            }
            if (t > 1.0D) {
                t = 1.0D;
            }
            out[i] = t;
        }
        return out;
    }

    public double skyTemperature(int x, int z) {
        return temperatureNoise.generate(new double[1], x, z, 1, 1, TEMPERATURE_SCALE, TEMPERATURE_SCALE, 0.5D)[0];
    }

    public static BetaBiome[] biomes(BetaBiome[] out, double[] temperature, double[] humidity, int count) {
        if (out == null || out.length < count) {
            out = new BetaBiome[count];
        }
        for (int i = 0; i < count; i++) {
            out[i] = BetaBiome.fromClimate(temperature[i], humidity[i]);
        }
        return out;
    }

    public Point at(int x, int z) {
        double[] t = new double[1];
        double[] h = new double[1];
        sample(x, z, 1, 1, t, h);
        return new Point(t[0], h[0]);
    }

    public BetaBiome biomeAt(int x, int z) {
        return at(x, z).biome();
    }

    public record Point(double temperature, double humidity) {
        public BetaBiome biome() {
            return BetaBiome.fromClimate(temperature, humidity);
        }
    }
}
