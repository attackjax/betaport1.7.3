package dev.b173.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public final class SmoothLightParityTest {
    private static final int[] IDS = {0, 0, 0, 0, 1, 2, 3, 4, 5, 12, 13, 17, 18, 20, 24, 35, 44, 45, 48, 49, 53,
            54, 58, 61, 79, 80, 82, 85, 87, 88, 89, 91};
    private static final int[] CENTRES = {1, 2, 3, 4, 5, 12, 13, 17, 18, 20, 24, 35, 45, 48, 49, 58, 79, 80, 82,
            87, 88, 89, 91};

    private SmoothLightParityTest() {
    }

    public static void run(TestRunner t) throws IOException {
        t.section("Smooth lighting against Beta's RenderBlocks");
        String record;
        long expected;
        try (InputStream in = SmoothLightParityTest.class.getResourceAsStream("/smooth-light-parity.txt")) {
            String[] lines = new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\n");
            record = lines[0].trim();
            expected = Long.parseUnsignedLong(lines[1].trim().substring("hash 0x".length()), 16);
        }
        float[] table = new float[16];
        for (int i = 0; i <= 15; i++) {
            float f = 1.0F - (float) i / 15.0F;
            table[i] = (1.0F - f) / (f * 3.0F + 1.0F) * (1.0F - 0.05F) + 0.05F;
        }
        Random random = new Random(2026);
        long hash = 0xcbf29ce484222325L;
        int samples = record.length() / 3;
        int faces = 0;
        for (int s = 0; s < samples; s++) {
            int[][][] ids = new int[5][5][5];
            int[][][] levels = new int[5][5][5];
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    for (int z = 0; z < 5; z++) {
                        ids[x][y][z] = IDS[random.nextInt(IDS.length)];
                        levels[x][y][z] = random.nextInt(16);
                    }
                }
            }
            int centre = CENTRES[random.nextInt(CENTRES.length)];
            ids[2][2][2] = centre;
            float r = random.nextBoolean() ? 1.0F : random.nextFloat();
            float g = random.nextBoolean() ? 1.0F : random.nextFloat();
            float b = random.nextBoolean() ? 1.0F : random.nextFloat();
            BetaSmoothLight.Access world = new BetaSmoothLight.Access() {
                private boolean in(int x, int y, int z) {
                    return x >= -2 && x <= 2 && y >= 62 && y <= 66 && z >= -2 && z <= 2;
                }

                public int id(int x, int y, int z) {
                    return in(x, y, z) ? ids[x + 2][y - 62][z + 2] : 0;
                }

                public float brightness(int x, int y, int z, int min) {
                    return table[Math.max(in(x, y, z) ? levels[x + 2][y - 62][z + 2] : 15, min)];
                }
            };
            int drawn = Integer.parseInt(record.substring(s * 3, s * 3 + 2), 16);
            boolean overlay = record.charAt(s * 3 + 2) == '1';
            for (int face = 0; face < 6; face++) {
                if ((drawn & 1 << face) == 0) {
                    continue;
                }
                faces++;
                float[] corners = BetaSmoothLight.face(world, BetaSmoothLight.GRASS, face, 0, 64, 0,
                        BetaSmoothLight.LIGHT_VALUE[centre]);
                boolean tinted = centre != BetaBlocks.GRASS || face == 1;
                for (float corner : corners) {
                    hash = mix(hash, BetaSmoothLight.colour(corner, face, tinted, r, g, b));
                }
                if (overlay && face >= 2) {
                    for (float corner : corners) {
                        hash = mix(hash, BetaSmoothLight.overlayColour(corner, face, r, g, b));
                    }
                }
            }
        }
        t.check(samples + " patches and " + faces + " faces give Beta's colour hash", hash == expected,
                String.format("0x%016x, Beta 0x%016x", hash, expected));
    }

    private static long mix(long hash, int colour) {
        for (int shift = 0; shift < 32; shift += 8) {
            hash = (hash ^ (colour >>> shift & 255)) * 0x100000001b3L;
        }
        return hash;
    }
}
