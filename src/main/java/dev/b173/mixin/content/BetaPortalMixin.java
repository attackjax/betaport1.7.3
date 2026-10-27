package dev.b173.mixin.content;

import dev.b173.rules.BetaRules;
import dev.b173.rules.BetaTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(NetherPortalBlock.class)
abstract class BetaPortalMixin {
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void b173$dropOff(BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier effects, boolean intersects, CallbackInfo ci) {
        if (entity instanceof Player && entity.isPassenger() && !level.isClientSide() && BetaRules.betaWorld(level)) {
            entity.stopRiding();
        }
    }

    @Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
    private void b173$betaTrip(ServerLevel level, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTransition> cir) {
        if (BetaRules.betaWorld(level)) {
            cir.setReturnValue(BetaTeleporter.destination(level, entity));
        }
    }
}
