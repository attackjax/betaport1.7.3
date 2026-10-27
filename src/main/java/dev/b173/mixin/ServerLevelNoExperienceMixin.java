package dev.b173.mixin;

import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelNoExperienceMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void b173$noExperience(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof ExperienceOrb && BetaRules.active(((ServerLevel) (Object) this).getServer())) {
            callback.setReturnValue(false);
        }
    }
}
