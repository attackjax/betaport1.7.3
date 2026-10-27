package dev.b173.world;

import dev.b173.Beta173Mod;
import dev.b173.core.BetaBiome;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class BetaBiomeKeys {
    public static final ResourceKey<Biome> RAINFOREST = key(BetaBiome.RAINFOREST);
    public static final ResourceKey<Biome> SWAMPLAND = key(BetaBiome.SWAMPLAND);
    public static final ResourceKey<Biome> SEASONAL_FOREST = key(BetaBiome.SEASONAL_FOREST);
    public static final ResourceKey<Biome> FOREST = key(BetaBiome.FOREST);
    public static final ResourceKey<Biome> SAVANNA = key(BetaBiome.SAVANNA);
    public static final ResourceKey<Biome> SHRUBLAND = key(BetaBiome.SHRUBLAND);
    public static final ResourceKey<Biome> TAIGA = key(BetaBiome.TAIGA);
    public static final ResourceKey<Biome> DESERT = key(BetaBiome.DESERT);
    public static final ResourceKey<Biome> PLAINS = key(BetaBiome.PLAINS);
    public static final ResourceKey<Biome> ICE_DESERT = key(BetaBiome.ICE_DESERT);
    public static final ResourceKey<Biome> TUNDRA = key(BetaBiome.TUNDRA);

    private BetaBiomeKeys() {
    }

    public static ResourceKey<Biome> key(BetaBiome biome) {
        return ResourceKey.create(Registries.BIOME, Beta173Mod.id(biome.id()));
    }
}
