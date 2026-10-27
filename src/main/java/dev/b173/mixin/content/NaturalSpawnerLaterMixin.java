package dev.b173.mixin.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerLaterMixin {
    @Inject(method = "isValidSpawnPostitionForType", at = @At("HEAD"), cancellable = true)
    private static void b173$onlyBetaMobs(ServerLevel level, MobCategory category, StructureManager structures,
                                          ChunkGenerator generator, MobSpawnSettings.SpawnerData data,
                                          BlockPos.MutableBlockPos pos, double distance,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (!data.type().isEnabled(level.enabledFeatures())) {
            cir.setReturnValue(false);
        }
    }
}
