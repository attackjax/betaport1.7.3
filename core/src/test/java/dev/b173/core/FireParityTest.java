package dev.b173.core;

import java.nio.charset.StandardCharsets;

public final class FireParityTest {
    private static final long ORACLE_HASH = 0xe77a30ea6437135bL;

    private FireParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("Fire against Beta 1.7.3's BlockFire tables and burning materials (oracle hash)");
        StringBuilder out = new StringBuilder();
        for (int id = 0; id < 256; id++) {
            if (BetaFire.encourage(id) != 0 || BetaFire.ability(id) != 0) {
                out.append("fire ").append(id).append(' ').append(BetaFire.encourage(id)).append(' ')
                        .append(BetaFire.ability(id)).append('\n');
            }
        }
        out.append("burns");
        for (int id = 1; id < 256; id++) {
            if (BetaFire.materialBurns(id)) {
                out.append(' ').append(id);
            }
        }
        out.append('\n');
        long hash = 0xCBF29CE484222325L;
        for (byte b : out.toString().getBytes(StandardCharsets.UTF_8)) {
            hash = (hash ^ (b & 255)) * 0x100000001B3L;
        }
        t.check("encouragement, ability to catch and burning material for every block, as Beta's classes give them",
                hash == ORACLE_HASH, String.format("hash %016x", hash));
    }
}
