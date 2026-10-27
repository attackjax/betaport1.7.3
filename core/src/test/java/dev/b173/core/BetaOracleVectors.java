package dev.b173.core;

import dev.b173.core.populate.BetaPopulator;
import dev.b173.core.world.BetaWorld;
import dev.b173.core.world.ChunkMapWorld;

import java.util.TreeSet;

final class BetaOracleVectors {
    private BetaOracleVectors() {
    }

    record Area(String name, long seed, int minX, int minZ, int maxX, int maxZ, long blocks, long light, long tiles) {
    }

    private static final Area[] AREAS = {
            new Area("pack.png seed", 3257840388504953787L, -5, -5, 4, 4, 0x5ccfee910846d2d4L, 0x75544c6661fa88a5L, 0x4717efcdf13b8b32L),
            new Area("seed 0", 0L, -5, -5, 4, 4, 0xcdd660c14f2119e4L, 0x59f52b9455f4dadfL, 0x958258c7cbfe9a75L),
            new Area("negative seed", -987654321L, 20, -40, 29, -31, 0xff6c227bf673f5e2L, 0x634de0e3b61690dfL, 0xe288fd80a13e6087L),
            new Area("tundra", 0L, -120, -120, -115, -115, 0x066107a507bc6435L, 0xbb053f4aa5bbacc1L, 0x9589e89994bb8b2fL),
            new Area("desert", 0L, 0, 78, 5, 83, 0xfdf895dcaf7c8648L, 0xe9f8bd01340f8809L, 0xb01abb842509d415L),
            new Area("taiga", 0L, -84, -120, -79, -115, 0xf527d6c58f8a57d3L, 0x5a7af4bc77de9518L, 0xcbf29ce484222325L),
            new Area("swampland", 0L, -108, 18, -103, 23, 0x9dc61f7fd0383b74L, 0x3a3e0b0fbd1fdd43L, 0x45e026e127c2b669L),
            new Area("rainforest", 0L, 6, -24, 11, -19, 0xb0ba289d2653d90dL, 0xbca95650b4eb4969L, 0x9002fce10a61cb68L),
            new Area("forest", 0L, -108, 48, -103, 53, 0x75444b1f341a8bbbL, 0x196ef5e110116c68L, 0xcbf29ce484222325L),
            new Area("Far Lands edge", 0L, 784424, 0, 784429, 5, 0x6bb700f4c68c9881L, 0x10c3b535c74e2e64L, 0xcbf29ce484222325L),
            new Area("Far Lands corner", 3257840388504953787L, 784424, 784424, 784427, 784427, 0xc7e5a081a6a3ed8eL, 0xde1bbece1dbad69eL, 0xcbf29ce484222325L)
    };

    static void run(TestRunner t) {
        t.section("Faithful population against the real Beta 1.7.3 generator (whole areas)");
        for (Area area : AREAS) {
            BetaTerrain terrain = new BetaTerrain(area.seed());
            ChunkMapWorld world = new ChunkMapWorld(terrain, BetaWorld.Rules.FAITHFUL);
            for (int x = area.minX() - 2; x <= area.maxX() + 2; x++) {
                for (int z = area.minZ() - 2; z <= area.maxZ() + 2; z++) {
                    world.chunk(x, z);
                }
            }
            BetaPopulator populator = new BetaPopulator(terrain);
            for (int x = area.minX() - 2; x <= area.maxX() + 1; x++) {
                for (int z = area.minZ() - 2; z <= area.maxZ() + 1; z++) {
                    populator.populate(world, x, z);
                }
            }
            long blocks = FNV_OFFSET;
            long light = FNV_OFFSET;
            for (int x = area.minX() * 16; x < (area.maxX() + 1) * 16; x++) {
                for (int z = area.minZ() * 16; z < (area.maxZ() + 1) * 16; z++) {
                    for (int y = 0; y < 128; y++) {
                        blocks = mix(blocks, world.getBlockId(x, y, z));
                        blocks = mix(blocks, world.getBlockMetadata(x, y, z));
                        light = mix(light, world.skyLight(x, y, z));
                    }
                }
            }
            long tiles = tiles(OracleCheck.tiles(world, area.minX(), area.minZ(), area.maxX(), area.maxZ()));
            int chunks = (area.maxX() - area.minX() + 1) * (area.maxZ() - area.minZ() + 1);
            t.check(String.format("%s: seed %d, %d chunks from %d,%d", area.name(), area.seed(), chunks, area.minX(), area.minZ()),
                    blocks == area.blocks() && light == area.light() && tiles == area.tiles(),
                    String.format("blocks %s, light %s, tiles %s", blocks == area.blocks() ? "match" : "differ",
                            light == area.light() ? "match" : "differ", tiles == area.tiles() ? "match" : "differ"));
        }
    }

    static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    static long mix(long hash, int value) {
        return (hash ^ (value & 255)) * FNV_PRIME;
    }

    static long tiles(TreeSet<String> lines) {
        long hash = FNV_OFFSET;
        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {
                hash = mix(hash, line.charAt(i));
            }
            hash = mix(hash, '\n');
        }
        return hash;
    }
}
