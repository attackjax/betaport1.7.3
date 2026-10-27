package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.b173.rules.BetaExplosions;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
abstract class BlockExplosionDropMixin {
    @ModifyExpressionValue(method = "onExplosionHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Explosion;radius()F"))
    private float b173$betaDropChance(float radius, @Local(argsOnly = true) ServerLevel level) {
        return BetaRules.betaWorld(level) ? BetaExplosions.DROP_RADIUS : radius;
    }

    @ModifyExpressionValue(method = "onExplosionHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Explosion;getBlockInteraction()Lnet/minecraft/world/level/Explosion$BlockInteraction;",
            ordinal = 1))
    private Explosion.BlockInteraction b173$alwaysDecay(Explosion.BlockInteraction interaction,
                                                        @Local(argsOnly = true) ServerLevel level) {
        return interaction == Explosion.BlockInteraction.DESTROY && BetaRules.betaWorld(level)
                ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : interaction;
    }
}
