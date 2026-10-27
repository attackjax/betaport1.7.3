package dev.b173.core.noise;

import dev.b173.core.BetaRandom;

public final class SimplexOctaves {
    private final SimplexNoise[] octaves;

    public SimplexOctaves(BetaRandom random, int count) {
        octaves = new SimplexNoise[count];
        for (int i = 0; i < count; i++) {
            octaves[i] = new SimplexNoise(random);
        }
    }

    public double[] generate(double[] out, double x, double z, int sizeX, int sizeZ,
                             double scaleX, double scaleZ, double lacunarity) {
        return generate(out, x, z, sizeX, sizeZ, scaleX, scaleZ, lacunarity, 0.5D);
    }

    public double[] generate(double[] out, double x, double z, int sizeX, int sizeZ,
                             double scaleX, double scaleZ, double lacunarity, double persistence) {
        scaleX /= 1.5D;
        scaleZ /= 1.5D;
        if (out == null || out.length < sizeX * sizeZ) {
            out = new double[sizeX * sizeZ];
        } else {
            java.util.Arrays.fill(out, 0.0D);
        }
        double amplitudeDivisor = 1.0D;
        double frequency = 1.0D;
        for (SimplexNoise octave : octaves) {
            octave.add(out, x, z, sizeX, sizeZ, scaleX * frequency, scaleZ * frequency, 0.55D / amplitudeDivisor);
            frequency *= lacunarity;
            amplitudeDivisor *= persistence;
        }
        return out;
    }
}
