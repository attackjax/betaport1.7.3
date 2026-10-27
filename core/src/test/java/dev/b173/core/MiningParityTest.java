package dev.b173.core;

import java.nio.charset.StandardCharsets;

public final class MiningParityTest {
    private static final long ORACLE_HASH = 0x1f80fc0b1f36aff9L;
    private static final int[] TOOLS = {256, 257, 258, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279,
            283, 284, 285, 286, 290, 291, 292, 293, 294, 359};

    private MiningParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("Mining against Beta 1.7.3's tools and hardness (oracle hash)");
        StringBuilder out = new StringBuilder();
        for (int tool : TOOLS) {
            for (int id = 1; id <= 96; id++) {
                float s = BetaMining.strength(tool, id);
                if (s != 1.0F) {
                    out.append(tool).append(' ').append(id).append(' ').append(s).append('\n');
                }
            }
        }
        for (int id = 1; id <= 96; id++) {
            out.append("hardness ").append(id).append(' ').append(BetaMining.hardness(id)).append('\n');
        }
        long hash = 0xCBF29CE484222325L;
        for (byte b : out.toString().getBytes(StandardCharsets.UTF_8)) {
            hash = (hash ^ (b & 255)) * 0x100000001B3L;
        }
        t.check("26 tools against 96 blocks, and every hardness, as Beta's classes give them", hash == ORACLE_HASH,
                String.format("hash %016x", hash));
    }
}
