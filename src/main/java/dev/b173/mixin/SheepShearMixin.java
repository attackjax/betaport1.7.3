package dev.b173.mixin;

import dev.b173.rules.BetaDrops;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public abstract class SheepShearMixin {
    @Inject(method = "shear", at = @At("HEAD"), cancellable = true)
    private void b173$betaShear(ServerLevel level, SoundSource source, ItemStack shears, CallbackInfo callback) {
        if (BetaRules.active(level.getServer())) {
            BetaDrops.shear(level, (Sheep) (Object) this);
            callback.cancel();
        }
    }
}
