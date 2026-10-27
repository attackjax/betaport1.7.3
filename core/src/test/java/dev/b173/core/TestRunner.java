package dev.b173.core;

import java.util.ArrayList;
import java.util.List;

public final class TestRunner {
    private int passed;
    private final List<String> failures = new ArrayList<>();

    public void section(String name) {
        System.out.println();
        System.out.println("== " + name);
    }

    public void check(String name, boolean ok) {
        check(name, ok, "");
    }

    public void check(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            System.out.println("  pass  " + name);
        } else {
            failures.add(name + (detail.isEmpty() ? "" : " (" + detail + ")"));
            System.out.println("  FAIL  " + name + (detail.isEmpty() ? "" : "  " + detail));
        }
    }

    public static void main(String[] args) {
        TestRunner t = new TestRunner();
        String only = args.length > 0 ? args[0] : "";
        if (only.isEmpty() || only.equals("terrain")) {
            TerrainParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("population")) {
            PopulationParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("oracle")) {
            BetaOracleVectors.run(t);
        }
        if (only.isEmpty() || only.equals("colours")) {
            ColourParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("recipes")) {
            RecipeParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("mining")) {
            MiningParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("fire")) {
            FireParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("pick")) {
            PickTest.run(t);
        }
        if (only.isEmpty() || only.equals("paths")) {
            PathParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("smooth")) {
            try {
                SmoothLightParityTest.run(t);
            } catch (java.io.IOException e) {
                t.check("smooth light record could be read", false, e.toString());
            }
        }
        if (only.isEmpty() || only.equals("locator")) {
            try {
                LocatorTest.run(t);
            } catch (java.io.IOException e) {
                t.check("locator test folders could be made", false, e.toString());
            }
        }
        if (only.isEmpty() || only.equals("drops")) {
            BlockDropParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("content")) {
            ContentParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("chests")) {
            ChestParityTest.run(t);
            dev.b173.core.pack.ChestModelCheck.run(t);
        }
        if (only.isEmpty() || only.equals("nether")) {
            NetherParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("region")) {
            RegionSimulationTest.run(t);
        }
        if (only.isEmpty() || only.equals("textures")) {
            TextureFxParityTest.run(t);
        }
        if (only.isEmpty() || only.equals("sky")) {
            SkyParityTest.run(t);
        }
        System.out.println();
        System.out.println(t.passed + " passed, " + t.failures.size() + " failed");
        for (String failure : t.failures) {
            System.out.println("  failed: " + failure);
        }
        if (!t.failures.isEmpty()) {
            System.exit(1);
        }
    }
}
