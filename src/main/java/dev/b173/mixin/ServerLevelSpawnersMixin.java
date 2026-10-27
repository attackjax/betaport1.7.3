package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.b173.rules.BetaRules;
import dev.b173.rules.BetaSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelSpawnersMixin {
    @Inject(method = "tickCustomSpawners", at = @At("HEAD"))
    private void b173$betaSpawner(boolean spawnEnemies, CallbackInfo callback) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (BetaRules.active(level.getServer()) && BetaSpawner.runsIn(level)) {
            BetaSpawner.tick(level, spawnEnemies);
        }
    }

    @WrapWithCondition(method = "tickCustomSpawners", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/CustomSpawner;tick(Lnet/minecraft/server/level/ServerLevel;Z)V"))
    private boolean b173$betaSpawners(CustomSpawner spawner, ServerLevel level, boolean spawnEnemies) {
        boolean later = spawner instanceof PhantomSpawner || spawner instanceof PatrolSpawner
                || spawner instanceof WanderingTraderSpawner || spawner instanceof CatSpawner
                || spawner instanceof VillageSiege;
        return !later || !BetaRules.active(level.getServer());
    }
}
