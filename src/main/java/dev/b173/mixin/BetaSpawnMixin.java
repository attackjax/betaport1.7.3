package dev.b173.mixin;

import dev.b173.rules.BetaSpawnPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class BetaSpawnMixin {
    @Inject(method = "setInitialSpawn", at = @At("HEAD"), cancellable = true)
    private static void b173$betaSpawn(ServerLevel level, ServerLevelData data, boolean bonusChest, boolean debug,
                                       LevelLoadListener listener, CallbackInfo ci) {
        if (BetaSpawnPoint.applies(level) && BetaSpawnPoint.setInitial(level, data)) {
            ci.cancel();
        }
    }
}
