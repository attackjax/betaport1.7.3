package dev.b173.mixin.vehicle;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityBlockEffectsInvoker {
    @Invoker("applyEffectsFromBlocks")
    void b173$applyEffectsFromBlocks();
}
