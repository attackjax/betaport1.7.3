package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.b173.core.BetaMath;
import dev.b173.rules.BetaHealthLeft;
import dev.b173.rules.BetaRules;
import dev.b173.rules.ai.BetaSlimeBrain;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
abstract class SlimeMixin {
    @Shadow
    public float targetSquish;

    @Shadow
    public float squish;

    @Shadow
    public abstract int getSize();

    private boolean b173$beta() {
        Slime self = (Slime) (Object) this;
        return self.getType() == EntityType.SLIME && BetaRules.betaWorld(self.level());
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void b173$betaTouch(Player player, CallbackInfo callback) {
        if (b173$beta()) {
            Slime self = (Slime) (Object) this;
            if (self.isEffectiveAi()) {
                BetaSlimeBrain.touch(self, player);
            }
            callback.cancel();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void b173$betaSplit(Entity.RemovalReason reason, CallbackInfo callback) {
        Slime self = (Slime) (Object) this;
        if (b173$beta() && !self.level().isClientSide() && self.isDeadOrDying()) {
            BetaSlimeBrain.split(self, ((BetaHealthLeft) self).b173$healthLeft());
        }
    }

    @ModifyExpressionValue(method = "remove", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/Slime;isDeadOrDying()Z"))
    private boolean b173$noModernSplit(boolean dying) {
        return dying && !b173$beta();
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private boolean b173$noModernSplash(Level level, ParticleOptions particle, double x, double y, double z,
                                        double dx, double dy, double dz) {
        return !b173$beta();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/Slime;getSquishSound()Lnet/minecraft/sounds/SoundEvent;"))
    private void b173$betaSplash(CallbackInfo callback) {
        if (!b173$beta()) {
            return;
        }
        Slime self = (Slime) (Object) this;
        RandomSource random = self.getRandom();
        int size = getSize();
        for (int i = 0; i < size * 8; i++) {
            float angle = random.nextFloat() * 3.1415927F * 2.0F;
            float reach = random.nextFloat() * 0.5F + 0.5F;
            float dx = BetaMath.sin(angle) * (float) size * 0.5F * reach;
            float dz = BetaMath.cos(angle) * (float) size * 0.5F * reach;
            self.level().addParticle(ParticleTypes.ITEM_SLIME, self.getX() + (double) dx,
                    self.getBoundingBox().minY, self.getZ() + (double) dz, 0.0D, 0.0D, 0.0D);
        }
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/Slime;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private boolean b173$betaLandingSound(Slime self, SoundEvent sound, float volume, float pitch) {
        return !b173$beta() || getSize() > 2;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void b173$betaSquish(CallbackInfo callback) {
        if (b173$beta()) {
            squish = targetSquish;
        }
    }

    @ModifyReturnValue(method = "getSoundVolume", at = @At("RETURN"))
    private float b173$betaVolume(float volume) {
        return b173$beta() ? BetaSlimeBrain.VOLUME : volume;
    }
}
