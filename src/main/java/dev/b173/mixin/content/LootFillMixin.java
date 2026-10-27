package dev.b173.mixin.content;

import dev.b173.content.BetaConversion;
import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
abstract class LootFillMixin {
    @Inject(method = "fill", at = @At("TAIL"))
    private void b173$betaLoot(Container container, LootParams params, long seed, CallbackInfo ci) {
        BetaConversion.convertContainer(container);
    }
}
