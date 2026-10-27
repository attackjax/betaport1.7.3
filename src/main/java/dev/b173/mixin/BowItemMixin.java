package dev.b173.mixin;

import dev.b173.rules.BetaCombat;
import dev.b173.rules.BetaRules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void b173$betaBow(Level level, Player player, InteractionHand hand,
                              CallbackInfoReturnable<InteractionResult> callback) {
        if (BetaRules.betaWorld(level)) {
            callback.setReturnValue(BetaCombat.shootBow(level, player, hand));
        }
    }
}
