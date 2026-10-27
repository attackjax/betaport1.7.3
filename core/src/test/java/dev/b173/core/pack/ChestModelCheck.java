package dev.b173.core.pack;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaChest;
import dev.b173.core.TestRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class ChestModelCheck {
    private static final String[] SIDES = {"down", "up", "north", "south", "west", "east"};
    private static final Map<String, Integer> TILES = Map.of("top", 25, "side", 26, "front", 27,
            "front_a", 41, "front_b", 42, "back_a", 57, "back_b", 58);

    private ChestModelCheck() {
    }

    public static String variant(int[] look) {
        String front = SIDES[look[0]];
        if (look[1] < 0) {
            return "single_" + front;
        }
        return (look[1] == 2 || look[1] == 3 ? "z_" : "x_") + SIDES[look[1]] + "_" + front;
    }

    public static void run(TestRunner t) {
        Map<String, String[]> models = new HashMap<>();
        for (String[] variant : BetaBlockModels.chestVariants()) {
            models.put(variant[0], variant);
        }
        int[] palette = {0, 54, 1, 20, 18, 3, 50, 46, 44, 54};
        int wrong = 0;
        for (int n = 0; n < 4000; n++) {
            Random random = new Random(n);
            int[] patch = new int[25];
            for (int i = 0; i < 25; i++) {
                patch[i] = palette[random.nextInt(10)];
            }
            patch[12] = BetaBlocks.CHEST;
            BetaChest.Blocks world = (x, y, z) -> y != 0 || x < 0 || x > 4 || z < 0 || z > 4 ? 0 : patch[x * 5 + z];
            String[] model = models.get(variant(BetaChest.look(world, 2, 0, 2)));
            for (int side = 0; side < 6; side++) {
                if (model == null || TILES.get(model[side + 1]) != BetaChest.texture(world, 2, 0, 2, side)) {
                    wrong++;
                    break;
                }
            }
        }
        t.check("the pack's chest models show Beta's tiles for the look in each of 4,000 patches", wrong == 0,
                wrong + " wrong");
    }
}
