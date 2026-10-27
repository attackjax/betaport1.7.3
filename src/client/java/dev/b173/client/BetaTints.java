package dev.b173.client;

import dev.b173.core.BetaColors;
import dev.b173.core.ClimateCache;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import java.util.List;

final class BetaTints {
    private static final int OPAQUE = 0xFF000000;

    private BetaTints() {
    }

    private static @Nullable ClimateCache climate(BlockAndTintGetter level) {
        return level.getHeight() == 0 ? null : BetaClientState.climate();
    }

    static void register() {
        BlockColorRegistry.register(List.of(GRASS_BLOCK), Blocks.GRASS_BLOCK);
        BlockColorRegistry.register(List.of(TALL_GRASS), Blocks.SHORT_GRASS, Blocks.FERN);
        BlockColorRegistry.register(List.of(OAK_LEAVES), Blocks.OAK_LEAVES);
    }

    static int grass(ClimateCache climate, int x, int z) {
        return GrassColor.get(climate.temperature(x, z), climate.humidity(x, z));
    }

    static int tallGrass(ClimateCache climate, int x, int y, int z) {
        long sample = BetaColors.tallGrassSample(x, y, z);
        return grass(climate, BetaColors.sampleX(sample), BetaColors.sampleZ(sample));
    }

    static int foliage(ClimateCache climate, int x, int z) {
        return FoliageColor.get(climate.temperature(x, z), climate.humidity(x, z));
    }

    private static final BlockTintSource GRASS_BLOCK = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return OPAQUE | GrassColor.get(0.5D, 1.0D);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            ClimateCache climate = climate(level);
            if (climate == null) {
                return OPAQUE | BiomeColors.getAverageGrassColor(level, pos);
            }
            return OPAQUE | grass(climate, pos.getX(), pos.getZ());
        }
    };

    private static final BlockTintSource TALL_GRASS = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return OPAQUE | GrassColor.get(0.5D, 1.0D);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            ClimateCache climate = climate(level);
            if (climate == null) {
                return OPAQUE | BiomeColors.getAverageGrassColor(level, pos);
            }
            return OPAQUE | tallGrass(climate, pos.getX(), pos.getY(), pos.getZ());
        }
    };

    private static final BlockTintSource OAK_LEAVES = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return OPAQUE | FoliageColor.get(0.5D, 1.0D);
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            ClimateCache climate = climate(level);
            if (climate == null) {
                return OPAQUE | BiomeColors.getAverageFoliageColor(level, pos);
            }
            return OPAQUE | foliage(climate, pos.getX(), pos.getZ());
        }
    };
}
