package dev.b173.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.b173.Beta173Mod;
import dev.b173.core.BetaChunk;
import dev.b173.core.BetaHell;
import dev.b173.core.populate.BetaHellPopulator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class BetaNetherGenerator extends ChunkGenerator {
    public static final MapCodec<BetaNetherGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(BetaNetherGenerator::getBiomeSource)
    ).apply(instance, instance.stable(BetaNetherGenerator::new)));

    public static final int MIN_Y = 0;
    public static final int HEIGHT = 128;
    public static final int SEA_LEVEL = BetaHell.LAVA_LEVEL;

    private static final Object POPULATION_LOCK = new Object();
    private static final boolean TRACE_POPULATION = Boolean.getBoolean("b173.tracePopulation");

    private volatile BetaHell hell;
    private final ThreadLocal<Base> lastBase = new ThreadLocal<>();

    public BetaNetherGenerator(BiomeSource biomeSource) {
        super(biomeSource, biome -> BiomeGenerationSettings.EMPTY);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState,
                                                    long levelSeed) {
        hell(levelSeed);
        return ChunkGeneratorStructureState.createForFlat(randomState, levelSeed, biomeSource, Stream.empty());
    }

    private BetaHell hell(long seed) {
        BetaHell current = hell;
        if (current == null || current.seed() != seed) {
            synchronized (this) {
                current = hell;
                if (current == null || current.seed() != seed) {
                    current = new BetaHell(seed);
                    hell = current;
                }
            }
        }
        return current;
    }

    private BetaHell hell() {
        BetaHell current = hell;
        return current != null ? current : hell(0L);
    }

    public long seed() {
        return hell().seed();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        BetaChunkGenerator.writeTerrain(hell().generate(pos.x(), pos.z()), chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        BetaHell current = hell(level.getSeed());
        ChunkPos pos = chunk.getPos();
        synchronized (POPULATION_LOCK) {
            if (TRACE_POPULATION) {
                Beta173Mod.LOGGER.info("b173 populate nether {} {}", pos.x(), pos.z());
            }
            RegionBetaWorld world = new RegionBetaWorld(level, current, pos.x(), pos.z());
            BetaHellPopulator.populate(world, pos.x(), pos.z());
            world.finish();
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState,
                             ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor,
                             RandomState randomState) {
        BetaChunk chunk = baseChunk(x >> 4, z >> 4);
        for (int y = HEIGHT - 1; y >= 0; y--) {
            int index = BetaChunk.index(x & 15, y, z & 15);
            BlockState state = BetaBlockStates.modern(chunk.blocks[index] & 255, chunk.metadata[index]);
            if (type.isOpaque().test(state)) {
                return y + 1;
            }
        }
        return MIN_Y;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        BetaChunk chunk = baseChunk(x >> 4, z >> 4);
        BlockState[] states = new BlockState[HEIGHT];
        for (int y = 0; y < HEIGHT; y++) {
            int index = BetaChunk.index(x & 15, y, z & 15);
            states[y] = BetaBlockStates.modern(chunk.blocks[index] & 255, chunk.metadata[index]);
        }
        return new NoiseColumn(MIN_Y, states);
    }

    private BetaChunk baseChunk(int chunkX, int chunkZ) {
        BetaHell current = hell();
        Base cached = lastBase.get();
        if (cached == null || cached.seed() != current.seed()
                || cached.chunk().chunkX != chunkX || cached.chunk().chunkZ != chunkZ) {
            cached = new Base(current.seed(), current.generate(chunkX, chunkZ));
            lastBase.set(cached);
        }
        return cached.chunk();
    }

    private record Base(long seed, BetaChunk chunk) {
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Beta 1.7.3: Hell");
    }
}
