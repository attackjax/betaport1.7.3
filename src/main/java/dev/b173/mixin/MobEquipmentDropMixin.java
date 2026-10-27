package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobEquipmentDropMixin {
    @ModifyExpressionValue(method = "dropCustomDeathLoot", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/DropChances;byEquipment(Lnet/minecraft/world/entity/EquipmentSlot;)F"))
    private float b173$noSpawnedEquipment(float chance, ServerLevel level, DamageSource source, boolean killedByPlayer) {
        if (chance <= DropChances.PRESERVE_ITEM_DROP_CHANCE_THRESHOLD
                && BetaMobs.isBeta(((Mob) (Object) this).getType()) && BetaRules.active(level.getServer())) {
            return 0.0F;
        }
        return chance;
    }
}
