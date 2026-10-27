package dev.b173.mixin;

import dev.b173.rules.BetaSpawnPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(PlayerSpawnFinder.class)
abstract class BetaPlayerSpawnMixin {
    @Inject(method = "findSpawn", at = @At("HEAD"), cancellable = true)
    private static void b173$betaPlace(ServerLevel level, BlockPos spawn, CallbackInfoReturnable<CompletableFuture<Vec3>> cir) {
        if (BetaSpawnPoint.applies(level)) {
            cir.setReturnValue(CompletableFuture.completedFuture(BetaSpawnPoint.place(level, spawn)));
        }
    }
}
