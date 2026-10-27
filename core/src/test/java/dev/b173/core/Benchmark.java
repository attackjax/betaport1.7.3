package dev.b173.core;

import dev.b173.core.populate.BetaPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Benchmark {
    private Benchmark() {
    }

    public static void main(String[] args) throws Exception {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 3257840388504953787L;
        BetaTerrain terrain = new BetaTerrain(seed);
        System.out.println("seed " + seed + ", " + Runtime.getRuntime().availableProcessors() + " cores, "
                + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));

        for (int i = 0; i < 400; i++) {
            terrain.generate(1000 + i % 20, 1000 + i / 20);
        }

        int side = 24;
        long start = System.nanoTime();
        for (int x = 0; x < side; x++) {
            for (int z = 0; z < side; z++) {
                terrain.generate(x, z);
            }
        }
        double single = (System.nanoTime() - start) / 1e6 / (side * side);
        System.out.printf("base terrain, 1 thread:   %.3f ms/chunk  (%.0f chunks/s)%n", single, 1000.0 / single);

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            int total = side * side * threads;
            start = System.nanoTime();
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                int offset = 2000 + t * 100;
                futures.add(pool.submit(() -> {
                    for (int x = 0; x < side; x++) {
                        for (int z = 0; z < side; z++) {
                            terrain.generate(offset + x, z);
                        }
                    }
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
            double wall = (System.nanoTime() - start) / 1e6;
            System.out.printf("base terrain, %d threads:  %.0f chunks/s on one shared, immutable generator%n",
                    threads, total / (wall / 1000.0));
        } finally {
            pool.shutdown();
        }

        BetaPopulator populator = new BetaPopulator(terrain);
        for (BetaWorld.Rules rules : new BetaWorld.Rules[]{BetaWorld.Rules.FAITHFUL, BetaWorld.Rules.ISOLATED}) {
            ChunkMapWorld world = new ChunkMapWorld(terrain, rules);
            int area = 24;
            for (int x = -1; x <= area; x++) {
                for (int z = -1; z <= area; z++) {
                    world.chunk(x, z);
                }
            }
            start = System.nanoTime();
            for (int x = 0; x < area; x++) {
                for (int z = 0; z < area; z++) {
                    populator.populate(world, x, z);
                }
            }
            double perChunk = (System.nanoTime() - start) / 1e6 / (area * area);
            System.out.printf("population, %-9s %.3f ms/chunk  (%d pending ticks over %d chunks)%n",
                    rules == BetaWorld.Rules.FAITHFUL ? "faithful:" : "isolated:", perChunk,
                    world.pendingTicks().size(), area * area);
        }

        BetaClimate climate = terrain.climate();
        double[] t = new double[256];
        double[] h = new double[256];
        start = System.nanoTime();
        int climateChunks = 4000;
        for (int i = 0; i < climateChunks; i++) {
            climate.sample(i * 16, 0, 16, 16, t, h);
        }
        double climateMicros = (System.nanoTime() - start) / 1e3 / climateChunks;
        System.out.printf("client climate, 16x16:    %.1f us/chunk column (computed once, then cached)%n", climateMicros);
    }
}
