package dev.b173.mixin;

import dev.b173.rules.BetaBeds;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BedBlock.class)
public abstract class BedStandUpMixin {
    @Inject(method = "findStandUpPosition(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;F)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true)
    private static void b173$betaStandUp(EntityType<?> type, CollisionGetter collisions, BlockPos head, Direction facing,
                                         float yaw, CallbackInfoReturnable<Optional<Vec3>> callback) {
        if (type == EntityType.PLAYER && collisions instanceof Level level && BetaRules.betaWorld(level)) {
            BlockPos spot = BetaBeds.emptySpot(level, head, facing, 0);
            callback.setReturnValue(spot == null ? Optional.empty()
                    : Optional.of(new Vec3(spot.getX() + 0.5D, spot.getY() + 0.1D, spot.getZ() + 0.5D)));
        }
    }
}
