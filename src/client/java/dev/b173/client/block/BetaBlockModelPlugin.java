package dev.b173.client.block;

import dev.b173.client.model.BetaModels;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public final class BetaBlockModelPlugin implements ModelLoadingPlugin {
    private static final String[] CHEST_VARIANTS = {
            "single_north", "single_south", "single_west", "single_east",
            "z_north_east", "z_north_west", "z_south_east", "z_south_west",
            "x_west_south", "x_west_north", "x_east_south", "x_east_north"
    };

    private final Map<String, ExtraModelKey<BlockStateModel>> chests = new HashMap<>();

    public BetaBlockModelPlugin() {
        for (String variant : CHEST_VARIANTS) {
            this.chests.put(variant, ExtraModelKey.create(() -> "b173 chest " + variant));
        }
    }

    @Override
    public void initialize(Context context) {
        if (!BetaModels.packLoaded()) {
            return;
        }
        for (Map.Entry<String, ExtraModelKey<BlockStateModel>> chest : this.chests.entrySet()) {
            context.addModel(chest.getValue(), SimpleUnbakedExtraModel.blockStateModel(
                    Identifier.fromNamespaceAndPath("b173", "block/chest_" + chest.getKey())));
        }
        context.modifyBlockModelAfterBake().register((model, bake) ->
                bake.state().is(Blocks.CHEST) ? new BetaChestModel(model, this.chests) : model);
    }
}
