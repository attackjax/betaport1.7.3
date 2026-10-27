package dev.b173.client.mixin.render;

import dev.b173.client.render.BetaItemEntityState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
abstract class ItemEntityRenderStateMixin implements BetaItemEntityState {
    @Unique
    private float b173$blockScale;

    @Override
    public float b173$blockScale() {
        return this.b173$blockScale;
    }

    @Override
    public void b173$setBlockScale(float scale) {
        this.b173$blockScale = scale;
    }
}
