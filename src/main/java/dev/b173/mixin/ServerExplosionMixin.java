package dev.b173.mixin;

import dev.b173.rules.BetaExplosions;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ServerExplosion.class)
abstract class ServerExplosionMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Vec3 center;

    @Shadow
    @Final
    private Entity source;

    @Shadow
    @Final
    private float radius;

    @Shadow
    @Final
    private DamageSource damageSource;

    @Shadow
    @Final
    private ExplosionDamageCalculator damageCalculator;

    @Shadow
    @Final
    private Map<Player, Vec3> hitPlayers;

    @Inject(method = "hurtEntities", at = @At("HEAD"), cancellable = true)
    private void b173$betaHurt(CallbackInfo callback) {
        if (BetaRules.betaWorld(level)) {
            BetaExplosions.hurtEntities((ServerExplosion) (Object) this, level, source, center, radius, damageSource,
                    damageCalculator, hitPlayers);
            callback.cancel();
        }
    }

    @Inject(method = "createFire", at = @At("HEAD"), cancellable = true)
    private void b173$noFire(List<BlockPos> blocks, CallbackInfo callback) {
        if (BetaRules.betaWorld(level)) {
            callback.cancel();
        }
    }
}
