package dev.b173.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.b173.core.BetaBiome;
import dev.b173.core.BetaClimate;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.stream.Stream;

public final class BetaBiomeSource extends BiomeSource {
    public static final MapCodec<BetaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveElement(BetaBiomeKeys.RAINFOREST),
            RegistryOps.retrieveElement(BetaBiomeKeys.SWAMPLAND),
            RegistryOps.retrieveElement(BetaBiomeKeys.SEASONAL_FOREST),
            RegistryOps.retrieveElement(BetaBiomeKeys.FOREST),
            RegistryOps.retrieveElement(BetaBiomeKeys.SAVANNA),
            RegistryOps.retrieveElement(BetaBiomeKeys.SHRUBLAND),
            RegistryOps.retrieveElement(BetaBiomeKeys.TAIGA),
            RegistryOps.retrieveElement(BetaBiomeKeys.DESERT),
            RegistryOps.retrieveElement(BetaBiomeKeys.PLAINS),
            RegistryOps.retrieveElement(BetaBiomeKeys.ICE_DESERT),
            RegistryOps.retrieveElement(BetaBiomeKeys.TUNDRA)
    ).apply(instance, instance.stable(BetaBiomeSource::new)));

    private final List<Holder<Biome>> biomes;
    private volatile BetaClimate climate = new BetaClimate(0L);
    private final ThreadLocal<Tile> tiles = ThreadLocal.withInitial(Tile::new);

    public BetaBiomeSource(Holder.Reference<Biome> rainforest, Holder.Reference<Biome> swampland,
                           Holder.Reference<Biome> seasonalForest, Holder.Reference<Biome> forest,
                           Holder.Reference<Biome> savanna, Holder.Reference<Biome> shrubland,
                           Holder.Reference<Biome> taiga, Holder.Reference<Biome> desert,
                           Holder.Reference<Biome> plains, Holder.Reference<Biome> iceDesert,
                           Holder.Reference<Biome> tundra) {
        this.biomes = List.of(rainforest, swampland, seasonalForest, forest, savanna, shrubland, taiga, desert,
                plains, iceDesert, tundra);
    }

    void installClimate(BetaClimate climate) {
        this.climate = climate;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        return biomes.get(betaBiome(quartX, quartZ).ordinal());
    }

    public Holder<Biome> holder(BetaBiome biome) {
        return biomes.get(biome.ordinal());
    }

    private BetaBiome betaBiome(int quartX, int quartZ) {
        BetaClimate current = climate;
        Tile tile = tiles.get();
        int tileX = quartX >> 2;
        int tileZ = quartZ >> 2;
        if (tile.climate != current || tile.tileX != tileX || tile.tileZ != tileZ) {
            for (int dx = 0; dx < 4; dx++) {
                for (int dz = 0; dz < 4; dz++) {
                    int blockX = ((tileX << 2) + dx) * 4 + 2;
                    int blockZ = ((tileZ << 2) + dz) * 4 + 2;
                    tile.cells[dx * 4 + dz] = current.biomeAt(blockX, blockZ);
                }
            }
            tile.climate = current;
            tile.tileX = tileX;
            tile.tileZ = tileZ;
        }
        return tile.cells[(quartX & 3) * 4 + (quartZ & 3)];
    }

    private static final class Tile {
        BetaClimate climate;
        int tileX = Integer.MIN_VALUE;
        int tileZ = Integer.MIN_VALUE;
        final BetaBiome[] cells = new BetaBiome[16];
    }
}
