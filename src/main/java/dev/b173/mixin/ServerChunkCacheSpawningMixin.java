package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaRules;
import dev.b173.rules.BetaSpawner;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheSpawningMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @ModifyExpressionValue(method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/world/level/NaturalSpawner;getFilteredSpawningCategories(Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)Ljava/util/List;"))
    private List<MobCategory> b173$betaSpawnsInstead(List<MobCategory> categories) {
        if (BetaRules.active(level.getServer()) && BetaSpawner.runsIn(level)) {
            return List.of();
        }
        return categories;
    }
}
