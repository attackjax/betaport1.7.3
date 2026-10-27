package dev.b173.core;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public final class BlockDropParityTest {
    private static final long ORACLE_HASH = 0x426d79879247326bL;
    private static final int TRIES = 200000;

    private BlockDropParityTest() {
    }

    public static void run(TestRunner t) {
        t.section("Block drops against Beta 1.7.3's blocks (oracle hash)");
        long hash = 0xCBF29CE484222325L;
        int drops = 0;
        int harvests = 0;
        for (String line : BetaBlockDrops.LINES) {
            for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) {
                hash = (hash ^ (b & 255)) * 0x100000001B3L;
            }
            drops += line.startsWith("drop ") ? 1 : 0;
            harvests += line.startsWith("harvest ") ? 1 : 0;
        }
        t.check("231 lines as Beta listed them (135 drops, 96 harvests)",
                hash == ORACLE_HASH && drops == 135 && harvests == 96,
                String.format("hash %016x, %d drops, %d harvests", hash, drops, harvests));

        for (String rule : BetaBlockDrops.RULES) {
            int block = Integer.parseInt(rule.split(" ")[0]);
            BetaBlockDrops.Rule parsed = BetaBlockDrops.rule(block);
            List<String> lines = BetaBlockDrops.drops(block);
            boolean ok = !lines.isEmpty();
            StringBuilder detail = new StringBuilder();
            for (String line : lines) {
                String[] parts = line.split(" ");
                int meta = Integer.parseInt(parts[1].substring(parts[1].indexOf(':') + 1));
                String expected = simulate(parsed, block, meta);
                if (!matches(expected, line)) {
                    ok = false;
                    detail.append(line).append(" vs ").append(expected).append("; ");
                }
            }
            t.check("rule \"" + rule + "\" gives Beta's drop", ok, detail.toString());
        }

        boolean crops = true;
        for (int age = 0; age < 8; age++) {
            Random random = new Random(age);
            int seeds = 0;
            int wheat = 0;
            for (int i = 0; i < TRIES; i++) {
                int[] got = BetaBlockDrops.crops(age, random::nextInt);
                wheat += got[0];
                seeds += got[1];
            }
            double share = seeds / (double) TRIES;
            double expected = 3.0D * (age + 1) / 15.0D;
            crops &= Math.abs(share - expected) < 0.01D && wheat == (age == 7 ? TRIES : 0);
        }
        t.check("crops: wheat at age 7, three tries at a seed of (age + 1) in 15", crops);

        boolean pickaxes = true;
        StringBuilder which = new StringBuilder();
        for (int block : BetaBlockDrops.PICKAXE_ONLY) {
            String tools = BetaBlockDrops.harvest(block);
            pickaxes &= BetaBlockDrops.PICKAXES.equals(tools);
            which.append(block).append(": ").append(tools).append("; ");
        }
        t.check("slabs, stone plate, iron door and glowstone drop for a pickaxe alone", pickaxes, which.toString());
        t.check("leaves, tall grass and dead bush drop for a bare hand, cobweb for a sword or shears",
                BetaBlockDrops.harvest(18).startsWith("0 ") && BetaBlockDrops.harvest(31).startsWith("0 ")
                        && BetaBlockDrops.harvest(32).startsWith("0 ")
                        && BetaBlockDrops.harvest(30).equals("268 272 267 283 276 359"),
                "web: " + BetaBlockDrops.harvest(30));
    }

    private static String simulate(BetaBlockDrops.Rule rule, int block, int meta) {
        if (rule.crops()) {
            return meta == 7 ? "1-1 296:0=1.000" : "0-0";
        }
        if (rule.none()) {
            return "0-0";
        }
        Random random = new Random(block * 31L + meta);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        long total = 0;
        for (int i = 0; i < TRIES; i++) {
            int n = rule.roll(random::nextInt);
            min = Math.min(min, n);
            max = Math.max(max, n);
            total += n;
        }
        String item = rule.self() ? block + ":0" : rule.item() + ":" + (rule.damage() < 0 ? meta : rule.damage());
        return min + "-" + max + " " + item + "=" + String.format("%.3f", total / (double) TRIES);
    }

    private static boolean matches(String expected, String line) {
        String[] want = expected.split(" ");
        String[] got = line.split(" ");
        if (!want[0].equals(got[2]) || want.length != got.length - 2) {
            return false;
        }
        for (int i = 1; i < want.length; i++) {
            String[] w = want[i].split("=");
            String[] g = got[i + 2].split("=");
            if (!w[0].equals(g[0]) || Math.abs(Double.parseDouble(w[1]) - Double.parseDouble(g[1])) > 0.006D
                    * Math.max(1.0D, Double.parseDouble(w[1]))) {
                return false;
            }
        }
        return true;
    }
}
