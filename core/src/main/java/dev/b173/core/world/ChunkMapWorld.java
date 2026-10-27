package dev.b173.core.world;

import dev.b173.core.BetaBlocks;
import dev.b173.core.BetaChunk;
import dev.b173.core.BetaChunkSource;
import dev.b173.core.BetaHell;

import java.util.HashMap;
import java.util.Map;

public final class ChunkMapWorld extends BetaWorld {
    private static final class Slot {
        final BetaChunk chunk;
        final int[] heights = new int[256];
        final byte[] skyLight = new byte[32768];

        Slot(BetaChunk chunk) {
            this.chunk = chunk;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    heights[z << 4 | x] = chunk.heightAt(x, z);
                    generateSkylight(x, z);
                }
            }
        }

        private void generateSkylight(int x, int z) {
            int column = x << 11 | z << 7;
            int light = 15;
            int y = 127;
            do {
                light -= BetaBlocks.lightOpacity(chunk.blocks[column | y] & 255);
                if (light > 0) {
                    skyLight[column | y] = (byte) light;
                }
            } while (--y > 0 && light > 0);
        }
    }

    private final BetaChunkSource terrain;
    private final Map<Long, Slot> chunks = new HashMap<>();
    private long lastKey = Long.MIN_VALUE;
    private Slot lastSlot;

    public ChunkMapWorld(BetaChunkSource terrain, Rules rules) {
        super(rules, terrain.seed() ^ 0x5DEECE66DL, terrain instanceof BetaHell);
        this.terrain = terrain;
    }

    public BetaChunkSource terrain() {
        return terrain;
    }

    private static long key(int chunkX, int chunkZ) {
        return (long) chunkX << 32 ^ (chunkZ & 0xFFFFFFFFL);
    }

    private Slot slot(int chunkX, int chunkZ) {
        long key = key(chunkX, chunkZ);
        if (key == lastKey) {
            return lastSlot;
        }
        Slot slot = chunks.get(key);
        if (slot == null) {
            slot = new Slot(terrain.generate(chunkX, chunkZ));
            chunks.put(key, slot);
        }
        lastKey = key;
        lastSlot = slot;
        return slot;
    }

    public BetaChunk chunk(int chunkX, int chunkZ) {
        return slot(chunkX, chunkZ).chunk;
    }

    public boolean isLoaded(int chunkX, int chunkZ) {
        return chunks.containsKey(key(chunkX, chunkZ));
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return true;
    }

    @Override
    protected int storageId(int x, int y, int z) {
        return slot(x >> 4, z >> 4).chunk.blocks[(x & 15) << 11 | (z & 15) << 7 | y] & 255;
    }

    @Override
    protected int storageMeta(int x, int y, int z) {
        return slot(x >> 4, z >> 4).chunk.metadata[(x & 15) << 11 | (z & 15) << 7 | y];
    }

    @Override
    protected void storageSet(int x, int y, int z, int id, int meta) {
        Slot slot = slot(x >> 4, z >> 4);
        int lx = x & 15;
        int lz = z & 15;
        int index = lx << 11 | lz << 7 | y;
        slot.chunk.blocks[index] = (byte) id;
        slot.chunk.metadata[index] = (byte) meta;
        int column = lz << 4 | lx;
        int height = slot.heights[column];
        if (BetaBlocks.lightOpacity(id) != 0) {
            if (y >= height) {
                slot.heights[column] = y + 1;
            }
        } else if (y == height - 1) {
            slot.heights[column] = slot.chunk.heightAt(lx, lz);
        }
    }

    @Override
    protected int storageHeight(int x, int z) {
        return slot(x >> 4, z >> 4).heights[(z & 15) << 4 | (x & 15)];
    }

    @Override
    protected int storedSkyLight(int x, int y, int z) {
        return slot(x >> 4, z >> 4).skyLight[(x & 15) << 11 | (z & 15) << 7 | y];
    }

    @Override
    protected void storeSkyLight(int x, int y, int z, int light) {
        slot(x >> 4, z >> 4).skyLight[(x & 15) << 11 | (z & 15) << 7 | y] = (byte) light;
    }
}
