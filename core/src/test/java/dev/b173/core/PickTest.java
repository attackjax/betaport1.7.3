package dev.b173.core;

public final class PickTest {
    private PickTest() {
    }

    public static void run(TestRunner t) {
        t.section("Beta's middle click");
        int[] inv = new int[36];
        inv[4] = 3;
        t.check("grass picks the dirt in hotbar slot 5", BetaPick.slot(inv, 2) == 4, String.valueOf(BetaPick.slot(inv, 2)));
        inv[4] = 0;
        inv[20] = 3;
        t.check("dirt only in the main inventory: nothing", BetaPick.slot(inv, 3) == -1, "");
        inv[2] = 44;
        t.check("a double slab picks a slab", BetaPick.slot(inv, 43) == 2, "");
        inv[7] = 1;
        t.check("bedrock (and a mob) picks stone", BetaPick.slot(inv, 7) == 7, "");
        inv[0] = 1;
        t.check("the first stack wins", BetaPick.slot(inv, 1) == 0, "");
        inv[8] = 324;
        t.check("a placed door (64) never finds the door item (324)", BetaPick.slot(inv, 64) == -1, "");
        t.check("air picks nothing", BetaPick.slot(new int[36], 0) == -1, "");
    }
}
