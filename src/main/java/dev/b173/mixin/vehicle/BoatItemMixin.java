package dev.b173.mixin.vehicle;

import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatItem.class)
abstract class BoatItemMixin {
    @Shadow
    @Final
    private EntityType<? extends AbstractBoat> entityType;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void b173$betaBoat(Level level, Player player, InteractionHand hand,
                               CallbackInfoReturnable<InteractionResult> info) {
        if (!BetaRules.betaWorld(level)) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(5.0D));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            info.setReturnValue(InteractionResult.PASS);
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            AbstractBoat boat = this.entityType.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
            if (boat == null) {
                info.setReturnValue(InteractionResult.FAIL);
                return;
            }
            double x = (double) ((float) hit.getBlockPos().getX() + 0.5F);
            double y = (double) ((float) hit.getBlockPos().getY() + 1.0F);
            double z = (double) ((float) hit.getBlockPos().getZ() + 0.5F);
            boat.setInitialPos(x, y, z);
            EntityType.createDefaultStackConfig(serverLevel, stack, player).accept(boat);
            boat.setYRot(boat instanceof Boat ? 90.0F : player.getYRot());
            serverLevel.addFreshEntity(boat);
            serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, hit.getLocation());
            stack.consume(1, player);
        }
        player.awardStat(Stats.ITEM_USED.get((BoatItem) (Object) this));
        info.setReturnValue(InteractionResult.SUCCESS);
    }
}
