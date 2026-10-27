package dev.b173.core.noise;

import dev.b173.core.BetaRandom;

public final class PerlinOctaves {
    private final ImprovedNoise[] octaves;

    public PerlinOctaves(BetaRandom random, int count) {
        octaves = new ImprovedNoise[count];
        for (int i = 0; i < count; i++) {
            octaves[i] = new ImprovedNoise(random);
        }
    }

    public double[] generate(double[] out, double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                             double scaleX, double scaleY, double scaleZ) {
        if (out == null) {
            out = new double[sizeX * sizeY * sizeZ];
        } else {
            java.util.Arrays.fill(out, 0.0D);
        }
        double frequency = 1.0D;
        for (ImprovedNoise octave : octaves) {
            octave.add(out, x, y, z, sizeX, sizeY, sizeZ,
                    scaleX * frequency, scaleY * frequency, scaleZ * frequency, frequency);
            frequency /= 2.0D;
        }
        return out;
    }

    public double[] generate2d(double[] out, int x, int z, int sizeX, int sizeZ, double scaleX, double scaleZ) {
        return generate(out, x, 10.0D, z, sizeX, 1, sizeZ, scaleX, 1.0D, scaleZ);
    }

    public double samplePoint(double x, double z) {
        double total = 0.0D;
        double frequency = 1.0D;
        for (ImprovedNoise octave : octaves) {
            total += octave.sample(x * frequency, z * frequency, 0.0D) / frequency;
            frequency /= 2.0D;
        }
        return total;
    }
}
