package dev.b173.core;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public final class ChestParityTest {
    private static final long ORACLE_HASH = 0x6dad67d9a4c7bda0L;
    private static final int[] PALETTE = {0, 54, 1, 20, 18, 3, 50, 46, 44, 54};
    private static final int CASES = 4000;

    private ChestParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("Chests against Beta 1.7.3's BlockChest (oracle hash)");
        long hash = 0xCBF29CE484222325L;
        int doubles = 0;
        for (int n = 0; n < CASES; n++) {
            Random random = new Random(n);
            int[] patch = new int[25];
            for (int i = 0; i < 25; i++) {
                patch[i] = PALETTE[random.nextInt(10)];
            }
            patch[2 * 5 + 2] = BetaBlocks.CHEST;
            BetaChest.Blocks world = (x, y, z) -> y != 0 || x < 0 || x > 4 || z < 0 || z > 4 ? 0 : patch[x * 5 + z];
            StringBuilder line = new StringBuilder(Integer.toString(n));
            for (int side = 0; side < 6; side++) {
                line.append(' ').append(BetaChest.texture(world, 2, 0, 2, side));
            }
            doubles += BetaChest.look(world, 2, 0, 2)[1] >= 0 ? 1 : 0;
            for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) {
                hash = (hash ^ (b & 255)) * 0x100000001B3L;
            }
        }
        t.check("4,000 patches, every face as Beta drew it", hash == ORACLE_HASH,
                String.format("hash %016x, %d double chests", hash, doubles));

        int[][] row = {{0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}, {0, 54, 54, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 0, 0, 0}};
        BetaChest.Blocks pair = (x, y, z) -> y != 0 || x < 0 || x > 4 || z < 0 || z > 4 ? 0 : row[x][z];
        boolean third = !BetaChest.canPlace(pair, 2, 0, 3) && !BetaChest.canPlace(pair, 1, 0, 1)
                && !BetaChest.canPlace(pair, 2, 0, 0) && BetaChest.canPlace(pair, 4, 0, 2);
        BetaChest.Blocks alone = (x, y, z) -> 0;
        t.check("no third chest beside a double one, nor between two", third && BetaChest.canPlace(alone, 2, 0, 2));
    }
}
