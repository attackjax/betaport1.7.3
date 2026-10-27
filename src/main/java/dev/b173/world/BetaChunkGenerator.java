package dev.b173.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.b173.Beta173Mod;
import dev.b173.core.BetaChunk;
import dev.b173.core.BetaClimate;
import dev.b173.core.BetaTerrain;
import dev.b173.core.populate.BetaPopulator;
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
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class BetaChunkGenerator extends ChunkGenerator {
    public static final MapCodec<BetaChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(BetaChunkGenerator::getBiomeSource)
    ).apply(instance, instance.stable(BetaChunkGenerator::new)));

    public static final int MIN_Y = 0;
    public static final int HEIGHT = 128;
    public static final int SEA_LEVEL = 64;

    private static final Object POPULATION_LOCK = new Object();

    private static final boolean TRACE_POPULATION = Boolean.getBoolean("b173.tracePopulation");

    private volatile Seeded seeded;
    private final ThreadLocal<Base> lastBase = new ThreadLocal<>();

    public BetaChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource, biome -> BiomeGenerationSettings.EMPTY);
    }

    private record Seeded(long seed, BetaTerrain terrain, BetaPopulator populator) {
        Seeded(long seed) {
            this(seed, new BetaTerrain(seed));
        }

        private Seeded(long seed, BetaTerrain terrain) {
            this(seed, terrain, new BetaPopulator(terrain));
        }
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState,
                                                    long levelSeed) {
        seeded(levelSeed);
        return ChunkGeneratorStructureState.createForFlat(randomState, levelSeed, biomeSource, Stream.empty());
    }

    private Seeded seeded(long seed) {
        Seeded current = seeded;
        if (current == null || current.seed() != seed) {
            synchronized (this) {
                current = seeded;
                if (current == null || current.seed() != seed) {
                    current = new Seeded(seed);
                    seeded = current;
                    if (biomeSource instanceof BetaBiomeSource betaBiomes) {
                        betaBiomes.installClimate(current.terrain().climate());
                    }
                }
            }
        }
        return current;
    }

    private Seeded seeded() {
        Seeded current = seeded;
        return current != null ? current : seeded(0L);
    }

    public long seed() {
        return seeded().seed();
    }

    public BetaClimate climate() {
        return seeded().terrain().climate();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        BetaChunk beta = seeded().terrain().generate(pos.x(), pos.z());
        writeTerrain(beta, chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static void writeTerrain(BetaChunk beta, ChunkAccess chunk) {
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int sectionY = 0; sectionY < HEIGHT >> 4; sectionY++) {
            int baseY = sectionY << 4;
            if (chunk.isOutsideBuildHeight(baseY)) {
                continue;
            }
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(baseY));
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int column = x << 11 | z << 7;
                    for (int dy = 0; dy < 16; dy++) {
                        int y = baseY | dy;
                        int id = beta.blocks[column | y] & 255;
                        if (id == 0) {
                            continue;
                        }
                        BlockState state = BetaBlockStates.modern(id, beta.metadata[column | y]);
                        section.setBlockState(x, dy, z, state, false);
                        oceanFloor.update(x, y, z, state);
                        worldSurface.update(x, y, z, state);
                    }
                }
            }
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        Seeded current = seeded(level.getSeed());
        ChunkPos pos = chunk.getPos();
        synchronized (POPULATION_LOCK) {
            if (TRACE_POPULATION) {
                Beta173Mod.LOGGER.info("b173 populate {} {}", pos.x(), pos.z());
            }
            RegionBetaWorld world = new RegionBetaWorld(level, current.terrain(), pos.x(), pos.z());
            current.populator().populate(world, pos.x(), pos.z());
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
        Seeded current = seeded();
        Base cached = lastBase.get();
        if (cached == null || cached.seed() != current.seed()
                || cached.chunk().chunkX != chunkX || cached.chunk().chunkZ != chunkZ) {
            cached = new Base(current.seed(), current.terrain().generate(chunkX, chunkZ));
            lastBase.set(cached);
        }
        return cached.chunk();
    }

    private record Base(long seed, BetaChunk chunk) {
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        BetaClimate.Point point = climate().at(pos.getX(), pos.getZ());
        info.add(String.format(Locale.ROOT, "Beta 1.7.3: %s  T %.3f  H %.3f",
                point.biome().displayName(), point.temperature(), point.humidity()));
    }
}
