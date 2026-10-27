package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaRules;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
public abstract class ZombieWaterMixin {
    @ModifyReturnValue(method = "convertsInWater", at = @At("RETURN"))
    private boolean b173$staysAZombie(boolean converts) {
        Zombie self = (Zombie) (Object) this;
        return converts && !(self.getType() == EntityType.ZOMBIE && BetaRules.betaWorld(self.level()));
    }
}
