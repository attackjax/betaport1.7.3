package dev.b173.core;

import java.util.Random;

final class SkyParityTest {
    private SkyParityTest() {
    }

    record Case(long seed, long checksum, long cloudChecksum) {
    }

    private static final Case[] CASES = {
            new Case(0L, 0xcb49589e10260a07L, 0x753f56b1864630b3L),
            new Case(3257840388504953787L, 0x827e273f85c195c2L, 0x4a13d7c8edb0958bL)
    };

    private static final long STAR_CHECKSUM = 0x9d95c0f176fa0fa6L;

    static void run(TestRunner t) {
        t.section("Sky, fog, sunrise, stars and clouds against the real Beta 1.7.3 client");
        for (Case c : CASES) {
            BetaClimate climate = new BetaClimate(c.seed());
            Random random = new Random(c.seed() * 31L + 7L);
            float[] sky = new float[3];
            float[] fog = new float[3];
            float[] glow = new float[4];
            long hash = 0xcbf29ce484222325L;
            for (int i = 0; i < 3000; i++) {
                long time = random.nextInt(48000);
                float pt = random.nextFloat();
                float[] weather = new float[4];
                for (int w = 0; w < 4; w++) {
                    weather[w] = random.nextInt(3) == 0 ? random.nextFloat() : 0.0F;
                }
                int flash = random.nextInt(4) == 0 ? random.nextInt(3) : 0;
                int x = random.nextInt(400000) - 200000;
                int z = random.nextInt(400000) - 200000;
                float rain = weather[0] + (weather[1] - weather[0]) * pt;
                float thunder = (weather[2] + (weather[3] - weather[2]) * pt) * rain;
                float angle = BetaSky.celestialAngle(time, pt);
                int rgb = BetaSky.skyColorByTemperature((float) climate.skyTemperature(x, z));
                BetaSky.skyColor(rgb, angle, rain, thunder, flash, pt, sky);
                BetaSky.fogColor(angle, fog);
                boolean rising = BetaSky.sunriseColor(angle, glow);
                for (float f : sky) {
                    hash = mix(hash, Float.floatToIntBits(f));
                }
                for (float f : fog) {
                    hash = mix(hash, Float.floatToIntBits(f));
                }
                hash = mix(hash, !rising ? -1 : Float.floatToIntBits(glow[0]) ^ Float.floatToIntBits(glow[1]) * 31
                        ^ Float.floatToIntBits(glow[2]) * 961 ^ Float.floatToIntBits(glow[3]) * 29791);
                hash = mix(hash, Float.floatToIntBits(BetaSky.starBrightness(angle)));
            }
            t.check(String.format("seed %d: 3000 skies through days, rain, thunder and lightning", c.seed()),
                    hash == c.checksum(), String.format("checksum 0x%016x", hash));
            float[] cloud = new float[3];
            long clouds = 0xcbf29ce484222325L;
            for (int i = 0; i < 3000; i++) {
                long time = random.nextInt(48000);
                float pt = random.nextFloat();
                float[] weather = new float[4];
                for (int w = 0; w < 4; w++) {
                    weather[w] = random.nextInt(3) == 0 ? random.nextFloat() : 0.0F;
                }
                float rain = weather[0] + (weather[1] - weather[0]) * pt;
                float thunder = (weather[2] + (weather[3] - weather[2]) * pt) * rain;
                BetaSky.cloudColor(BetaSky.celestialAngle(time, pt), rain, thunder, cloud);
                for (float f : cloud) {
                    clouds = mix(clouds, Float.floatToIntBits(f));
                }
            }
            t.check(String.format("seed %d: 3000 cloud colours through days, rain and thunder", c.seed()),
                    clouds == c.cloudChecksum(), String.format("checksum 0x%016x", clouds));
        }

        Random random = new Random(42L);
        int mismatches = 0;
        for (int i = 0; i < 200000; i++) {
            float h = random.nextFloat() * 4.0F - 2.0F;
            float s = random.nextInt(10) == 0 ? 0.0F : random.nextFloat();
            float b = random.nextFloat();
            if (BetaSky.hsbToRgb(h, s, b) != java.awt.Color.HSBtoRGB(h, s, b)) {
                mismatches++;
            }
        }
        t.check("HSB to RGB as java.awt.Color does it, 200000 colours", mismatches == 0, mismatches + " differ");

        float[] stars = BetaSky.stars();
        long starHash = 0xcbf29ce484222325L;
        for (float f : stars) {
            starHash = mix(starHash, Float.floatToIntBits(f));
        }
        t.check("780 stars, every corner where RenderGlobal put it", stars.length == 780 * 4 * 3
                && starHash == STAR_CHECKSUM, String.format("%d stars, checksum 0x%016x", stars.length / 12, starHash));
        float[] above = BetaSky.skyPlane(true);
        float[] below = BetaSky.skyPlane(false);
        t.check("sky planes of 13 by 13 squares, 16 blocks above and below",
                above.length == 169 * 12 && below.length == 169 * 12 && above[1] == 16.0F && below[1] == -16.0F
                        && above[0] == -384.0F && above[above.length - 3] == 384.0F && above[above.length - 1] == 448.0F,
                above.length / 12 + " squares");
    }

    private static long mix(long hash, int value) {
        for (int shift = 0; shift < 32; shift += 8) {
            hash = (hash ^ (value >>> shift & 255)) * 0x100000001b3L;
        }
        return hash;
    }
}
