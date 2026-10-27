package dev.b173.core;

public final class BetaChunk {
    public static final int HEIGHT = 128;
    public static final int VOLUME = 16 * 16 * HEIGHT;

    public final int chunkX;
    public final int chunkZ;
    public final byte[] blocks;
    public final byte[] metadata;

    public BetaChunk(int chunkX, int chunkZ) {
        this(chunkX, chunkZ, new byte[VOLUME], new byte[VOLUME]);
    }

    public BetaChunk(int chunkX, int chunkZ, byte[] blocks, byte[] metadata) {
        if (blocks.length != VOLUME || metadata.length != VOLUME) {
            throw new IllegalArgumentException("Beta chunks hold exactly " + VOLUME + " blocks");
        }
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = blocks;
        this.metadata = metadata;
    }

    public static int index(int x, int y, int z) {
        return x << 11 | z << 7 | y;
    }

    public int id(int x, int y, int z) {
        return blocks[index(x, y, z)] & 255;
    }

    public int meta(int x, int y, int z) {
        return metadata[index(x, y, z)];
    }

    public void set(int x, int y, int z, int id, int meta) {
        int i = index(x, y, z);
        blocks[i] = (byte) id;
        metadata[i] = (byte) meta;
    }

    public int heightAt(int x, int z) {
        int base = x << 11 | z << 7;
        int y = HEIGHT;
        while (y > 0 && BetaBlocks.lightOpacity(blocks[base + y - 1] & 255) == 0) {
            y--;
        }
        return y;
    }

    public BetaChunk copy() {
        return new BetaChunk(chunkX, chunkZ, blocks.clone(), metadata.clone());
    }
}
