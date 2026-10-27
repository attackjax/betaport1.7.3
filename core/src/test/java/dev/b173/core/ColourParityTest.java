package dev.b173.core;

final class ColourParityTest {
    private ColourParityTest() {
    }

    record Case(long seed, int positions, long positionSeed, long checksum) {
    }

    private static final Case[] CASES = {
            new Case(0L, 10000, 1L, 0x14923f1cbfa18744L),
            new Case(3257840388504953787L, 10000, 2L, 0x15d4886fe3551430L),
            new Case(-987654321L, 10000, 3L, 0x8553451052b1de09L)
    };

    static void run(TestRunner t) {
        t.section("Block colours against the real Beta 1.7.3 client");
        for (Case c : CASES) {
            ClimateCache climate = new ClimateCache(new BetaClimate(c.seed()));
            BetaRandom positions = new BetaRandom(c.positionSeed());
            long hash = 0xcbf29ce484222325L;
            for (int i = 0; i < c.positions(); i++) {
                int x = positions.nextInt(4000000) - 2000000;
                int y = positions.nextInt(128);
                int z = positions.nextInt(4000000) - 2000000;
                int grass = BetaColors.colormapIndex(climate.temperature(x, z), climate.humidity(x, z));
                long sample = BetaColors.tallGrassSample(x, y, z);
                int sx = BetaColors.sampleX(sample);
                int sz = BetaColors.sampleZ(sample);
                int tallGrass = BetaColors.colormapIndex(climate.temperature(sx, sz), climate.humidity(sx, sz));
                hash = mix(mix(mix(hash, grass), grass), tallGrass);
            }
            t.check(String.format("seed %d: grass, oak leaves and tall grass at %d random positions", c.seed(), c.positions()),
                    hash == c.checksum(), String.format("checksum 0x%016x", hash));
        }
    }

    private static long mix(long hash, int value) {
        for (int shift = 0; shift < 32; shift += 8) {
            hash = (hash ^ (value >>> shift & 255)) * 0x100000001b3L;
        }
        return hash;
    }
}
