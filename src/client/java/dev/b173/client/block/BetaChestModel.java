package dev.b173.client.block;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

final class BetaChestModel extends WrapperBlockStateModel {
    private static final String ITEM_LIKE = "single_south";

    private final Map<String, ExtraModelKey<BlockStateModel>> variants;

    BetaChestModel(BlockStateModel vanilla, Map<String, ExtraModelKey<BlockStateModel>> variants) {
        super(vanilla);
        this.variants = variants;
    }

    private BlockStateModel model(String name) {
        BlockStateModel model = ((FabricModelManager) (Object) Minecraft.getInstance().getModelManager())
                .getModel(this.variants.get(name));
        return model != null ? model : this.wrapped;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        BlockStateModel model = this.model(BetaChests.variant(level, pos, state));
        ((FabricBlockStateModel) model).emitQuads(emitter, level, pos, state, random, cullTest);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        this.model(ITEM_LIKE).collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.model(ITEM_LIKE).particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return this.model(ITEM_LIKE).materialFlags();
    }

    @Override
    public boolean hasMaterialFlag(int flag) {
        return this.model(ITEM_LIKE).hasMaterialFlag(flag);
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this.materialFlags();
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, int flag) {
        return this.hasMaterialFlag(flag);
    }
}
