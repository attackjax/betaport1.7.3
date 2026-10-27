package dev.b173.core;

public interface BetaChunkSource {
    long seed();

    BetaChunk generate(int chunkX, int chunkZ);
}
