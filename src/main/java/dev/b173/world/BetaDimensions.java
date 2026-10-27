package dev.b173.world;

import dev.b173.Beta173Mod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class BetaDimensions {
    public static final ResourceKey<DimensionType> NETHER_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, Beta173Mod.id("the_nether"));
    public static final ResourceKey<Biome> HELL = ResourceKey.create(Registries.BIOME, Beta173Mod.id("hell"));

    private BetaDimensions() {
    }

    public static WorldDimensions withBetaNether(WorldDimensions dimensions, HolderLookup.Provider registries) {
        LevelStem overworld = dimensions.dimensions().get(LevelStem.OVERWORLD);
        LevelStem nether = dimensions.dimensions().get(LevelStem.NETHER);
        if (overworld == null || !(overworld.generator() instanceof BetaChunkGenerator) || nether == null) {
            return dimensions;
        }
        if (!(nether.generator() instanceof NoiseBasedChunkGenerator noise) || !noise.stable(NoiseGeneratorSettings.NETHER)) {
            return dimensions;
        }
        Optional<? extends Holder<DimensionType>> type = registries.lookup(Registries.DIMENSION_TYPE)
                .flatMap(lookup -> lookup.get(NETHER_TYPE));
        Optional<? extends Holder<Biome>> hell = registries.lookup(Registries.BIOME).flatMap(lookup -> lookup.get(HELL));
        if (type.isEmpty() || hell.isEmpty()) {
            return dimensions;
        }
        Map<ResourceKey<LevelStem>, LevelStem> stems = new LinkedHashMap<>(dimensions.dimensions());
        stems.put(LevelStem.NETHER, new LevelStem(type.get(), new BetaNetherGenerator(new FixedBiomeSource(hell.get()))));
        Beta173Mod.LOGGER.info("This Beta world's Nether was saved as this game's; it is Beta's Nether from now on");
        return new WorldDimensions(stems);
    }
}
