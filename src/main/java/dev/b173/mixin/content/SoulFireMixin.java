package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BaseFireBlock.class)
abstract class SoulFireMixin {
    @ModifyExpressionValue(method = "getState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/SoulFireBlock;canSurviveOnBlock(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean b173$noSoulFire(boolean soul) {
        return false;
    }
}
