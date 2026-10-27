package dev.b173.core;

import dev.b173.core.populate.BetaHellPopulator;
import dev.b173.core.populate.BetaPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class RegionSimulationTest {
    private RegionSimulationTest() {
    }

    private interface Populate {
        void run(BetaWorld world, int chunkX, int chunkZ);
    }

    public static void run(TestRunner t) {
        t.section("Modern storage model against unrestricted Beta storage (faithful rules)");
        for (long seed : new long[]{3257840388504953787L, 0L, -987654321L}) {
            BetaTerrain terrain = new BetaTerrain(seed);
            BetaPopulator populator = new BetaPopulator(terrain);
            simulate(t, "overworld", terrain, populator::populate);
        }
        for (long seed : new long[]{3257840388504953787L, 12345L, -4172144997902289642L}) {
            simulate(t, "Nether", new BetaHell(seed), BetaHellPopulator::populate);
        }
    }

    private static void simulate(TestRunner t, String name, BetaChunkSource terrain, Populate populator) {
        int side = 10;
        ChunkMapWorld reference = new ChunkMapWorld(terrain, BetaWorld.Rules.FAITHFUL);
        ChunkMapWorld backing = new ChunkMapWorld(terrain, BetaWorld.Rules.FAITHFUL);
        for (int x = -1; x <= side; x++) {
            for (int z = -1; z <= side; z++) {
                reference.chunk(x, z);
                backing.chunk(x, z);
            }
        }
        int dropped = 0;
        for (int x = 0; x < side; x++) {
            for (int z = 0; z < side; z++) {
                populator.run(reference, x, z);
                WindowWorld window = new WindowWorld(backing, terrain, x, z);
                populator.run(window, x, z);
                dropped += window.dropped;
            }
        }
        Map<String, Integer> kinds = new TreeMap<>();
        int differing = 0;
        int compared = 0;
        for (int x = 16; x < (side - 1) * 16; x++) {
            for (int z = 16; z < (side - 1) * 16; z++) {
                for (int y = 0; y < 128; y++) {
                    compared++;
                    int a = fold(reference.getBlockId(x, y, z));
                    int b = fold(backing.getBlockId(x, y, z));
                    if (a != b) {
                        differing++;
                        kinds.merge(BetaBlocks.name(a) + "->" + BetaBlocks.name(b), 1, Integer::sum);
                    }
                }
            }
        }
        double share = 100.0 * differing / compared;
        t.check(String.format("%s seed %d, %dx%d chunks: %d of %d blocks differ (%.4f%%), %d writes past the window",
                name, terrain.seed(), side, side, differing, compared, share, dropped), share < 0.01,
                kinds.toString());
    }

    private static int fold(int id) {
        return id == BetaBlocks.FLOWING_WATER ? BetaBlocks.WATER : id == BetaBlocks.FLOWING_LAVA ? BetaBlocks.LAVA : id;
    }

    private static final class WindowWorld extends BetaWorld {
        private final ChunkMapWorld backing;
        private final BetaChunkSource terrain;
        private final int centreX;
        private final int centreZ;
        private final Map<Long, Integer> overlay = new HashMap<>();
        private final Map<Long, BetaChunk> outside = new HashMap<>();
        int dropped;

        WindowWorld(ChunkMapWorld backing, BetaChunkSource terrain, int centreX, int centreZ) {
            super(Rules.FAITHFUL, terrain.seed() ^ ((long) centreX * 341873128712L + (long) centreZ * 132897987541L),
                    terrain instanceof BetaHell);
            keepLightForThisPassOnly();
            this.backing = backing;
            this.terrain = terrain;
            this.centreX = centreX;
            this.centreZ = centreZ;
        }

        @Override
        public boolean chunkExists(int chunkX, int chunkZ) {
            return Math.abs(chunkX - centreX) <= 1 && Math.abs(chunkZ - centreZ) <= 1;
        }

        @Override
        protected boolean deferTicksOutsideLoadedArea() {
            return true;
        }

        @Override
        protected boolean tickAreaLoaded(int x, int y, int z) {
            return chunkExists(x >> 4, z >> 4);
        }

        private int packed(int x, int y, int z) {
            Integer exact = overlay.get(pack(x, y, z));
            if (exact != null) {
                return exact;
            }
            if (chunkExists(x >> 4, z >> 4)) {
                int id = backing.getBlockId(x, y, z);
                int meta = backing.getBlockMetadata(x, y, z);
                if (BetaBlocks.isWater(id)) {
                    id = meta == 0 ? BetaBlocks.WATER : BetaBlocks.FLOWING_WATER;
                } else if (BetaBlocks.isLava(id)) {
                    id = meta == 0 ? BetaBlocks.LAVA : BetaBlocks.FLOWING_LAVA;
                }
                return id | meta << 8;
            }
            long key = (long) (x >> 4) << 32 ^ ((z >> 4) & 0xFFFFFFFFL);
            BetaChunk base = outside.computeIfAbsent(key, k -> terrain.generate(x >> 4, z >> 4));
            int index = BetaChunk.index(x & 15, y, z & 15);
            return (base.blocks[index] & 255) | base.metadata[index] << 8;
        }

        @Override
        protected int storageId(int x, int y, int z) {
            return packed(x, y, z) & 255;
        }

        @Override
        protected int storageMeta(int x, int y, int z) {
            return packed(x, y, z) >>> 8 & 15;
        }

        @Override
        protected void storageSet(int x, int y, int z, int id, int meta) {
            if (!chunkExists(x >> 4, z >> 4)) {
                dropped++;
                return;
            }
            overlay.put(pack(x, y, z), id | meta << 8);
            backing.chunk(x >> 4, z >> 4).set(x & 15, y, z & 15, id, meta);
        }

        @Override
        protected int storageHeight(int x, int z) {
            int y = 128;
            while (y > 0 && BetaBlocks.lightOpacity(storageId(x, y - 1, z)) == 0) {
                y--;
            }
            return y;
        }
    }
}
