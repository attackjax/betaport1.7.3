package dev.b173.client.visual;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Set;

public final class BetaVisuals {
    private static final int WHITE = 0xFFFFFFFF;

    private BetaVisuals() {
    }

    public static void register() {
        BlockTintSource water = untintedWithBetaPack(BlockTintSources.water());
        FluidRenderingRegistry.register(Fluids.WATER, Fluids.FLOWING_WATER, new FluidModel.Unbaked(
                new Material(Identifier.withDefaultNamespace("block/water_still")),
                new Material(Identifier.withDefaultNamespace("block/water_flow")),
                new Material(Identifier.withDefaultNamespace("block/water_overlay")),
                water));
        BlockColorRegistry.register(List.of(untintedWithBetaPack(BlockTintSources.sugarCane())), Blocks.SUGAR_CANE);
    }

    private static BlockTintSource untintedWithBetaPack(BlockTintSource modern) {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return BetaPack.active() ? WHITE : modern.color(state);
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return BetaPack.active() ? WHITE : modern.colorInWorld(state, level, pos);
            }

            @Override
            public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return BetaPack.active() ? WHITE : modern.colorAsTerrainParticle(state, level, pos);
            }

            @Override
            public Set<Property<?>> relevantProperties() {
                return modern.relevantProperties();
            }
        };
    }
}
