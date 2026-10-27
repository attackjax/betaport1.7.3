package dev.b173.core;

import dev.b173.core.populate.BetaFeatures;
import dev.b173.core.populate.BetaPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PopulationParityTest {
    private PopulationParityTest() {
    }

    private static final long[][] LAYOUT = {
            {0L, 0, 0, -9223164016657546891L},
            {0L, -1, -1, 9045326659797964013L},
            {0L, 20, 20, 1594360910335451069L},
            {1L, 0, 0, -5893143798136972391L},
            {1L, -32, 47, -7460256401104540188L},
            {-1L, 0, 0, 2993749327647676766L},
            {-1L, 511, -512, 6153242539256998833L},
            {12345L, 6, -13, -779556861285068605L},
            {12345L, 0, 0, -9113189411121354649L},
            {12345L, -100, 200, 1099759742527704851L},
            {-987654321L, -2, -3, 3070113346521495973L},
            {-987654321L, 2048, -4096, -5813430372215992477L},
            {Long.MAX_VALUE, 31, -47, 2114551146370329918L},
            {Long.MIN_VALUE, -31, 47, 216144121216348350L},
            {25214903917L, 300000, -300000, -2560949406261895663L},
            {8675309L, -1875, 2047, 404390262206430293L}
    };

    private static final long[][] STATE = {
            {0L, 0, 0, -9000757019170660491L},
            {0L, -1, -1, -2078204544850883603L},
            {0L, 20, 20, 1044638297484972477L},
            {1L, 0, 0, -5893143798136972391L},
            {1L, -32, 47, -6481225673412749340L},
            {-1L, 0, 0, -1429236846237003170L},
            {-1L, 511, -512, 7456808733142169521L},
            {12345L, 6, -13, 6423983609770610883L},
            {12345L, 0, 0, -9113189411121354649L},
            {12345L, -100, 200, 1099759742527704851L},
            {-987654321L, -2, -3, 3070113346521495973L},
            {-987654321L, 2048, -4096, -5813430372215992477L},
            {Long.MAX_VALUE, 31, -47, -2800477819005935554L},
            {Long.MIN_VALUE, -31, 47, 216144121216348350L},
            {25214903917L, 300000, -300000, -2560949406261895663L},
            {8675309L, -1875, 2047, -4493698468096567723L}
    };

    private static final long[][] OVERLAP = {
            {0L, 0, 0, -1895314890128223292L},
            {1L, -3, 5, 5897167218552055424L},
            {12345L, 6, -13, -1554863880759963355L},
            {-987654321L, 20, 20, 3078300680593495511L},
            {Long.MAX_VALUE, -31, 47, -3825940143351333059L},
            {Long.MIN_VALUE, 31, -47, 1191232275031452913L}
    };

    private static final long[][] DUNGEON = {
            {0L, 7781858791577028290L},
            {1L, -5023770165449294038L},
            {2L, 3946388126032264550L},
            {3L, 671547234450091382L},
            {4L, 4304187789777917271L},
            {5L, 4508025769618396271L},
            {42L, 7177099885647937943L},
            {12345L, 662173237184898008L},
            {-1L, 8209948158660952470L},
            {Long.MAX_VALUE, 8209948158660952470L},
            {Long.MIN_VALUE, 7781858791577028290L}
    };

    private static final List<String> LOOT = List.of("minecraft:saddle", "minecraft:iron_ingot", "minecraft:bread",
            "minecraft:wheat", "minecraft:gunpowder", "minecraft:string", "minecraft:bucket",
            "minecraft:golden_apple", "minecraft:redstone", "minecraft:music_disc_13", "minecraft:music_disc_cat",
            "minecraft:cocoa_beans");
    private static final List<String> MOBS = List.of("minecraft:skeleton", "minecraft:zombie", "minecraft:spider");

    public static void run(TestRunner t) {
        t.section("Dungeon carving, loot and spawner");
        for (long[] v : DUNGEON) {
            long hash = dungeonHash(v[0]);
            t.check("random seed " + v[0], hash == v[1], "got " + hash);
        }

        t.section("Population block layout (isolated rules)");
        for (long[] v : LAYOUT) {
            ChunkMapWorld world = populated(v[0], (int) v[1], (int) v[2], BetaWorld.Rules.ISOLATED);
            long hash = layoutHash(world, (int) v[1], (int) v[2]);
            t.check("seed " + v[0] + " chunk " + v[1] + "," + v[2], hash == v[3], "got " + hash);
        }

        t.section("Population metadata, chests and spawners (isolated rules)");
        for (long[] v : STATE) {
            ChunkMapWorld world = populated(v[0], (int) v[1], (int) v[2], BetaWorld.Rules.ISOLATED);
            long hash = stateHash(world, (int) v[1], (int) v[2], 2);
            t.check("seed " + v[0] + " chunk " + v[1] + "," + v[2], hash == v[3], "got " + hash);
        }

        t.section("Four overlapping population passes (isolated rules)");
        for (long[] v : OVERLAP) {
            long hash = overlapHash(v[0], (int) v[1], (int) v[2], BetaWorld.Rules.ISOLATED);
            t.check("seed " + v[0] + " chunks from " + v[1] + "," + v[2], hash == v[3], "got " + hash);
        }

        t.section("Faithful rules versus the isolated vectors (informational)");
        int same = 0;
        for (long[] v : LAYOUT) {
            ChunkMapWorld world = populated(v[0], (int) v[1], (int) v[2], BetaWorld.Rules.FAITHFUL);
            if (layoutHash(world, (int) v[1], (int) v[2]) == v[3]) {
                same++;
            }
        }
        System.out.println("  info  " + same + " of " + LAYOUT.length
                + " layouts are unchanged once Beta's neighbour updates, instant fluid ticks and stored light run");
    }

    static ChunkMapWorld populated(long seed, int chunkX, int chunkZ, BetaWorld.Rules rules) {
        BetaTerrain terrain = new BetaTerrain(seed);
        ChunkMapWorld world = new ChunkMapWorld(terrain, rules);
        for (int x = chunkX - 1; x <= chunkX + 2; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 2; z++) {
                world.chunk(x, z);
            }
        }
        new BetaPopulator(terrain).populate(world, chunkX, chunkZ);
        return world;
    }

    static long overlapHash(long seed, int chunkX, int chunkZ, BetaWorld.Rules rules) {
        BetaTerrain terrain = new BetaTerrain(seed);
        ChunkMapWorld world = new ChunkMapWorld(terrain, rules);
        for (int x = chunkX - 1; x <= chunkX + 3; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 3; z++) {
                world.chunk(x, z);
            }
        }
        BetaPopulator populator = new BetaPopulator(terrain);
        populator.populate(world, chunkX, chunkZ);
        populator.populate(world, chunkX, chunkZ + 1);
        populator.populate(world, chunkX + 1, chunkZ);
        populator.populate(world, chunkX + 1, chunkZ + 1);
        return stateHash(world, chunkX, chunkZ, 3);
    }

    private static int foldedId(int id) {
        return switch (id) {
            case BetaBlocks.FLOWING_WATER -> BetaBlocks.WATER;
            case BetaBlocks.LAVA -> BetaBlocks.FLOWING_LAVA;
            default -> id;
        };
    }

    static long layoutHash(ChunkMapWorld world, int chunkX, int chunkZ) {
        long hash = 0xcbf29ce484222325L;
        for (int x = chunkX; x <= chunkX + 1; x++) {
            for (int z = chunkZ; z <= chunkZ + 1; z++) {
                for (byte b : world.chunk(x, z).blocks) {
                    hash ^= foldedId(b & 255);
                    hash *= 0x100000001b3L;
                }
            }
        }
        return hash;
    }

    static long stateHash(ChunkMapWorld world, int chunkX, int chunkZ, int width) {
        Map<Long, String[]> chestNames = new HashMap<>();
        Map<Long, int[]> chestCounts = new HashMap<>();
        for (Map.Entry<Long, List<BetaWorld.ChestItem>> e : world.chests().entrySet()) {
            String[] names = new String[27];
            int[] counts = new int[27];
            for (BetaWorld.ChestItem item : e.getValue()) {
                names[item.slot()] = item.item();
                counts[item.slot()] = item.count();
            }
            chestNames.put(e.getKey(), names);
            chestCounts.put(e.getKey(), counts);
        }
        long hash = 0xcbf29ce484222325L;
        for (int cx = chunkX; cx < chunkX + width; cx++) {
            for (int cz = chunkZ; cz < chunkZ + width; cz++) {
                BetaChunk chunk = world.chunk(cx, cz);
                for (int index = 0; index < BetaChunk.VOLUME; index++) {
                    hash ^= foldedId(chunk.blocks[index] & 255) | (chunk.metadata[index] & 15) << 8;
                    hash *= 0x100000001b3L;
                    int lx = index / 2048;
                    int lz = index % 2048 / 128;
                    int y = index % 128;
                    long key = BetaWorld.pack(cx * 16 + lx, y, cz * 16 + lz);
                    String[] names = chestNames.get(key);
                    if (names != null) {
                        int[] counts = chestCounts.get(key);
                        for (int slot = 0; slot < 27; slot++) {
                            hash ^= names[slot] == null ? 0 : LOOT.indexOf(names[slot]) + 1;
                            hash *= 0x100000001b3L;
                            hash ^= names[slot] == null ? 0 : counts[slot];
                            hash *= 0x100000001b3L;
                        }
                    }
                    String mob = world.spawners().get(key);
                    if (mob != null) {
                        hash ^= MOBS.indexOf(mob) + 1;
                        hash *= 0x100000001b3L;
                    }
                }
            }
        }
        return hash;
    }

    private static long dungeonHash(long randomSeed) {
        MapWorld world = new MapWorld();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = 49; y <= 54; y++) {
                    world.put(x, y, z, BetaBlocks.STONE);
                }
            }
        }
        for (int x = -4; x <= -3; x++) {
            for (int y = 50; y <= 51; y++) {
                world.put(x, y, 0, BetaBlocks.AIR);
            }
        }
        BetaFeatures.dungeon(world, new BetaRandom(randomSeed), 0, 50, 0);
        long hash = 0xcbf29ce484222325L;
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = 49; y <= 54; y++) {
                    hash ^= foldedId(world.getBlockId(x, y, z)) | world.getBlockMetadata(x, y, z) << 8;
                    hash *= 0x100000001b3L;
                    long key = BetaWorld.pack(x, y, z);
                    List<BetaWorld.ChestItem> items = world.chests().get(key);
                    if (items != null) {
                        String[] names = new String[27];
                        int[] counts = new int[27];
                        for (BetaWorld.ChestItem item : items) {
                            names[item.slot()] = item.item();
                            counts[item.slot()] = item.count();
                        }
                        for (int slot = 0; slot < 27; slot++) {
                            hash ^= names[slot] == null ? 0 : LOOT.indexOf(names[slot]) + 1;
                            hash *= 0x100000001b3L;
                            hash ^= names[slot] == null ? 0 : counts[slot];
                            hash *= 0x100000001b3L;
                        }
                    }
                    String mob = world.spawners().get(key);
                    if (mob != null) {
                        hash ^= MOBS.indexOf(mob) + 1;
                        hash *= 0x100000001b3L;
                    }
                }
            }
        }
        return hash;
    }

    private static final class MapWorld extends BetaWorld {
        private final Map<Long, int[]> blocks = new HashMap<>();

        MapWorld() {
            super(Rules.ISOLATED, 0L);
        }

        void put(int x, int y, int z, int id) {
            blocks.put(pack(x, y, z), new int[]{id, 0});
        }

        @Override
        protected int storageId(int x, int y, int z) {
            int[] b = blocks.get(pack(x, y, z));
            return b == null ? 0 : b[0];
        }

        @Override
        protected int storageMeta(int x, int y, int z) {
            int[] b = blocks.get(pack(x, y, z));
            return b == null ? 0 : b[1];
        }

        @Override
        protected void storageSet(int x, int y, int z, int id, int meta) {
            blocks.put(pack(x, y, z), new int[]{id, meta});
        }

        @Override
        protected int storageHeight(int x, int z) {
            return 55;
        }

        @Override
        public boolean chunkExists(int chunkX, int chunkZ) {
            return true;
        }
    }
}
