package dev.b173.core;

import dev.b173.core.pack.BetaTextureFX;

import java.util.function.DoubleSupplier;

final class TextureFxParityTest {
    private TextureFxParityTest() {
    }

    record Case(String effect, long seed, int ticks, long checksum) {
    }

    private static final Case[] CASES = {
            new Case("lava_still", 12345L, 400, 0xfd9c031ae5503955L),
            new Case("lava_flow", 12345L, 400, 0x95b57a6ae655df8dL),
            new Case("water_still", 12345L, 400, 0x15f794e79a3f7570L),
            new Case("water_flow", 12345L, 400, 0xb13e3bd25ab50b91L),
            new Case("fire_0", 12345L, 400, 0xa24be3735e87eb96L),
            new Case("portal", 12345L, 400, 0x21167d18cec40016L),
            new Case("lava_still", -77L, 1000, 0xdf4f9b6d9c65e835L),
            new Case("lava_flow", -77L, 1000, 0x624d9cc812d9e585L),
            new Case("water_still", -77L, 1000, 0xace48412a5a548a7L),
            new Case("water_flow", -77L, 1000, 0xb09696d1d5bf5bdeL),
            new Case("fire_0", -77L, 1000, 0x07a9c902328bfb40L),
            new Case("portal", -77L, 1000, 0x013c4b2d7f66cffbL)
    };

    static void run(TestRunner t) {
        t.section("Animated textures and sky light against the real Beta 1.7.3 client");
        for (Case c : CASES) {
            DoubleSupplier random = BetaTextureFX.seeded(c.seed());
            BetaTextureFX.Effect effect = switch (c.effect()) {
                case "lava_still" -> new BetaTextureFX.Lava(false, random);
                case "lava_flow" -> new BetaTextureFX.Lava(true, random);
                case "water_still" -> new BetaTextureFX.Water(false, random);
                case "water_flow" -> new BetaTextureFX.Water(true, random);
                case "fire_0" -> new BetaTextureFX.Fire(random);
                default -> new BetaTextureFX.Portal();
            };
            long hash = 0xcbf29ce484222325L;
            for (int tick = 0; tick < c.ticks(); tick++) {
                effect.tick();
                for (byte b : effect.pixels) {
                    hash = (hash ^ (b & 255)) * 0x100000001b3L;
                }
            }
            t.check(String.format("%s: %d ticks from seed %d", c.effect(), c.ticks(), c.seed()),
                    hash == c.checksum(), String.format("checksum 0x%016x", hash));
        }

        long hash = 0xcbf29ce484222325L;
        for (long time = 0; time < 24000; time++) {
            hash = (hash ^ BetaSky.skyDarkening(time, 1.0F, 0.0F, 0.0F)) * 0x100000001b3L;
        }
        t.check("sky darkening for every tick of a day", hash == 0x7172631b57efeb66L,
                String.format("checksum 0x%016x", hash));
    }
}
